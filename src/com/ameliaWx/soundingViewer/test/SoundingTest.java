package com.ameliaWx.soundingViewer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.weatherUtils.WeatherUtils;

public class SoundingTest {
	public static void main(String[] args) throws FileNotFoundException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		File sounding = new File("src/com/ameliaWx/soundingViewer/test/soundingData_2023022703-OUN.txt");
		Scanner sc = new Scanner(sounding);
		
		ArrayList<String> lines = new ArrayList<>();
		
		sc.nextLine();
		while(sc.hasNextLine()) {
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
		
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			
			String pressureStr = line.substring(10, 15);
			String heightStr = line.substring(16, 21);
			String temperatureStr = line.substring(23, 27);
			String relativeHumidityStr = line.substring(29, 33);
			String windDirectionStr = line.substring(41, 45);
			String windSpeedStr = line.substring(47, 51);
			
			double pressure_ = Double.valueOf(pressureStr);
			double height_ = Double.valueOf(heightStr);
			double temperature_ = Double.valueOf(temperatureStr)/10.0 + 273.15;
			double relativeHumidity_ = Double.valueOf(relativeHumidityStr)/1000.0;
			double windDirection_ = Double.valueOf(windDirectionStr) + 180.0;
			double windSpeed_ = Double.valueOf(windSpeedStr)/10.0;
			
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
		DateTime time = new DateTime(2023, 2, 27, 3, 0, DateTimeZone.UTC);
		
		new SoundingFrame("Norman OK Weather Balloon", soundingObj, time, 35.1808, -97.4378);
	}
}
