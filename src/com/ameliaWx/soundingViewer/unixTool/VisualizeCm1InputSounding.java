package com.ameliaWx.soundingViewer.unixTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.weatherUtils.WeatherUtils;

public class VisualizeCm1InputSounding {
	public static void main(String[] args) throws FileNotFoundException {
		String pathToInputSounding = "";
		if(args.length > 0) {
			pathToInputSounding = args[0];
		} else {
			pathToInputSounding = "/media/nvme1/CM1/cm1r21.0/run/input_sounding";
		}
		
		Scanner sc = new Scanner(new File(pathToInputSounding));
		
		String firstLine = sc.nextLine();
		
		double surfacePressure = Double.valueOf(firstLine.substring(0, 12));
		double surfaceTheta = Double.valueOf(firstLine.substring(16, 25));
		double surfaceMixingRatio = Double.valueOf(firstLine.substring(29, 36));
		
		System.out.println(firstLine);
		System.out.println(surfaceTheta);
		System.out.println(surfaceMixingRatio);
		System.out.println(surfacePressure);
		
		ArrayList<double[]> lineData = new ArrayList<>();
		
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			
			double height = Double.valueOf(line.substring(0, 14));
			double theta = Double.valueOf(line.substring(14, 27));
			double mixingRatio = Double.valueOf(line.substring(27, 39));
			double uWind = Double.valueOf(line.substring(39, 49));
			double vWind = Double.valueOf(line.substring(49));
			
			lineData.add(new double[] {height, theta, mixingRatio, uWind, vWind});
		}
		
		sc.close();
		
		double[] pressure = new double[lineData.size() + 1];
		double[] height = new double[lineData.size() + 1];
		double[] temperature = new double[lineData.size() + 1];
		double[] dewpoint = new double[lineData.size() + 1];
		double[] uWind = new double[lineData.size() + 1];
		double[] vWind = new double[lineData.size() + 1];
		
		pressure[pressure.length - 1] = surfacePressure * 100;
		height[height.length - 1] = 0;
		temperature[temperature.length - 1] = WeatherUtils.temperatureFromPotentialTemperature(surfacePressure * 100, surfaceTheta);
		dewpoint[dewpoint.length - 1] = WeatherUtils.dewpointFromMixingRatio(surfacePressure * 100, surfaceMixingRatio/1000);
		uWind[uWind.length - 1] = 0;
		vWind[vWind.length - 1] = 0;
		
		double previousHeight = 0;
		for(int i = 0; i < lineData.size(); i++) {
			double[] data = lineData.get(i);
			
			double currentHeight = data[0];
			double theta = data[1];
			double mixingRatio = data[2];
			double ugrd = data[3];
			double vgrd = data[4];
			
			double dz = currentHeight - previousHeight;

			System.out.println(pressure[pressure.length - 1 - i]);
			System.out.println(currentHeight);
			System.out.println(previousHeight);
			System.out.println(temperature[temperature.length - 1 - i]);
			System.out.println();
			double pressureAtCurrentHeight = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1 - i], dz, temperature[temperature.length - 1 - i]);
			
			pressure[pressure.length - 2 - i] = pressureAtCurrentHeight;
			height[height.length - 2 - i] = currentHeight;
			temperature[temperature.length - 2 - i] = WeatherUtils.temperatureFromPotentialTemperature(pressureAtCurrentHeight, theta);
			dewpoint[dewpoint.length - 2 - i] = WeatherUtils.dewpointFromMixingRatio(pressureAtCurrentHeight, mixingRatio/1000);
			uWind[uWind.length - 2 - i] = ugrd;
			vWind[vWind.length - 2 - i] = vgrd;
			
			previousHeight = currentHeight;
		}
		
		uWind[uWind.length - 1] = uWind[uWind.length - 2];
		vWind[vWind.length - 1] = vWind[vWind.length - 2];
		
		System.out.println(Arrays.toString(pressure));
		System.out.println(Arrays.toString(height));
		System.out.println(Arrays.toString(temperature));
		System.out.println(Arrays.toString(dewpoint));
		System.out.println(Arrays.toString(uWind));
		System.out.println(Arrays.toString(vWind));
		
		Sounding sounding = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);
		SoundingFrame sf = new SoundingFrame("Idealized Maximum 30C CAPE Bomb", sounding, DateTime.now(DateTimeZone.UTC), 33,
				-96.5);
		
		sf.setVisible(true);
	}
}
