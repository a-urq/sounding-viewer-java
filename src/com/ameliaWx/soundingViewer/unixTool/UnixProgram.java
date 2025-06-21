package com.ameliaWx.soundingViewer.unixTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.ameliaWx.soundingViewer.dataSources.FileTools;
import com.ameliaWx.soundingViewer.dataSources.acars.Acars;
import com.ameliaWx.soundingViewer.dataSources.acars.AcarsStation;
import com.ameliaWx.soundingViewer.dataSources.acars.AcarsStations;
import com.ameliaWx.soundingViewer.dataSources.raob.Igra2;
import com.ameliaWx.soundingViewer.dataSources.raob.SpcExper;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.ameliaWx.soundingViewer.Sounding;
import com.ameliaWx.soundingViewer.SoundingFrame;
import com.ameliaWx.soundingViewer.unixTool.nwp.ModelDerived;

public class UnixProgram {

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

//		args = new String[] { "-h", "-s", "KFWD", "-d", "20151226-21" };
//		args = new String[] { "-h", "-s", "KDDC", "-d", "20160524-21" };
//		args = new String[] { "-c", "-s", "KOAX"};

		if (args.length == 0) {
			doGui();
		} else {
			doCli(args);
		}
	}

	private static void doGui() {
		JFrame init = new JFrame("Initializing...");
		init.setSize(500, 0);
		init.setLocationRelativeTo(null);
		init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		init.setVisible(true);

		try {
			Igra2.initialize();
//			System.out.println("igra2 done");
			RadiosondeSite.loadSitesFromCache();
//			System.out.println("rs done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		init.dispose();

		int soundingTypeOption = JOptionPane.showOptionDialog(null,
				"What kind of sounding would you like to view?", "Sounding Viewer",
				0, JOptionPane.DEFAULT_OPTION, null, new String[] { "Radiosonde", "ACARS" }, 0);

		if (soundingTypeOption == 0) {
			int currHistOption = JOptionPane.showOptionDialog(null,
					"Would you like to view a current or historical weather balloon?", "IGRA2/SPC-EXPER Radiosonde Selection",
					0, JOptionPane.DEFAULT_OPTION, null, new String[]{"Current", "Historical"}, 0);

			doGuiCurrHist(currHistOption);
		} else if (soundingTypeOption == 1) {
			doGuiAcars();
		}
	}

	private static void doGuiCurrHist(int currHistOption) {
		if (currHistOption == 0) {
			RadiosondeSite site = selectSiteGui(true);
			
			System.out.println("FOUR LETTER CODE: " + site.getFourLetterCode());

			if (site.getFourLetterCode().length() > 0) {
				UnixProgram.displayCurrentSounding(site);
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
//					UnixProgram.displaySounding(site, DateTime.now(DateTimeZone.UTC));
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

	private static void doGuiAcars() {
		int acarsSearchOption = JOptionPane.showOptionDialog(null,
				"How would you like to search for ACARS data?", "Acars Data Search",
				0, JOptionPane.DEFAULT_OPTION, null, new String[] { "Most Recent from Station", "Day + Hour", "Day + Station" }, 0);

		TreeMap<String, String> fileList = null;
		switch (acarsSearchOption) {
			case 0:
				String station = acarsStationSelectionGui();

				try {
					fileList = Acars.getRecentAcarsFilesByStation(station);
                } catch (IOException e) {
					JOptionPane.showMessageDialog(null, "ACARS file list could not be read.");
                }

                break;
			case 1:
				DateTime d = selectDateGui();

				try {
					fileList = Acars.getAcarsFilesByDayAndHour(d);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "ACARS file list could not be read.");
				}

				break;
			case 2:
				String dateStr = (String) JOptionPane.showInputDialog(null,
						"What convective day would you like to find data for?\nUse format YYYYMMDD.\n(Convective day: from 12:00Z to 11:59Z)",
						"ACARS date selector", JOptionPane.QUESTION_MESSAGE);
				d = new DateTime(Integer.parseInt(dateStr.substring(0, 4)), Integer.parseInt(dateStr.substring(4, 6)), Integer.parseInt(dateStr.substring(6, 8)),
						0, 0, DateTimeZone.UTC);

				station = acarsStationSelectionGui();

				try {
					fileList = Acars.getAcarsFilesByDayAndStation(d, station);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "ACARS file list could not be read.");
				}

				break;
		}

		String selectedFile = acarsFileSelectionGui(fileList);
		displayAcarsSounding(selectedFile);
	}

	// Made to be expandable later if user isn't familiar with relevant ACARS codes.
	private static String acarsStationSelectionGui() {
		String station = JOptionPane.showInputDialog(null, "Input an ACARS station code (Ex. \"DAL\", \"OKC\")");

		return station;
	}

	// Complicated so should be reused.
	private static String acarsFileSelectionGui(TreeMap<String, String> fileList) {
		ArrayList<String> links = new ArrayList<>();
		ArrayList<String> aliases = new ArrayList<>();

		for(String file : fileList.keySet()) {
			String alias = fileList.get(file);

			links.add(file);
			aliases.add(alias);
		}

		String[] linksArr = new String[links.size()];
		String[] aliasesArr = new String[aliases.size()];
		linksArr = links.toArray(linksArr);
		aliasesArr = aliases.toArray(aliasesArr);

		String chosenFileAlias = (String) JOptionPane.showInputDialog(null,
				"Which file would you like to use?", "ACARS File Select",
				JOptionPane.QUESTION_MESSAGE, null, aliasesArr, aliasesArr[0]);

		int indexOfChoice = ArrayUtils.indexOf(aliasesArr, chosenFileAlias);

		return linksArr[indexOfChoice];
	}

	// Complicated so should be reused.
	private static void displayAcarsSounding(String linkUrl) {
        File acars = null;
        try {
            acars = FileTools.downloadFile(linkUrl, "acars.snd");
        } catch (IOException e) {
			JOptionPane.showMessageDialog(null, "ACARS file could not be downloaded!");
        }

        Sounding s = null;
		try {
			s = Acars.readAcarsFile(acars);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "ACARS file could not be read!");
		}

		AcarsStation stat = AcarsStations.acarsStationFromCode((String) s.getMetadata("station"));
		new SoundingFrame(stat.location + " ACARS", s, (DateTime) s.getMetadata("date-time"), stat.lat, stat.lon).setVisible(true);
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
						Igra2.initialize();
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
				UnixProgram.displayCurrentSounding(site);
			} else {
				try {
					UnixProgram.displaySounding(site, DateTime.now(DateTimeZone.UTC));
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
						Igra2.initialize();
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

				UnixProgram.displaySounding(site, d);
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
			if (Igra2.startOfCurrentData == 0) {
				JFrame init = new JFrame("Connecting to IGRA2 archive, this may take a few seconds...");
				init.setSize(500, 0);
				init.setLocationRelativeTo(null);
				init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				init.setVisible(true);

				Igra2.initialize();

				init.dispose();
			}

			Object[] soundingRet = SpcExper.getSounding(site);
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
			if (Igra2.startOfCurrentData == 0) {
				JFrame init = new JFrame("Connecting to IGRA2 archive, this may take a few seconds...");
				init.setSize(500, 0);
				init.setLocationRelativeTo(null);
				init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				init.setVisible(true);

				Igra2.initialize();

				init.dispose();
			}

			JFrame loading = new JFrame(
					(d.getYear() >= Igra2.startOfCurrentData) ? "Loading radiosonde, this may take a little while..."
							: "Loading radiosonde, this may take a minute or two...");

			loading.setSize(500, 0);
			loading.setLocationRelativeTo(null);
			loading.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			loading.setVisible(true);

			Object[] soundingRet = Igra2.getSounding(site, d);
			Sounding sounding = (Sounding) soundingRet[0];
			d = (DateTime) soundingRet[1];

			loading.dispose();
			new SoundingFrame(site.locationString() + " Radiosonde", sounding, d, site.getLatitude(),
					site.getLongitude()).setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
