package com.ameliaWx.soundingViewer;

import java.util.HashMap;
import java.util.Set;

import com.ameliaWx.weatherUtils.WeatherUtils;

public class Sounding {
	private double[] pressureLevels;
	private double[] temperature;
	private double[] wetbulb;
	private double[] dewpoint;
	private double[] frostPoint;
	private double[] height;
	private double[] uWind;
	private double[] vWind;
	private double[] wWind;
	
	private HashMap<String, Object> metadata;

	public Sounding(double[] pressureLevels, double[] temperature, double[] dewpoint, double[] height,
			double[] uWind, double[] vWind) {
		this(pressureLevels, temperature, dewpoint, height, uWind, vWind, new double[0]);
	}
	
	public Sounding(double[] pressureLevels, double[] temperature, double[] dewpoint, double[] height,
			double[] uWind, double[] vWind, double[] wWind) {
//		System.out.println("entering double[] constructor");
		this.metadata = new HashMap<>();
		
		this.pressureLevels = pressureLevels;
		this.temperature = temperature;
		this.dewpoint = dewpoint;
		this.height = height;
		this.uWind = uWind;
		this.vWind = vWind;
		this.wWind = wWind;
		
		wetbulb = new double[dewpoint.length];
		
//		System.out.println("wetbulb.length: " + wetbulb.length);
		for(int i = 0; i < wetbulb.length; i++) {
//			System.out.printf("%10.0f%10.2f%10.2f%10.2f%10.2f%10.2f", pressureLevels[i], height[i], temperature[i], dewpoint[i], uWind[i], vWind[i]);
			
			wetbulb[i] = WeatherUtils.wetBulbTemperature(temperature[i], dewpoint[i], pressureLevels[i]);
		}
		
		frostPoint = new double[dewpoint.length];

		for(int i = 0; i < frostPoint.length; i++) {
			frostPoint[i] = WeatherUtils.frostPointFromDewpoint(dewpoint[i]);
//			System.out.println("assigning frost points");
		}
	}
	
	public Sounding(float[] pressureLevels, float[] temperature, float[] dewpoint, float[] height,
			float[] uWind, float[] vWind) {
		this(pressureLevels, temperature, dewpoint, height, uWind, vWind, new float[0]);
	}
	
	public Sounding(float[] pressureLevels, float[] temperature, float[] dewpoint, float[] height,
			float[] uWind, float[] vWind, float[] wWind) {
		this.metadata = new HashMap<>();
		
		this.pressureLevels = convFloatToDouble(pressureLevels);
		this.temperature = convFloatToDouble(temperature);
		this.dewpoint = convFloatToDouble(dewpoint);
		this.height = convFloatToDouble(height);
		this.uWind = convFloatToDouble(uWind);
		this.vWind = convFloatToDouble(vWind);
		this.wWind = convFloatToDouble(wWind);
		
		wetbulb = new double[dewpoint.length];
		
		for(int i = 0; i < wetbulb.length; i++) {
			wetbulb[i] = WeatherUtils.wetBulbTemperature(temperature[i], dewpoint[i], pressureLevels[i]);
		}
		
		frostPoint = new double[dewpoint.length];

		for(int i = 0; i < frostPoint.length; i++) {
			frostPoint[i] = WeatherUtils.frostPointFromDewpoint(dewpoint[i]);
//			System.out.println("assigning frost points");
		}
	}
	
	private double[] convFloatToDouble(float[] arr) {
		double[] ret = new double[arr.length];
		
		for(int i = 0; i < ret.length; i++) {
			ret[i] = (double) arr[i];
		}
		
		return ret;
	}
	
	public double[] getPressureLevels() {
		return pressureLevels;
	}

	public double[] getTemperature() {
		return temperature;
	}

	public double[] getWetbulb() {
		return wetbulb;
	}

	public double[] getDewpoint() {
		return dewpoint;
	}

	public double[] getFrostPoint() {
		return frostPoint;
	}

	public double[] getHeight() {
		return height;
	}

	public double[] getUWind() {
		return uWind;
	}

	public double[] getVWind() {
		return vWind;
	}

	public double[] getWWind() {
		return wWind;
	}
	
	public void addMetadata(String key, Object object) {
		metadata.put(key, object);
	}
	
	public Object getMetadata(String key) {
		return metadata.get(key);
	}
	
	public void removeMetadata(String key) {
		metadata.remove(key);
	}
	
	public Set<String> listMetadata() {
		return metadata.keySet();
	}
}
