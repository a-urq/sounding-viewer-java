package com.ameliaWx.soundingViewer.unixTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.soundingViewer.unixTool.nwp.ModelDerived;
import com.ameliaWx.weatherUtils.UnitConversions;
import com.ameliaWx.weatherUtils.WeatherUtils;

public class RadiosondeWrapper {
	private static int startOfCurrentData;

	// created for use in optimizing RadarView performance
	public static void initialize() throws IOException {
		startOfCurrentData = determineStartOfCurrentData();
		@SuppressWarnings("unused")
		int dummy = startOfCurrentData; // useless on its own, induces static init block. idk if there's a smarter or
										// less hacky way to do this
	}

	public static void main(String[] args) {
//		System.out.println(UnitConversions.kelvinToFahrenheit(WeatherUtils.dewpoint(UnitConversions.fahrenheitToKelvin(66), 0.31)) + "F");
//		
//		System.exit(0);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

//		try {
//			downloadFile(, "USM00072357-data.txt.zip");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		displaySounding(RadiosondeSite.findSite("KFWD"), 2019, 10, 21, 0);
//		displaySounding(RadiosondeSite.findSite("KFWD"), 2017, 2, 11, 19);
//		displaySounding(RadiosondeSite.findSite("KOUN"), 1999, 5, 4, 0);
//		displaySounding(RadiosondeSite.findSite("KOUN"), 2021, 2, 16, 0);
//		displaySounding(RadiosondeSite.findSite("KFWD"), 2023, 6, 21, 0);
//		displaySounding(RadiosondeSite.findSite("KOUN"), 2021, 2, 16, 0);
//		displaySounding(RadiosondeSite.findSite("KDDC"), 2023, 7, 7, 0);
//		displaySounding(RadiosondeSite.findSite("KMAF"), 2023, 5, 24, 19);
//		displaySounding(RadiosondeSite.findSite("KOUN"), 2023, 9, 7, 0);
//		displaySounding(RadiosondeSite.findSite("KOUN"), 2023, 2, 27, 3);

//		displayCurrentSounding(RadiosondeSite.findSite("KCRP"));

//		args = new String[] { "-h", "-s", "KMPX", "-d", "20190719-21" };
//		args = new String[] { "-c", "-s", "KOUN"};

		if (args.length == 0) {
			doGui();
		} else {
			doCli(args);
		}
	}

	private static void doGui() {
		JFrame init = new JFrame("Connecting to IGRA2 archive, this may take a few seconds...");
		init.setSize(500, 0);
		init.setLocationRelativeTo(null);
		init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		init.setVisible(true);

		try {
			initialize();
			RadiosondeSite.init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		init.dispose();

		int currHistOption = JOptionPane.showOptionDialog(null,
				"Would you like to view a current or historical weather balloon?", "IGRA2/SPC-EXPER Radiosonde Viewer",
				0, JOptionPane.DEFAULT_OPTION, null, new String[] { "Current", "Historical" }, 0);

		doGuiCurrHist(currHistOption);
	}

	private static void doGuiCurrHist(int currHistOption) {
		if (currHistOption == 0) {
			RadiosondeSite site = selectSiteGui(true);
			
			System.out.println("FOUR LETTER CODE: " + site.getFourLetterCode());

			if (site.getFourLetterCode().length() > 0) {
				RadiosondeWrapper.displayCurrentSounding(site);
			} else {
				JFrame init = new JFrame("Getting GFS data, this may take a few seconds...");
				init.setSize(500, 0);
				init.setLocationRelativeTo(null);
				init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				init.setVisible(true);
				
				Sounding gfs = ModelDerived.getGfsSounding(site.getLatitude(), site.getLongitude());
				
				init.dispose();
				
				new SoundingFrame(site.locationString() + " GFS-Derived", gfs, DateTime.now(DateTimeZone.UTC), 33,
						-96.5).setVisible(true);
				
//				try {
//					RadiosondeWrapper.displaySounding(site, DateTime.now(DateTimeZone.UTC));
//				} catch (RadiosondeNotFoundException e) {
//					e.printStackTrace();
//					System.exit(0);
//				}
			}
		} else if (currHistOption == 1) {
			RadiosondeSite site = selectSiteGui(false);

			doGuiHistDate(currHistOption, site);
		}
	}

	private static void doGuiHistDate(int currHistOption, RadiosondeSite site) {
		DateTime d = selectDateGui();

		try {
			displaySounding(site, d);
		} catch (RadiosondeNotFoundException e) {
			int errOption = JOptionPane.showOptionDialog(null, "Radiosonde not found for\n " + site.toString() + " - "
					+ d.toString()
					+ "\nand no suitably close data could be found.\nWould you like to try another date, another site, or quit the program?",
					"IGRA2/SPC-EXPER Radiosonde Viewer", 0, JOptionPane.ERROR_MESSAGE, null,
					new String[] { "Another Date", "Another Site", "Quit" }, 0);

			switch (errOption) {
			case 0:
				doGuiHistDate(currHistOption, site);
				break;
			case 1:
				doGuiCurrHist(currHistOption);
				break;
			case 2:
				System.exit(0);
				break;
			default:
				System.exit(0);
			}
		}
	}

	private static RadiosondeSite selectSiteGui(boolean current) {
		String chosenCountry = (String) JOptionPane.showInputDialog(null,
				"In what country is the site you want to use?", "IGRA2/SPC-EXPER Radiosonde Viewer",
				JOptionPane.QUESTION_MESSAGE, null, RadiosondeSite.getCountries(), RadiosondeSite.getCountries()[0]);

		System.out.println(chosenCountry);

		String chosenState = "";
		if ("United States".equals(chosenCountry)) {
			chosenState = (String) JOptionPane.showInputDialog(null, "In what state is the site you want to use?",
					"IGRA2/SPC-EXPER Radiosonde Viewer", JOptionPane.QUESTION_MESSAGE, null,
					RadiosondeSite.getUsStates(), RadiosondeSite.getUsStates()[0]);

			System.out.println(chosenState);
		}

		RadiosondeSite[] sites = RadiosondeSite.getSitesInRegion(chosenCountry, chosenState, current);

		String[] siteNames = new String[sites.length];

		for (int i = 0; i < sites.length; i++) {
			siteNames[i] = sites[i].getCity();

			if (!current) {
				siteNames[i] += " (" + sites[i].getStartYear() + " - " + sites[i].getEndYear() + ")";
			}
		}

		String chosenCity = (String) JOptionPane.showInputDialog(null, "Which site you want to use?",
				"IGRA2/SPC-EXPER Radiosonde Viewer", JOptionPane.QUESTION_MESSAGE, null, siteNames, siteNames[0]);

		System.out.println(chosenCity);

		int indexOfSite = -1;

		for (int i = 0; i < siteNames.length; i++) {
			if (chosenCity.equals(siteNames[i])) {
				indexOfSite = i;
				break;
			}
		}

		RadiosondeSite selectedSite = sites[indexOfSite];

		return selectedSite;
	}

	private static DateTime selectDateGui() {
		String dateStr = (String) JOptionPane.showInputDialog(null,
				"What date and time would you like to see? Use format YYYYMMDD-HH and be sure to use UTC.",
				"IGRA2/SPC-EXPER Radiosonde Viewer", JOptionPane.QUESTION_MESSAGE);

		int yyyy = Integer.valueOf(dateStr.substring(0, 4));
		int mm = Integer.valueOf(dateStr.substring(4, 6));
		int dd = Integer.valueOf(dateStr.substring(6, 8));
		int hh = Integer.valueOf(dateStr.substring(9, 11));

		return new DateTime(yyyy, mm, dd, hh, 0, DateTimeZone.UTC);
	}

	private static void doCli(String[] args) {
		String currHistFlag = args[0];

		if ("-c".equals(currHistFlag)) {
			String[] flag = { args[1] };
			String[] arg = { args[2] };

			RadiosondeSite site = null;

			for (int i = 0; i < flag.length; i++) {
				if ("-s".equals(flag[0])) {
					String siteArg = arg[0];

					System.out.println("Connecting to IGRA2 archive, please wait 10-15 seconds...");
					try {
						initialize();
					} catch (IOException e) {
						System.err.println("Could not connect to IGRA2 archive.");
						e.printStackTrace();
						return;
					}

					site = RadiosondeSite.findSite(siteArg);
					System.out.println("Connection complete.");
				}
			}

			if (site.getFourLetterCode().length() > 0) {
				RadiosondeWrapper.displayCurrentSounding(site);
			} else {
				try {
					RadiosondeWrapper.displaySounding(site, DateTime.now(DateTimeZone.UTC));
				} catch (RadiosondeNotFoundException e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		} else if ("-h".equals(currHistFlag)) {
			String[] flag = { args[1], args[3] };
			String[] arg = { args[2], args[4] };

			RadiosondeSite site = null;
			DateTime d = null;

			for (int i = 0; i < flag.length; i++) {
				if ("-s".equals(flag[i])) {
					String siteArg = arg[i];

					System.out.println("Connecting to IGRA2 archive, please wait 10-15 seconds...");
					try {
						initialize();
					} catch (IOException e) {
						System.err.println("Could not connect to IGRA2 archive.");
						e.printStackTrace();
						return;
					}

					site = RadiosondeSite.findSite(siteArg);
					System.out.println("Connection complete.");
				}

				if ("-d".equals(flag[i])) {
					String dateArg = arg[i];

					System.out.println("if sounding doesn't load, try using date format YYYYMMDD-HH");

					int yyyy = Integer.valueOf(dateArg.substring(0, 4));
					int mm = Integer.valueOf(dateArg.substring(4, 6));
					int dd = Integer.valueOf(dateArg.substring(6, 8));
					int hh = Integer.valueOf(dateArg.substring(9, 11));

					System.out.println();

					d = new DateTime(yyyy, mm, dd, hh, 0, DateTimeZone.UTC);
				}
			}

			System.out.println();

			try {

				RadiosondeWrapper.displaySounding(site, d);
			} catch (RadiosondeNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
			}
		} else {
			System.out.println("Please use either -c (Current) or -h (Historical) as the first argument.");
		}
	}

	public static void displayCurrentSounding(RadiosondeSite site) {
		JFrame loading = new JFrame("Loading radiosonde, this may take a little while...");

		loading.setSize(500, 0);
		loading.setLocationRelativeTo(null);
		loading.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loading.setVisible(true);

		displaySpcExperSounding(site);
		loading.dispose();
	}

	public static void displaySounding(RadiosondeSite site, int year, int month, int day, int hour)
			throws RadiosondeNotFoundException {
		DateTime d = new DateTime(year, month, day, hour, 0, DateTimeZone.UTC);

		displaySounding(site, d);
	}

	public static void displaySpcExperSounding(RadiosondeSite site) {
		try {
			if (startOfCurrentData == 0) {
				JFrame init = new JFrame("Connecting to IGRA2 archive, this may take a few seconds...");
				init.setSize(500, 0);
				init.setLocationRelativeTo(null);
				init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				init.setVisible(true);

				initialize();

				init.dispose();
			}

			Object[] soundingRet = getSoundingSPCExper(site);
			Sounding sounding = (Sounding) soundingRet[0];
			DateTime d = (DateTime) soundingRet[1];

			new SoundingFrame(site.locationString() + " Radiosonde", sounding, d, site.getLatitude(),
					site.getLongitude()).setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void displaySounding(RadiosondeSite site, DateTime d) throws RadiosondeNotFoundException {
		try {
			if (startOfCurrentData == 0) {
				JFrame init = new JFrame("Connecting to IGRA2 archive, this may take a few seconds...");
				init.setSize(500, 0);
				init.setLocationRelativeTo(null);
				init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				init.setVisible(true);

				initialize();

				init.dispose();
			}

			JFrame loading = new JFrame(
					(d.getYear() >= startOfCurrentData) ? "Loading radiosonde, this may take a little while..."
							: "Loading radiosonde, this may take a minute or two...");

			loading.setSize(500, 0);
			loading.setLocationRelativeTo(null);
			loading.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			loading.setVisible(true);

			Object[] soundingRet = getSounding(site, d);
			Sounding sounding = (Sounding) soundingRet[0];
			d = (DateTime) soundingRet[1];

			loading.dispose();
			new SoundingFrame(site.locationString() + " Radiosonde", sounding, d, site.getLatitude(),
					site.getLongitude()).setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Object[] getSounding(RadiosondeSite site, DateTime d)
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

		for (int i = 0; i < lines.size(); i++) {
			if (height[i] == -8888) {
				height[i] = height[i - 1] + WeatherUtils.heightAtPressure(pressure[i - 1], pressure[i],
						(temperature[i] + temperature[i - 1]) / 2);
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
				SoundingFrame.dataFolder + "sounding" + site.getInternationalCode() + "-" + d.toString() + "/");

		File soundingFile = new File(SoundingFrame.dataFolder + "sounding" + site.getInternationalCode() + "-"
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
						new File(SoundingFrame.dataFolder + "sounding" + site.getInternationalCode() + "-"
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
				SoundingFrame.dataFolder + "sounding" + site.getInternationalCode() + "-" + d.toString() + "/");
		new File(SoundingFrame.dataFolder + "sounding" + site.getInternationalCode() + "-" + d.toString() + "/")
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

	private static Object[] getSoundingSPCExper(RadiosondeSite site) throws IOException {
		Object[] soundingTextRet = getSoundingTextSPCExper(site); // { String, DateTime } a little gross but best i can
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
//			String relativeHumidityStr = line.substring(31, 41);
			String windDirectionStr = line.substring(42, 52);
//			String windSpeedStr = line.substring(53, 63);

//			double pressure_ = Double.valueOf(pressureStr) * 100;
//			double height_ = Double.valueOf(heightStr);
			double temperature_ = Double.valueOf(temperatureStr) / 10.0 + 273.15;
//			double relativeHumidity_ = Double.valueOf(relativeHumidityStr)/1000.0;
			double windDirection_ = Double.valueOf(windDirectionStr) + 180.0;
//			double windSpeed_ = Double.valueOf(windSpeedStr)/10.0;

			if (temperature_ < 130 && windDirection_ < -10) {
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

			double pressure_ = Double.valueOf(pressureStr) * 100;
			double height_ = Double.valueOf(heightStr);
			double temperature_ = Double.valueOf(temperatureStr) + 273.15;
			double dewpoint_ = Double.valueOf(relativeHumidityStr) + 273.15;
			double windDirection_ = Double.valueOf(windDirectionStr) + 180.0;
			double windSpeed_ = Double.valueOf(windSpeedStr) * 0.5144444;

			double uWind_ = Math.sin(Math.toRadians(windDirection_)) * windSpeed_;
			double vWind_ = Math.cos(Math.toRadians(windDirection_)) * windSpeed_;

			pressure[i] = pressure_;
			height[i] = height_;
			temperature[i] = temperature_;
			dewpoint[i] = dewpoint_;
			uWind[i] = uWind_;
			vWind[i] = vWind_;

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

					uWind[i] = weight1 * uWind[indBefore] + weight2 * uWind[indBefore];
					vWind[i] = weight1 * vWind[indBefore] + weight2 * vWind[indBefore];
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

	private static Object[] getSoundingTextSPCExper(RadiosondeSite site) throws IOException {
		DateTime d = DateTime.now(DateTimeZone.UTC);

		d = d.minusMinutes(d.getMinuteOfHour());

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

	private static int determineStartOfCurrentData() throws IOException {
		List<String> archiveFolder = listFilesInWebFolder(
				"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/access/data-y2d/", 2);

		return Integer.valueOf(archiveFolder.get(0).substring(20, 24));
	}

	private static List<String> listFilesInWebFolder(String url, int amt) throws IOException {
		File index = downloadFile(url, "index.html");

		ArrayList<String> files = new ArrayList<String>();

		Pattern p = Pattern.compile("<td><a href=.*?>");
		Matcher m = p.matcher(usingBufferedReader(index));

		while (m.find()) {
			String group = m.group();
			String filename = group.substring(13, m.group().length() - 2);

			if (filename.endsWith(".zip")) {
				files.add(filename);
			}

			if (files.size() >= amt && amt != -1) {
				break;
			}
		}

		index.delete();

		return files;
	}

	/*
	 * Example of reading Zip archive using ZipFile class
	 */

	private static void readUsingZipFile(String fileName, String outputDir) throws IOException {
		new File(outputDir).mkdirs();
		final ZipFile file = new ZipFile(fileName);
		// System.out.println("Iterating over zip file : " + fileName);

		try {
			final Enumeration<? extends ZipEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				final ZipEntry entry = entries.nextElement();
				extractEntry(entry, file.getInputStream(entry), outputDir);
			}
			// System.out.printf("Zip file %s extracted successfully in %s",
			// fileName, outputDir);
		} finally {
			file.close();
		}
	}

	/*
	 * Utility method to read data from InputStream
	 */

	private static void extractEntry(final ZipEntry entry, InputStream is, String outputDir) throws IOException {
		String exractedFile = outputDir + entry.getName();
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(exractedFile);
			final byte[] buf = new byte[8192];
			int length;

			while ((length = is.read(buf, 0, buf.length)) >= 0) {
				fos.write(buf, 0, length);
			}

		} catch (IOException ioex) {
			fos.close();
		}

	}

	private static String usingBufferedReader(File filePath) {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine).append(" ");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentBuilder.toString();
	}

	private static File downloadFile(String url, String fileName) throws IOException {
		URL dataURL = new URL(url);

		File dataDir = new File(SoundingFrame.dataFolder);
		dataDir.mkdirs();
		InputStream is = dataURL.openStream();

		OutputStream os = new FileOutputStream(SoundingFrame.dataFolder + fileName);
		byte[] buffer = new byte[16 * 1024];
		int transferredBytes = is.read(buffer);
		while (transferredBytes > -1) {
			os.write(buffer, 0, transferredBytes);
			transferredBytes = is.read(buffer);
		}
		is.close();
		os.close();

		return new File(SoundingFrame.dataFolder + fileName);
	}
}
