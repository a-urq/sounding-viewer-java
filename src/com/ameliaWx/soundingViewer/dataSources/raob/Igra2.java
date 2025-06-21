package com.ameliaWx.soundingViewer.dataSources.raob;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.soundingViewer.unixTool.RadiosondeNotFoundException;
import com.ameliaWx.soundingViewer.unixTool.RadiosondeSite;
import com.ameliaWx.weatherUtils.WeatherUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static com.ameliaWx.soundingViewer.GlobalVars.dataFolder;
import static com.ameliaWx.soundingViewer.dataSources.FileTools.*;

public class Igra2 {
    public static int startOfCurrentData;
    public static boolean initialized = false;

    // created for use in optimizing RadarView performance
    public static void initialize() throws IOException {
        startOfCurrentData = determineStartOfCurrentData();
        initialized = true;
        int dummy = startOfCurrentData; // useless on its own, induces static init block. idk if there's a smarter or
        // less hacky way to do this
    }

    public static Object[] getSounding(RadiosondeSite site, DateTime d)
            throws IOException, RadiosondeNotFoundException {
        DateTime dOrig = d;

        Object[] soundingTextRet = getSoundingText(site, d); // { String, DateTime } a little gross but best i can think
        // of

        String soundingText = (String) soundingTextRet[0];
        d = (DateTime) soundingTextRet[1];

//		System.out.println(d);
//		System.out.println(soundingText);

        if (d == null) {
            throw new RadiosondeNotFoundException("Radiosonde " + site.getInternationalCode() + " " + dOrig
                    + " does not exist and no suitably close data could be found.");
        }

        String[] linesRaw = soundingText.split("\\n");

        ArrayList<String> lines = new ArrayList<String>();
        for (int i = 0; i < linesRaw.length; i++) {
            String line = linesRaw[linesRaw.length - 1 - i];

            String pressureStr = line.substring(9, 15);
//			String heightStr = line.substring(16, 21);
            String temperatureStr = line.substring(22, 27);
            String relativeHumidityStr = line.substring(28, 33);
            String windDirectionStr = line.substring(40, 45);
            String windSpeedStr = line.substring(46, 51);

            double pressure_ = Double.valueOf(pressureStr);
//			double height_ = Double.valueOf(heightStr);
            double temperature_ = Double.valueOf(temperatureStr) / 10.0 + 273.15;
            double relativeHumidity_ = Double.valueOf(relativeHumidityStr) / 1000.0;
            double windDirection_ = Double.valueOf(windDirectionStr) + 180.0;
            double windSpeed_ = Double.valueOf(windSpeedStr) / 10.0;

            if (temperature_ >= 130 && relativeHumidity_ < -1) {
                relativeHumidity_ = 0.001;
            }

            if (pressure_ < -10 || temperature_ < 130 || relativeHumidity_ < -1 || windDirection_ < -10
                    || windSpeed_ < -10) {
                continue;
            }

            lines.add(line);
        }

        double[] pressure = new double[lines.size()];
        double[] height = new double[lines.size()];
        double[] temperature = new double[lines.size()];
        double[] dewpoint = new double[lines.size()];
        double[] uWind = new double[lines.size()];
        double[] vWind = new double[lines.size()];

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
//			System.out.println(line);

            String pressureStr = line.substring(9, 15);
            String heightStr = line.substring(16, 21);
            String temperatureStr = line.substring(23, 27);
            String relativeHumidityStr = line.substring(28, 33);
            String dewpointDepressionStr = line.substring(34, 39);
            String windDirectionStr = line.substring(41, 45);
            String windSpeedStr = line.substring(47, 51);

            double pressure_ = Double.valueOf(pressureStr);
            double height_ = Double.valueOf(heightStr);
            double temperature_ = Double.valueOf(temperatureStr) / 10.0 + 273.15;
            double relativeHumidity_ = Double.valueOf(relativeHumidityStr) / 1000.0;
            double dewpointDepression_ = Double.valueOf(dewpointDepressionStr) / 10.0;
            double windDirection_ = Double.valueOf(windDirectionStr) + 180.0;
            double windSpeed_ = Double.valueOf(windSpeedStr) / 10.0;

//			if(temperature_ >= 130 && relativeHumidity_ < -1) {
////				System.out.println(relativeHumidity_);
//				relativeHumidity_ = 0.001;
//			}

            if (i == lines.size() - 1 && height_ < -1) {
                height_ = 0;
            }

            double dewpoint_ = 0.0;
            if (relativeHumidity_ > 0) {
                dewpoint_ = WeatherUtils.dewpoint(temperature_, relativeHumidity_);
            } else if (dewpointDepression_ > -100) {
                dewpoint_ = temperature_ - dewpointDepression_;
            } else {
                dewpoint_ = 0;
            }

            double uWind_ = Math.sin(Math.toRadians(windDirection_)) * windSpeed_;
            double vWind_ = Math.cos(Math.toRadians(windDirection_)) * windSpeed_;

            pressure[i] = pressure_;
            height[i] = height_;
            temperature[i] = temperature_;
            dewpoint[i] = dewpoint_;
            uWind[i] = uWind_;
            vWind[i] = vWind_;

//			System.out.printf("%6.1f\t%5.0f\t%5.1f\t%5.1f\t%4.1f\t%4.1f\n", pressure[i], height[i], temperature[i], dewpoint[i], uWind[i], vWind[i]);
        }

        // ONLY FOR TESTING
        // TAP OUT FOR CM1 SOUNDINGS
        // WILL MAKE A BETTER EXPORT FOR THIS LATER

        for (int i = lines.size() - 1; i >= 0; i--) {
            if (height[i] == -9999) {
                height[i] = height[i + 1] + WeatherUtils.heightAtPressure(pressure[i + 1], pressure[i],
                        (temperature[i] + temperature[i + 1]) / 2);
            }
        }

        Sounding soundingObj = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);

        return new Object[] { soundingObj, d };
    }

    private static Object[] getSoundingText(RadiosondeSite site, DateTime d) throws IOException {
        boolean archiveNeeded = (d.getYear() < startOfCurrentData);

        File soundingFileZip = null;

        if (archiveNeeded) {
            soundingFileZip = downloadFile(
                    "ftp://ftp.ncei.noaa.gov/pub/data/igra/data/data-por/" + site.getInternationalCode()
                            + "-data.txt.zip",
                    "sounding-" + site.getInternationalCode() + "-" + d.toString() + ".txt.zip");
        } else {
            soundingFileZip = downloadFile(
                    "https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/access/data-y2d/"
                            + site.getInternationalCode() + "-data-beg2021.txt.zip",
                    "sounding-" + site.getInternationalCode() + "-" + d.toString() + ".txt.zip");
        }

        readUsingZipFile(soundingFileZip.getAbsolutePath(),
                dataFolder + "sounding" + site.getInternationalCode() + "-" + d.toString() + "/");

        File soundingFile = new File(dataFolder + "sounding" + site.getInternationalCode() + "-"
                + d.toString() + "/" + site.getInternationalCode() + "-data.txt");

        soundingFileZip.delete();

        HashMap<DateTime, String> soundingTexts = new HashMap<>();
        Scanner sc = new Scanner(soundingFile);

        String line = "";
        while (sc.hasNextLine()) {
            if (line.length() == 0) {
                line = sc.nextLine();
            }

            if ('#' == line.charAt(0)) {
                int year = Integer.valueOf(line.substring(13, 17));
                int month = Integer.valueOf(line.substring(18, 20));
                int day = Integer.valueOf(line.substring(21, 23));
                int hour = Integer.valueOf(line.substring(24, 26));

                if (hour < 0 || hour > 24)
                    hour = 0;
                if (day < 1 || day > 31)
                    day = 1;
                if (month < 1 || month > 12)
                    hour = 1;
                if (year < 1900)
                    year = 1900;

                DateTime testD = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

                if (Math.abs(testD.getMillis() - d.getMillis()) < 86400000 * 7) {
                    String soundingText = "";

                    while (sc.hasNextLine()) {
                        line = sc.nextLine();

                        if ('#' == line.charAt(0)) {
                            break;
                        }

                        soundingText += line + "\n";
                    }

                    if (testD.getMillis() - d.getMillis() == 0) {
                        sc.close();
                        soundingFile.delete();
                        new File(dataFolder + "sounding" + site.getInternationalCode() + "-"
                                + d.toString() + "/").delete();

                        return new Object[] { soundingText, testD };
                    }

                    soundingTexts.put(testD, soundingText);
                } else {
                    while (sc.hasNextLine()) {
                        line = sc.nextLine();

                        if ('#' == line.charAt(0)) {
                            break;
                        }
                    }
                }
            }
        }

        sc.close();
        soundingFile.delete();
        System.out.println(
                dataFolder + "sounding" + site.getInternationalCode() + "-" + d.toString() + "/");
        new File(dataFolder + "sounding" + site.getInternationalCode() + "-" + d.toString() + "/")
                .delete();

        DateTime closestD = null;
        long closestMillis = Long.MAX_VALUE;

        for (DateTime testD : soundingTexts.keySet()) {
//			System.out.println(d + " " + testD + " " + closestD + " " + Math.abs(testD.getMillis() - d.getMillis()) + " " + closestMillis);
            if (Math.abs(testD.getMillis() - d.getMillis()) < closestMillis) {
                closestMillis = Math.abs(testD.getMillis() - d.getMillis());
                closestD = testD;
            }
        }

        return new Object[] { soundingTexts.get(closestD), closestD };
    }

    private static int determineStartOfCurrentData() throws IOException {
        List<String> archiveFolder = listFilesInWebFolder(
                "https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/access/data-y2d/", 2);

//        return Integer.parseInt(archiveFolder.get(0).substring(20, 24));
        return 2021;
    }
}
