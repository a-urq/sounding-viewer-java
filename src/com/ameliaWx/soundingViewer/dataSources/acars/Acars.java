package com.ameliaWx.soundingViewer.dataSources.acars;

import com.ameliaWx.soundingViewer.Sounding;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Acars {
    public static TreeMap<String, String> getAcarsFilesByDayAndHour(DateTime d) throws IOException {
        String url = String.format("https://sharp.weather.ou.edu/soundings/acars/%04d/%02d/%02d/%02d/",
                d.getYear(), d.getMonthOfYear(), d.getDayOfMonth(), d.getHourOfDay());

        Document doc = Jsoup.connect(url).get();

        ArrayList<String> linksInFolder = new ArrayList<>();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("href");
            if(href.charAt(0) != '?' && href.charAt(0) != '/') {
                linksInFolder.add(href);
            }
        }

        TreeMap<String, String> acarsFileList = new TreeMap<>();
        for(String link : linksInFolder){
            String stationCode = link.substring(0, 3);
            String timeCode = link.substring(4, 8);

            AcarsStation station = AcarsStations.acarsStationFromCode(stationCode);
            if(station == null) {
                System.err.println("Station \"" + stationCode + "\" not found in ACARS key! Please tell Amelia about this so she can manually update the key.");
            }
            assert station != null;
            String stationName = station.location;
            String timeString = timeCode.substring(0, 2) + ":" + timeCode.substring(2, 4) + "Z";

            String linkAlias = stationName + " | " + timeString;

            acarsFileList.put(url + link, linkAlias);
        }

        return acarsFileList;
    }

    // Uses convective day (12Z-12Z) for ease of use. This way, files from shortly after 00z from the same event will
    // not be excluded.
    public static TreeMap<String, String> getAcarsFilesByDayAndStation(DateTime d, String stationCode) throws IOException {
        DateTime startOfConvectiveDay = new DateTime(d.getYear(), d.getMonthOfYear(), d.getDayOfMonth(), 12, 0,
                DateTimeZone.UTC);
        DateTime endOfConvectiveDay = startOfConvectiveDay.plusDays(1);

        DateTime queryHour = startOfConvectiveDay;

        TreeMap<String, String> acarsFileList = new TreeMap<>();
        while(queryHour.isBefore(endOfConvectiveDay)) {
            String url = String.format("https://sharp.weather.ou.edu/soundings/acars/%04d/%02d/%02d/%02d/",
                    queryHour.getYear(), queryHour.getMonthOfYear(), queryHour.getDayOfMonth(), queryHour.getHourOfDay());

            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (HttpStatusException e) {
                queryHour = queryHour.plusHours(1);
                continue;
            }

            ArrayList<String> linksInFolder = new ArrayList<>();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");
                if (href.charAt(0) != '?' && href.charAt(0) != '/') {
                    String sCode = href.substring(0, 3);

                    if(stationCode.equals(sCode)) {
                        linksInFolder.add(href);
                    }
                }
            }

            AcarsStation station = AcarsStations.acarsStationFromCode(stationCode);
            String stationName = station.location;
            for (String link : linksInFolder) {
                String timeCode = link.substring(4, 8);

                String timeString = timeCode.substring(0, 2) + ":" + timeCode.substring(2, 4) + "Z";

                String linkAlias = stationName + " | " + timeString;

                acarsFileList.put(url + link, linkAlias);
            }

            queryHour = queryHour.plusHours(1);
        }

        return acarsFileList;
    }

    public static TreeMap<String, String> getRecentAcarsFilesByStation(String stationCode) throws IOException {
        return getRecentAcarsFilesByStation(stationCode, 24);
    }

    public static TreeMap<String, String> getRecentAcarsFilesByStation(String stationCode, int searchbackHours) throws IOException {
        DateTime now = DateTime.now(DateTimeZone.UTC);

        DateTime queryHour = now.minusHours(searchbackHours);

        TreeMap<String, String> acarsFileList = new TreeMap<>();
        while(!queryHour.isAfter(now)) {
            String url = String.format("https://sharp.weather.ou.edu/soundings/acars/%04d/%02d/%02d/%02d/",
                    queryHour.getYear(), queryHour.getMonthOfYear(), queryHour.getDayOfMonth(), queryHour.getHourOfDay());

            Document doc;
            try {
                doc = Jsoup.connect(url).get();
            } catch (HttpStatusException e) {
                queryHour = queryHour.plusHours(1);
                continue;
            }

            ArrayList<String> linksInFolder = new ArrayList<>();
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");
                if (href.charAt(0) != '?' && href.charAt(0) != '/') {
                    String sCode = href.substring(0, 3);

                    if(stationCode.equals(sCode)) {
                        linksInFolder.add(href);
                    }
                }
            }

            AcarsStation station = AcarsStations.acarsStationFromCode(stationCode);
            String stationName = station.location;
            for (String link : linksInFolder) {
                String timeCode = link.substring(4, 8);

                String timeString = String.format("%04d-%02d-%02d %s:%sZ", queryHour.getYear(),
                        queryHour.getMonthOfYear(), queryHour.getDayOfMonth(), timeCode.substring(0, 2),
                        timeCode.substring(2, 4));

                String linkAlias = stationName + " | " + timeString;

                acarsFileList.put(url + link, linkAlias);
            }

            queryHour = queryHour.plusHours(1);
        }

        acarsFileList = reverseSortMap(acarsFileList);
        return acarsFileList;
    }

    // Generic method to sort map in Java by the reverse ordering of its keys
    public static TreeMap<String, String> reverseSortMap(Map<String, String> map)
    {
        TreeMap<String, String> treeMap = new TreeMap<>(Comparator.reverseOrder());
        treeMap.putAll(map);
        return treeMap;
    }

    public static Sounding readAcarsFile(File f) throws FileNotFoundException {
        Scanner sc = new Scanner(f);

        boolean title = false;
        boolean raw = false;

        String rawTitle = "";
        List<double[]> rawData = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            if (title) {
                rawTitle = line;
            }

            if ("%END%".equals(line)) {
                raw = false;
            }

            if (raw) {
                String[] tokens = line.split(",");

                double[] record = new double[6];
                record[0] = Double.parseDouble(tokens[0].trim());
                record[1] = Double.parseDouble(tokens[1].trim());
                record[2] = Double.parseDouble(tokens[2].trim());
                record[3] = Double.parseDouble(tokens[3].trim());
                record[4] = Double.parseDouble(tokens[4].trim());
                record[5] = Double.parseDouble(tokens[5].trim());
//				System.out.println(Arrays.toString(record));

                if(record[0] > -1000.0) {
                    rawData.add(record);
                }
            }

            if ("%TITLE%".equals(line)) {
                title = true;
                continue;
            } else {
                title = false;
            }

            if ("%RAW%".equals(line)) {
                raw = true;
            }
        }

        sc.close();

        Collections.reverse(rawData);

        double[] pressure = new double[rawData.size()];
        double[] height = new double[rawData.size()];
        double[] temperature = new double[rawData.size()];
        double[] dewpoint = new double[rawData.size()];
        double[] uWind = new double[rawData.size()];
        double[] vWind = new double[rawData.size()];

        for(int i = 0; i < rawData.size(); i++) {
            pressure[i] = rawData.get(i)[0] * 100.0;
            height[i] = rawData.get(i)[1];
            temperature[i] = rawData.get(i)[2] + 273.15;
            dewpoint[i] = rawData.get(i)[3] + 273.15;

            double wdir = rawData.get(i)[4] + 180.0;
            double wspd = rawData.get(i)[5];

            uWind[i] = wspd * Math.sin(Math.toRadians(wdir));
            vWind[i] = wspd * Math.cos(Math.toRadians(wdir));

            if(wspd < -9900) {
                if(i == 0) {
                    wdir = rawData.get(i + 1)[4] + 180.0;
                    wspd = rawData.get(i + 1)[5];

                    uWind[i] = wspd * Math.sin(Math.toRadians(wdir));
                    vWind[i] = wspd * Math.cos(Math.toRadians(wdir));
                } else if(i == rawData.size() - 1) {
                    uWind[i] = uWind[i - 1];
                    vWind[i] = vWind[i - 1];
                } else {
                    wdir = rawData.get(i + 1)[4] + 180.0;
                    wspd = rawData.get(i + 1)[5];

                    double uWind_ = wspd * Math.sin(Math.toRadians(wdir));
                    double vWind_ = wspd * Math.cos(Math.toRadians(wdir));

                    uWind[i] = (uWind[i - 1] + uWind_)/2;
                    vWind[i] = (vWind[i - 1] + vWind_)/2;
                }
            }
        }

        String station = rawTitle.substring(0, 8).trim();
        String timestamp = rawTitle.substring(8);

//		System.out.println(station);
//		System.out.println(timestamp);

        // ASSUMES YEAR IS IN THE 2000s
        // FOR DIFFERENT CENTURIES THIS CODE WILL NEED TO BE ADJUSTED
        final int CENTURY = 2000;

        int year = CENTURY + Integer.parseInt(timestamp.substring(0, 2));
        int month = Integer.parseInt(timestamp.substring(2, 4));
        int day = Integer.parseInt(timestamp.substring(4, 6));
        int hour = Integer.parseInt(timestamp.substring(7, 9));
        int minute = Integer.parseInt(timestamp.substring(9, 11));

        DateTime dt = new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);

        Sounding s = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);
        s.addMetadata("station", station);
        s.addMetadata("date-time", dt);

        return s;
    }
}
