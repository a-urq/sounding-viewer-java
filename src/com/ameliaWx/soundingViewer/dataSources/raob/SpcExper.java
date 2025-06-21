package com.ameliaWx.soundingViewer.dataSources.raob;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.unixTool.RadiosondeSite;
import com.ameliaWx.weatherUtils.WeatherUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static com.ameliaWx.soundingViewer.dataSources.FileTools.*;

public class SpcExper {
    public static Object[] getSounding(RadiosondeSite site) throws IOException {
        Object[] soundingTextRet = getSoundingText(site); // { String, DateTime } a little gross but best i can
        // think of

        String soundingText = (String) soundingTextRet[0];
        DateTime d = (DateTime) soundingTextRet[1];

//		System.out.println(d);
//		System.out.println(soundingText);

        String[] linesRaw = soundingText.split("\\n");

        ArrayList<String> lines = new ArrayList<String>();
        for (int i = 0; i < linesRaw.length; i++) {
            String line = linesRaw[linesRaw.length - 1 - i];
//			System.out.println(line);

//			String pressureStr = line.substring(0, 8);
//			String heightStr = line.substring(9, 19);
            String temperatureStr = line.substring(20, 30);
            String relativeHumidityStr = line.substring(31, 41);
            String windDirectionStr = line.substring(42, 52);
//			String windSpeedStr = line.substring(53, 63);

//			double pressure_ = Double.valueOf(pressureStr) * 100;
//			double height_ = Double.valueOf(heightStr);
            double temperature_ = Double.parseDouble(temperatureStr) / 10.0 + 273.15;
            double relativeHumidity_ = Double.parseDouble(relativeHumidityStr)/1000.0;
            double windDirection_ = Double.parseDouble(windDirectionStr) + 180.0;
//			double windSpeed_ = Double.valueOf(windSpeedStr)/10.0;

            if (temperature_ < 130 && windDirection_ < -10) {
                continue;
            }

            if (relativeHumidity_ < -9) {
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

        ArrayList<Integer> indicesWithWindData = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
//			System.out.println(line);

            String pressureStr = line.substring(0, 8);
            String heightStr = line.substring(9, 19);
            String temperatureStr = line.substring(20, 30);
            String relativeHumidityStr = line.substring(31, 41);
            String windDirectionStr = line.substring(42, 52);
            String windSpeedStr = line.substring(53, 63);

            double pressure_ = Double.parseDouble(pressureStr) * 100;
            double height_ = Double.parseDouble(heightStr);
            double temperature_ = Double.parseDouble(temperatureStr) + 273.15;
            double dewpoint_ = Double.parseDouble(relativeHumidityStr) + 273.15;
            double windDirection_ = Double.parseDouble(windDirectionStr) + 180.0;
            double windSpeed_ = Double.parseDouble(windSpeedStr) * 0.5144444;

            double uWind_ = Math.sin(Math.toRadians(windDirection_)) * windSpeed_;
            double vWind_ = Math.cos(Math.toRadians(windDirection_)) * windSpeed_;

            pressure[i] = pressure_;
            height[i] = height_;
            temperature[i] = temperature_;
            dewpoint[i] = dewpoint_;
            uWind[i] = uWind_;
            vWind[i] = vWind_;

            if(dewpoint[i] < -9900) dewpoint[i] = 0.01;

            if (windSpeed_ > -10) {
                indicesWithWindData.add(i);
            }

//			System.out.printf("%6.1f\t%5.0f\t%5.1f\t%5.1f\t%4.1f\t%4.1f\n", pressure[i], height[i], temperature[i],
//					dewpoint[i], uWind[i], vWind[i]);
        }

        for (int i = 0; i < uWind.length; i++) {
            if (Math.hypot(uWind[i], vWind[i]) > 5000) {
                int indBefore = -1;
                int indAfter = -1;

                for (int j = 0; j < indicesWithWindData.size(); j++) {
                    if (indicesWithWindData.get(j) < i) {
                        indBefore = indicesWithWindData.get(j);
                    }
                }

                for (int j = indicesWithWindData.size() - 1; j >= 0; j--) {
                    if (indicesWithWindData.get(j) > i) {
                        indAfter = indicesWithWindData.get(j);
                    }
                }

                if (indBefore == -1) {
                    uWind[i] = uWind[indAfter];
                    vWind[i] = vWind[indAfter];
                } else if (indAfter == -1) {
                    uWind[i] = uWind[indBefore];
                    vWind[i] = vWind[indBefore];
                } else {
                    double weight1 = (double) (indAfter - i) / (indAfter - indBefore);
                    double weight2 = (double) (i - indBefore) / (indAfter - indBefore);

                    uWind[i] = weight1 * uWind[indBefore] + weight2 * uWind[indAfter];
                    vWind[i] = weight1 * vWind[indBefore] + weight2 * vWind[indAfter];
                }
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            if (height[i] == -8888) {
                height[i] = height[i - 1] + WeatherUtils.heightAtPressure(pressure[i - 1], pressure[i],
                        (temperature[i] + temperature[i - 1]) / 2);
            }
        }

        Sounding soundingObj = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);

        return new Object[] { soundingObj, d };
    }

    private static Object[] getSoundingText(RadiosondeSite site) throws IOException {
        DateTime d = DateTime.now(DateTimeZone.UTC);

        d = d.minusMinutes(d.getMinuteOfHour()).minusHours(0);

        File soundingFile = null;

        boolean bidailyPresent = false;
        try {
            DateTime dd = d.minusHours(d.getHourOfDay() % 12);

            String spcExperFile = String.format("https://www.spc.noaa.gov/exper/soundings/%02d%02d%02d%02d_OBS/%s.txt",
                    dd.getYearOfCentury(), dd.getMonthOfYear(), dd.getDayOfMonth(), dd.getHourOfDay(),
                    site.getFourLetterCode().substring(1));
            soundingFile = downloadFile(spcExperFile, "sounding-" + site.getFourLetterCode() + ".txt");
            bidailyPresent = true;

            d = dd;
        } catch (FileNotFoundException e) {
            try {
                DateTime dd = d.minusHours(d.getHourOfDay() % 12).minusHours(12);

                String spcExperFile = String.format(
                        "https://www.spc.noaa.gov/exper/soundings/%02d%02d%02d%02d_OBS/%s.txt", dd.getYearOfCentury(),
                        dd.getMonthOfYear(), dd.getDayOfMonth(), dd.getHourOfDay(),
                        site.getFourLetterCode().substring(1));
                soundingFile = downloadFile(spcExperFile, "sounding-" + site.getFourLetterCode() + ".txt");
                bidailyPresent = true;

                d = dd;
            } catch (FileNotFoundException e1) {
                d = d.minusHours(1);
                System.out.println(d);
            }
        }

        if (!bidailyPresent) {
            for (int i = 0; i < 72; i++) {
                try {
                    String spcExperFile = String.format(
                            "https://www.spc.noaa.gov/exper/soundings/%02d%02d%02d%02d_OBS/%s.txt",
                            d.getYearOfCentury(), d.getMonthOfYear(), d.getDayOfMonth(), d.getHourOfDay(),
                            site.getFourLetterCode().substring(1));
                    soundingFile = downloadFile(spcExperFile, "sounding-" + site.getFourLetterCode() + ".txt");
                } catch (FileNotFoundException e) {
                    d = d.minusHours(1);
                    System.out.println(d);
                }
            }
        }
//		System.out.println(spcExperFile);

        HashMap<DateTime, String> soundingTexts = new HashMap<>();
        assert soundingFile != null;
        Scanner sc = new Scanner(soundingFile);

        String soundingText = "";
        boolean soundingData = false;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            if (line.contains("%END%")) {
                soundingData = false;
            }

            if (soundingData) {
                soundingText += line + "\n";
                soundingTexts.put(d, soundingText);
            }

            if (line.contains("%RAW%")) {
                soundingData = true;
            }
        }

        sc.close();
        soundingFile.delete();

        return new Object[] { soundingText, d };
    }
}
