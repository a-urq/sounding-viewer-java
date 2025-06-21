package com.ameliaWx.soundingViewer.unixTool.nwp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.weatherUtils.WeatherUtils;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import static com.ameliaWx.soundingViewer.GlobalVars.dataFolder;

public class ModelDerived {
	private static final String gfsUrlPrefix = "https://nomads.ncep.noaa.gov/cgi-bin/filter_gfs_0p25_1hr.pl?dir=%2Fgfs.";
	private static final String gfsUrlDateTemplate = "%04d%02d%02d";
	private static final String gfsUrlInfix = "%2F";
	private static final String gfsUrlInitTimeTemplate = "%02d";
	private static final String gfsUrlInfix2 = "%2F";
	private static final String gfsUrlForecastTemplate = "atmos&file=gfs.t%02dz.pgrb2.0p25.f%03d&var_HGT=on&var_PRES=on&var_RH=on&var_TMP=on&var_UGRD=on&var_VGRD=on&var_VVEL=on&lev_2_m_above_ground=on&lev_10_m_above_ground=on&lev_1000_mb=on&lev_975_mb=on&lev_950_mb=on&lev_925_mb=on&lev_900_mb=on&lev_850_mb=on&lev_800_mb=on&lev_750_mb=on&lev_700_mb=on&lev_650_mb=on&lev_600_mb=on&lev_550_mb=on&lev_500_mb=on&lev_450_mb=on&lev_400_mb=on&lev_350_mb=on&lev_300_mb=on&lev_250_mb=on&lev_200_mb=on&lev_150_mb=on&lev_100_mb=on&lev_70_mb=on&lev_50_mb=on&lev_40_mb=on&lev_30_mb=on&lev_20_mb=on&lev_15_mb=on&lev_10_mb=on&lev_7_mb=on&lev_5_mb=on&lev_3_mb=on&lev_2_mb=on&lev_1_mb=on&lev_0.7_mb=on&lev_0.4_mb=on&lev_0.2_mb=on&lev_0.1_mb=on&lev_0.07_mb=on&lev_0.04_mb=on&lev_0.02_mb=on&lev_0.01_mb=on&lev_surface=on&subregion=&toplat=%3.3f&leftlon=%3.3f&rightlon=%3.3f&bottomlat=%3.3f";

	public static void main(String[] args) throws IOException {
		new SoundingFrame("GFS-derived Radiosonde", getGfsSounding(40.25, -89.75, new DateTime(2024, 2, 22, 18, 0, DateTimeZone.UTC), 126), new DateTime(2024, 2, 22, 18, 0, DateTimeZone.UTC).plusHours(126), 40.25, -89.75).setVisible(true);
		
//		try {
//			getGfsSounding(33, -96.5, new DateTime(2023, 11, 19, 18, 0, DateTimeZone.UTC), 0);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public static Sounding getGfsSounding(double lat, double lon) {
		DateTime now = DateTime.now(DateTimeZone.UTC);
		System.out.println("before aligning: " + now);
		now = now.minusMinutes(now.getMinuteOfHour());
		now = now.minusHours(now.getHourOfDay() % 6);
		System.out.println("after aligning: " + now);
		
		Sounding gfsSounding = null;
		for(int i = 0; i < 3; i++) {
			try {
				gfsSounding = getGfsSounding(lat, lon, now, 0);
				break;
			} catch (IOException e) {
				System.err.println("GFS not found for " + now + ", rolling back 6 hours");
				now = now.minusHours(6);
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				continue;
			}
		}
		
		return gfsSounding;
	}
	
	public static Sounding getGfsSounding(double lat, double lon, DateTime initTime, int fh) throws IOException {
		String gfsUrlDate = String.format(gfsUrlDateTemplate, initTime.getYear(), initTime.getMonthOfYear(),
				initTime.getDayOfMonth());
		String gfsUrlInitTime = String.format(gfsUrlInitTimeTemplate, initTime.getHourOfDay());
		String gfsUrlForecast = String.format(gfsUrlForecastTemplate, initTime.getHourOfDay(), fh, lat + 0.125,
				lon - 0.125, lon + 0.125, lat - 0.125);

		String gfsUrl = gfsUrlPrefix + gfsUrlDate + gfsUrlInfix + gfsUrlInitTime + gfsUrlInfix2 + gfsUrlForecast;

		System.out.println(gfsUrl);

		File gfsFile = downloadFile(gfsUrl, "gfsTemp.grib2");

		@SuppressWarnings("deprecation")
		NetcdfFile gfs = NetcdfFile.open(gfsFile.getAbsolutePath());

//		System.out.println(gfs);

		float[][][][] tmpIsobaric = readVariable4Dim(gfs.findVariable("Temperature_isobaric"));
		float[][][][] rhIsobaric = readVariable4Dim(gfs.findVariable("Relative_humidity_isobaric"));
		float[][][][] hgtIsobaric = readVariable4Dim(gfs.findVariable("Geopotential_height_isobaric"));
		float[][][][] uwndIsobaric = readVariable4Dim(gfs.findVariable("u-component_of_wind_isobaric"));
		float[][][][] vwndIsobaric = readVariable4Dim(gfs.findVariable("v-component_of_wind_isobaric"));
		float[][][][] vvelIsobaric = readVariable4Dim(gfs.findVariable("Vertical_velocity_pressure_isobaric"));

		float[][][] tmpSurface = readVariable3Dim(gfs.findVariable("Temperature_isobaric"));
		float[][][] rhSurface = readVariable3Dim(gfs.findVariable("Relative_humidity_height_above_ground"));
		float[][][] presSurface = readVariable3Dim(gfs.findVariable("Pressure_surface"));
		float[][][] uwndSurface = readVariable3Dim(gfs.findVariable("u-component_of_wind_height_above_ground"));
		float[][][] vwndSurface = readVariable3Dim(gfs.findVariable("v-component_of_wind_height_above_ground"));

		float[] pressure = { 1, 2, 4, 7, 10, 20, 40, 70, 100, 200, 300, 500, 700, 1000, 1500, 2000, 3000, 4000, 5000,
				7000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000, 55000, 60000, 65000, 70000, 75000,
				80000, 85000, 90000, 92500, 95000, 97500, 100000, presSurface[0][0][0], };
		
		float[] temperature = new float[tmpIsobaric[0].length + 1];
		float[] dewpoint = new float[rhIsobaric[0].length + 1];
		float[] height = new float[hgtIsobaric[0].length + 1];
		float[] uWind = new float[uwndIsobaric[0].length + 1];
		float[] vWind = new float[vwndIsobaric[0].length + 1];
		float[] vertVel = new float[vvelIsobaric[0].length];
		
//		System.out.println(vvelIsobaric.length);
//		System.out.println(vvelIsobaric[0].length);
//		System.out.println(vvelIsobaric[0][0].length);
//		System.out.println(vvelIsobaric[0][0][0].length);
		
		for(int i = 0; i < tmpIsobaric[0].length; i++) {
			temperature[i] = tmpIsobaric[0][i][0][0];
			dewpoint[i] = (float) WeatherUtils.dewpoint(tmpIsobaric[0][i][0][0], rhIsobaric[0][i][0][0]/100.0);
			height[i] = hgtIsobaric[0][i][0][0];
			uWind[i] = uwndIsobaric[0][i][0][0];
			vWind[i] = vwndIsobaric[0][i][0][0];
			vertVel[i] = vvelIsobaric[0][i][0][0];
		}
		
		temperature[temperature.length - 1] = tmpSurface[0][0][0];
		dewpoint[dewpoint.length - 1] = (float) WeatherUtils.dewpoint(tmpSurface[0][0][0], rhSurface[0][0][0]/100.0);
		height[temperature.length - 1] = 0;
		uWind[uWind.length - 1] = uwndSurface[0][0][0];
		vWind[vWind.length - 1] = vwndSurface[0][0][0];

		for(int i = 0; i < tmpIsobaric[0].length; i++) {
			if (pressure[i] > pressure[pressure.length - 1]) {
				pressure[i] = pressure[pressure.length - 1];
				temperature[i] = temperature[temperature.length - 1];
				dewpoint[i] = dewpoint[dewpoint.length - 1];
				height[i] = height[height.length - 1];
				uWind[i] = uWind[uWind.length - 1];
				vWind[i] = vWind[vWind.length - 1];
				vertVel[i] = 0.0f;
			}
		}
		
		Sounding gfsSounding = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind, vertVel);

		return gfsSounding;
	}

	private static float[][][] readVariable3Dim(Variable rawData) {
		int[] shape = rawData.getShape();
		Array _data = null;

		try {
			_data = rawData.read();
		} catch (IOException e) {
			e.printStackTrace();
			return new float[shape[0]][shape[1]][shape[2]];
		}

		float[][][] data = new float[shape[0]][shape[1]][shape[2]];
		for (int i = 0; i < _data.getSize(); i++) {
			int x = i % shape[2];
			int y = (i / shape[2]) % shape[1];
			int t = (i / (shape[2] * shape[1])) % shape[0];

			float record = _data.getFloat(i);

			data[t][shape[1] - 1 - y][x] = record;
		}

		return data;
	}

	private static float[][][][] readVariable4Dim(Variable rawData) {
		int[] shape = rawData.getShape();
		Array _data = null;

		try {
			_data = rawData.read();
		} catch (IOException e) {
			e.printStackTrace();
			return new float[shape[0]][shape[1]][shape[2]][shape[3]];
		}

		float[][][][] data = new float[shape[0]][shape[1]][shape[2]][shape[3]];
		// see if an alternate data-reading algorithm that avoids division and modulos
		// could be faster
		for (int i = 0; i < _data.getSize(); i++) {
			int x = i % shape[3];
			int y = (i / shape[3]) % shape[2];
			int z = (i / (shape[3] * shape[2])) % shape[1];
			int t = (i / (shape[3] * shape[2] * shape[1])) % shape[0];

			float record = _data.getFloat(i);

			data[t][z][shape[2] - 1 - y][x] = record;
		}

		return data;
	}

	private static File downloadFile(String url, String fileName) throws IOException {
		URL dataURL = new URL(url);

		File dataDir = new File(dataFolder);
		dataDir.mkdirs();
		InputStream is = dataURL.openStream();

		OutputStream os = new FileOutputStream(dataFolder + fileName);
		byte[] buffer = new byte[16 * 1024];
		int transferredBytes = is.read(buffer);
		while (transferredBytes > -1) {
			os.write(buffer, 0, transferredBytes);
			transferredBytes = is.read(buffer);
		}
		is.close();
		os.close();

		return new File(dataFolder + fileName);
	}
}
