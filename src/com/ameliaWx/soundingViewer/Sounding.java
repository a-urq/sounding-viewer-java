package com.ameliaWx.soundingViewer;

import com.ameliaWx.weatherUtils.WeatherUtils;

public class Sounding {
	private double[] pressureLevels;
	private double[] temperature;
	private double[] wetbulb;
	private double[] dewpoint;
	private double[] height;
	private double[] uWind;
	private double[] vWind;
	
	public Sounding(double[] pressureLevels, double[] temperature, double[] wetbulb, double[] dewpoint, double[] height,
			double[] uWind, double[] vWind) {
		this.pressureLevels = pressureLevels;
		this.temperature = temperature;
		this.dewpoint = dewpoint;
		this.height = height;
		this.uWind = uWind;
		this.vWind = vWind;
		
		wetbulb = new double[dewpoint.length];
		
		for(int i = 0; i < wetbulb.length; i++) {
			wetbulb[i] = WeatherUtils.wetBulbTemperature(temperature[i], dewpoint[i], pressureLevels[i]);
		}
	}
	
	public Sounding(double[] pressureLevels, double[] temperature, double[] dewpoint, double[] height,
			double[] uWind, double[] vWind) {
		this.pressureLevels = pressureLevels;
		this.temperature = temperature;
		this.dewpoint = dewpoint;
		this.height = height;
		this.uWind = uWind;
		this.vWind = vWind;
		
		wetbulb = new double[dewpoint.length];
		
		for(int i = 0; i < wetbulb.length; i++) {
			wetbulb[i] = WeatherUtils.wetBulbTemperature(temperature[i], dewpoint[i], pressureLevels[i]);
		}
	}
	
	public Sounding(float[] pressureLevels, float[] temperature, float[] wetbulb, float[] dewpoint, float[] height,
			float[] uWind, float[] vWind) {
		this.pressureLevels = convFloatToDouble(pressureLevels);
		this.temperature = convFloatToDouble(temperature);
		this.wetbulb = convFloatToDouble(wetbulb);
		this.dewpoint = convFloatToDouble(dewpoint);
		this.height = convFloatToDouble(height);
		this.uWind = convFloatToDouble(uWind);
		this.vWind = convFloatToDouble(vWind);
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

	public double[] getHeight() {
		return height;
	}

	public double[] getUWind() {
		return uWind;
	}

	public double[] getVWind() {
		return vWind;
	}
}
