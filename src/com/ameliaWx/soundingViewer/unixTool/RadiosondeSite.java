package com.ameliaWx.soundingViewer.unixTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class RadiosondeSite implements Comparable<RadiosondeSite> {
	public static String dataFolder = System.getProperty("user.home") + "/Documents/SoundingViewer/data/";

	public static ArrayList<RadiosondeSite> sites = new ArrayList<>();

	private String internationalCode;
	private String fourLetterCode;
	private double latitude;
	private double longitude;
	private double elevation;
	private String country;
	private String state; // also province for canadian stations
	private String city;
	private int startYear;
	private int endYear;
	private int numRecords;

	public RadiosondeSite(String internationalCode, String fourLetterCode, double latitude, double longitude,
			double elevation, String country, String state, String city, int startYear, int endYear, int numRecords) {
		this.internationalCode = internationalCode;
		this.fourLetterCode = fourLetterCode;
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
		this.country = country;
		this.state = state;
		this.city = city;
		this.startYear = startYear;
		this.endYear = endYear;
		this.numRecords = numRecords;
	}

	static {
		initializeSites();
	}

	public static void main(String[] args) {
//		for(int i = 0; i < sites.size(); i++) {
//			if(sites.get(i).getEndYear() == 2023) {
//				System.out.println(sites.get(i));
//			}
//		}
		
		System.out.println(findSite("KOUN"));
	}

	private static void initializeSites() {
		try {
			File stationList = downloadFile(
					"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-station-list.txt",
					"igra2-station-list.txt");
			File countryList = downloadFile(
					"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-country-list.txt",
					"igra2-country-list.txt");
			File usStatesList = downloadFile(
					"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-us-states.txt",
					"igra2-us-states.txt");
			
			File caProvinces = loadResourceAsFile("res/caProvinces.txt");
			File fourLetterCodes = loadResourceAsFile("res/fourLetterCodes.txt");

			String[][] stationListArr = parseFixedWidth(stationList, 11, 9, 10, 7, 4, 30, 5, 6, 5);
			String[][] usStatesArr = parseFixedWidth(usStatesList, 2, 40);
			String[][] countryListArr = parseFixedWidth(countryList, 2, 40);
			String[][] fourLetterCodesArr = parseFixedWidth(fourLetterCodes, 11, 40);

			HashMap<String, String> usStatesMap = arrToHashMap(usStatesArr);
			HashMap<String, String> countryListMap = arrToHashMap(countryListArr);
			HashMap<String, String> fourLetterCodesMap = arrToHashMap(fourLetterCodesArr);

			for (String[] line : stationListArr) {
//				System.out.println(Arrays.toString(line));

				String internationalCode = line[0];
				double latitude = Double.valueOf(line[1]);
				double longitude = Double.valueOf(line[2]);
				double elevation = Double.valueOf(line[3]);
				String state = usStatesMap.get(line[4]);
				String country = countryListMap.get(line[0].substring(0, 2));
				String city = line[5];
				int startYear = Integer.valueOf(line[6]);
				int endYear = Integer.valueOf(line[7]);
				int numRecords = Integer.valueOf(line[8]);
				
				if(state == null) {
					state = "";
				}

				if ("Canada".equals(country)) {
					// province assignments
				}

				String fourLetterCode = "";
				if ("United States".equals(country)) {
					if(fourLetterCodesMap.containsKey(internationalCode)) {
						fourLetterCode = fourLetterCodesMap.get(internationalCode);
					}
				}

				RadiosondeSite site = new RadiosondeSite(internationalCode, fourLetterCode, latitude, longitude, elevation, country, state,
						city, startYear, endYear, numRecords);
				
				sites.add(site);
			}
			
			Collections.sort(sites);
			
			stationList.delete();
			countryList.delete();
			usStatesList.delete();
		} catch (IOException e) {
			System.err.println("Unable to download files from IGRA-2 archive.");
			e.printStackTrace();
		}
	}
	
	public static RadiosondeSite findSite(String code) {
		if(code.length() == 4) {
			for(int i = 0; i < sites.size(); i++) {
				if("United States".equals(sites.get(i).getCountry())) {
					if(code.equals(sites.get(i).getFourLetterCode())) {
						return sites.get(i);
					}
				}
			}
		} else {
			for(int i = 0; i < sites.size(); i++) {
				if(code.equals(sites.get(i).getInternationalCode())) {
					return sites.get(i);
				}
			}
		}
		
		return null;
	}
	
	public static ArrayList<String> fourLetterCodeList() {
		ArrayList<String> list = new ArrayList<>();
		
		for(int i = 0; i < sites.size(); i++) {
			RadiosondeSite site = sites.get(i);
			
			if(site.getFourLetterCode().length() > 0) {
				list.add(site.locationString());
			}
		}
		
		return list;
	}

	private static String[][] parseFixedWidth(File file, int... widths) {
		try {
			Scanner sc = new Scanner(file);

			ArrayList<String> lines = new ArrayList<>();

			while (sc.hasNextLine()) {
				lines.add(sc.nextLine());
			}

			sc.close();

			String[][] tokens = new String[lines.size()][widths.length];

			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);

				int startPoint = 0;

				for (int j = 0; j < widths.length; j++) {
					int endPoint = startPoint + widths[j];

					if (endPoint > line.length())
						endPoint = line.length();

					tokens[i][j] = line.substring(startPoint, endPoint).trim();

					startPoint = endPoint;
				}
			}

			return tokens;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new String[0][0];
		}
	}

	private static HashMap<String, String> arrToHashMap(String[][] arr) {
		HashMap<String, String> ret = new HashMap<>();

		for (String[] line : arr) {
			ret.put(line[0], line[1]);
		}

		return ret;
	}

	public String getInternationalCode() {
		return internationalCode;
	}

	public String getFourLetterCode() {
		return fourLetterCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public String getCountry() {
		return country;
	}

	public String getState() {
		return state;
	}

	public String getCity() {
		return city;
	}

	public int getStartYear() {
		return startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public int getNumRecords() {
		return numRecords;
	}

	public static File loadResourceAsFile(String urlStr) {
//		System.out.println(urlStr);
		URL url = RadiosondeSite.class.getResource(urlStr);
		InputStream is = RadiosondeSite.class.getResourceAsStream(urlStr);
//		System.out.println(url);
//		System.out.println(is);
		URL tilesObj = url;

		// System.out.println("Temp-file created.");

		File file = new File(RadiosondeSite.dataFolder + "temp/" + urlStr + "");
		file.deleteOnExit();

		if (tilesObj == null) {
			System.out.println("Loading failed to start.");
			return null;
		}

		// System.out.println("Loading successfully started.");

		try {
			FileUtils.copyURLToFile(tilesObj, file);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file;
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
	
	public String locationString() {
		if(state.length() > 0) {
			if(fourLetterCode.length() > 0) {
				return fourLetterCode + " - " + city.toUpperCase() + ", " + state.toUpperCase() + ", " + country.toUpperCase();
			} else {
				return city.toUpperCase() + ", " + state.toUpperCase() + ", " + country.toUpperCase();
			}
		} else {
			return city.toUpperCase() + ", " + country.toUpperCase();
		}
	}
	
	@Override
	public String toString() {
		if(state.length() > 0) {
			return internationalCode + ": " + city + ", " + state + ", " + country + " - " + fourLetterCode;
		} else {
			return internationalCode + ": " + city + ", " + country;
		}
//		
//		return "internationalCode: " + internationalCode + ", " + 
//		"fourLetterCode: " + fourLetterCode + ", " + 
//		"latitude: " + latitude + ", " + 
//		"longitude: " + longitude + ", " + 
//		"elevation: " + elevation + ", " + 
//		"country: " + country + ", " + 
//		"state: " + state + ", " + 
//		"city: " + city + ", " + 
//		"startYear: " + startYear + ", " + 
//		"endYear: " + endYear + ", " + 
//		"numRecords: " + numRecords;15
	}

	@Override
	public int compareTo(RadiosondeSite o) {
		Collator usCollator = Collator.getInstance(Locale.US);
		usCollator.setStrength(Collator.PRIMARY);

		int countryCompare = 4 * usCollator.compare(country, o.country);
		int stateCompare = 2 * usCollator.compare(state, o.state);
		int cityCompare = 1 * usCollator.compare(city, o.city);

		int totalCompare = countryCompare + stateCompare + cityCompare;

		return totalCompare;
	}
}
