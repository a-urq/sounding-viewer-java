package com.ameliaWx.soundingViewer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.soundingViewer.AcarsStation;
import com.ameliaWx.soundingViewer.AcarsStations;
import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.weatherUtils.RecordAtLevel;
import com.ameliaWx.weatherUtils.WeatherUtils;

public class SoundingTest {
	public static void main(String[] args) throws FileNotFoundException {
		testSounding();
	}

	private static void testSounding() throws FileNotFoundException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

//		File sounding = new File("src/com/ameliaWx/soundingViewer/test/soundingData_2023022703-OUN.txt");
//		File sounding = new File("src/com/ameliaWx/soundingViewer/test/soundingData_2023041919-OUN.txt");
		File sounding = new File("src/com/ameliaWx/soundingViewer/test/soundingData_2013053118-OUN.txt");
		Scanner sc = new Scanner(sounding);

		ArrayList<String> lines = new ArrayList<>();

		sc.nextLine();
		while (sc.hasNextLine()) {
			lines.add(sc.nextLine());
		}

		sc.close();

		Collections.reverse(lines);

		double[] pressure = new double[lines.size()];
		double[] height = new double[lines.size()];
		double[] temperature = new double[lines.size()];
		double[] dewpoint = new double[lines.size()];
		double[] uWind = new double[lines.size()];
		double[] vWind = new double[lines.size()];

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			String pressureStr = line.substring(10, 15);
			String heightStr = line.substring(16, 21);
			String temperatureStr = line.substring(23, 27);
			String relativeHumidityStr = line.substring(29, 33);
			String windDirectionStr = line.substring(41, 45);
			String windSpeedStr = line.substring(47, 51);

			double pressure_ = Double.valueOf(pressureStr);
			double height_ = Double.valueOf(heightStr);
			double temperature_ = Double.valueOf(temperatureStr) / 10.0 + 273.15;
			double relativeHumidity_ = Double.valueOf(relativeHumidityStr) / 1000.0;
			double windDirection_ = Double.valueOf(windDirectionStr) + 180.0;
			double windSpeed_ = Double.valueOf(windSpeedStr) / 10.0;

			double dewpoint_ = WeatherUtils.dewpoint(temperature_, relativeHumidity_);

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

		Sounding soundingObj = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);
		DateTime time = new DateTime(2013, 5, 31, 18, 0, DateTimeZone.UTC);

		new SoundingFrame("Norman OK Weather Balloon", soundingObj, time, 35.1808, -97.4378).setVisible(true);
	}

	private static void testAcars() {
		File acars = new File("src/com/ameliaWx/soundingViewer/test/ACARS_20231214_ABQ_1030.txt");
		
		Sounding s = null;
		try {
			s = readAcarsFile(acars);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		AcarsStation stat = AcarsStations.acarsStationFromCode((String) s.getMetadata("station"));
		new SoundingFrame(stat.location + " ACARS", s, (DateTime) s.getMetadata("date-time"), stat.lat, stat.lon).setVisible(true);
	}

	private static Sounding readAcarsFile(File f) throws FileNotFoundException {
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
				record[0] = Double.valueOf(tokens[0].trim());
				record[1] = Double.valueOf(tokens[1].trim());
				record[2] = Double.valueOf(tokens[2].trim());
				record[3] = Double.valueOf(tokens[3].trim());
				record[4] = Double.valueOf(tokens[4].trim());
				record[5] = Double.valueOf(tokens[5].trim());
				System.out.println(Arrays.toString(record));
				
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
		
		// ASSUMES YEAR IS IN THE 2000s
		// FOR DIFFERENT CENTURIES THIS CODE WILL NEED TO BE ADJUSTED
		final int CENTURY = 2000;
		
		int year = CENTURY + Integer.valueOf(timestamp.substring(0, 2));
		int month = Integer.valueOf(timestamp.substring(2, 4));
		int day = Integer.valueOf(timestamp.substring(4, 6));
		int hour = Integer.valueOf(timestamp.substring(7, 9));
		int minute = Integer.valueOf(timestamp.substring(9, 11));
		
		DateTime dt = new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);
		
		Sounding s = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);
		s.addMetadata("station", station);
		s.addMetadata("date-time", dt);
		
		return s;
	}
}
