package com.ameliaWx.soundingViewer;

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

	public Sounding(double[] pressureLevels, double[] temperature, double[] dewpoint, double[] height,
			double[] uWind, double[] vWind) {
		this(pressureLevels, temperature, dewpoint, height, uWind, vWind, new double[0]);
	}
	
	public Sounding(double[] pressureLevels, double[] temperature, double[] dewpoint, double[] height,
			double[] uWind, double[] vWind, double[] wWind) {
//		System.out.println("entering constructor");
		this.pressureLevels = pressureLevels;
		this.temperature = temperature;
		this.dewpoint = dewpoint;
		this.height = height;
		this.uWind = uWind;
		this.vWind = vWind;
		this.wWind = wWind;
		
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
	
	public Sounding(float[] pressureLevels, float[] temperature, float[] dewpoint, float[] height,
			float[] uWind, float[] vWind) {
		this(pressureLevels, temperature, dewpoint, height, uWind, vWind, new float[0]);
	}
	
	public Sounding(float[] pressureLevels, float[] temperature, float[] dewpoint, float[] height,
			float[] uWind, float[] vWind, float[] wWind) {
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
}
