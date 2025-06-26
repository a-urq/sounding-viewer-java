package com.ameliaWx.soundingViewer.unixTool;

import java.io.*;
import java.net.URL;
import java.text.Collator;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.swing.*;

import static com.ameliaWx.soundingViewer.GlobalVars.cacheFolder;
import static com.ameliaWx.soundingViewer.GlobalVars.dataFolder;
import static com.ameliaWx.soundingViewer.dataSources.FileTools.downloadFileToCache;

public class RadiosondeSite implements Comparable<RadiosondeSite> {
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

//	static {
//		initializeSites();
//	}
	
	public static void init() {
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

	private static String[][] countryListArr = null;
	private static String[][] usStatesArr = null;

	private static final int CACHE_UPDATE_FREQUENCY = 28; // units: days
	public static void loadSitesFromCache() {
		File lastDateUpdated = new File(cacheFolder + "last-date-updated.txt");
        File stationList = new File(cacheFolder + "igra2-station-list.txt");
        File countryList = new File(cacheFolder + "igra2-country-list.txt");
        File usStatesList = new File(cacheFolder + "igra2-us-states.txt");

		if(!stationList.exists() || !countryList.exists() || !usStatesList.exists() || !lastDateUpdated.exists()) {
			JFrame init = new JFrame("Performing first-time setup, this may take a few seconds...");
			init.setSize(500, 0);
			init.setLocationRelativeTo(null);
			init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			init.setVisible(true);

			initializeSites();

			init.dispose();
		}

		DateTime ldu;
		DateTime now = DateTime.now();
        try (Scanner lduSc = new Scanner(lastDateUpdated)) {
            String lduStr = lduSc.nextLine();

			int year = Integer.parseInt(lduStr.substring(0, 4));
			int month = Integer.parseInt(lduStr.substring(5, 7));
			int day = Integer.parseInt(lduStr.substring(8, 10));

			ldu = new DateTime(year, month, day, 0, 0);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

		if(ldu.isBefore(now.minusDays(CACHE_UPDATE_FREQUENCY))) {
			JFrame init = new JFrame("Updating cache, this may take a few seconds...");
			init.setSize(500, 0);
			init.setLocationRelativeTo(null);
			init.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			init.setVisible(true);

			initializeSites();

			init.dispose();
		}

        File caProvinces = loadResourceAsFile("res/caProvinces.txt");
        File fourLetterCodes = loadResourceAsFile("res/fourLetterCodes.txt");

        String[][] stationListArr = parseFixedWidth(stationList, 11, 9, 10, 7, 4, 30, 5, 6, 5);
        usStatesArr = parseFixedWidth(usStatesList, 2, 40);
        countryListArr = parseFixedWidth(countryList, 2, 40);
        String[][] fourLetterCodesArr = parseFixedWidth(fourLetterCodes, 11, 40);

        HashMap<String, String> usStatesMap = arrToHashMap(usStatesArr);
        HashMap<String, String> countryListMap = arrToHashMap(countryListArr);
        HashMap<String, String> fourLetterCodesMap = arrToHashMap(fourLetterCodesArr);

        sites = new ArrayList<>();

        for (String[] line : stationListArr) {
//			System.out.println(Arrays.toString(line));
//			System.out.println(sites.size());

            String internationalCode = line[0];

			if(internationalCode.isEmpty()) {
				continue;
			}

            double latitude = Double.parseDouble(line[1]);
            double longitude = Double.parseDouble(line[2]);
            double elevation = Double.parseDouble(line[3]);
            String state = usStatesMap.get(line[4]);
            String country = countryListMap.get(line[0].substring(0, 2));
            String city = line[5];
            int startYear = Integer.parseInt(line[6]);
            int endYear = Integer.parseInt(line[7]);
            int numRecords = Integer.parseInt(line[8]);

            if(state == null) {
                state = "";
            }

            if ("Canada".equals(country)) {
				System.out.println(city);
                // province assignments
            }

            String fourLetterCode = "";
            if ("United States".equals(country)) {
                if(fourLetterCodesMap.containsKey(internationalCode)) {
                    fourLetterCode = fourLetterCodesMap.get(internationalCode);
                }
            }

			if(city.contains(";")){
				int indSemicolon = city.indexOf(';');
				city = city.substring(0, indSemicolon);
			}

            RadiosondeSite site = new RadiosondeSite(internationalCode, fourLetterCode, latitude, longitude, elevation, country, state,
                    city, startYear, endYear, numRecords);

            sites.add(site);
        }

        Collections.sort(sites);
    }

	private static void initializeSites() {
		try {
			File stationList = downloadFileToCache(
					"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-station-list.txt",
					"igra2-station-list.txt");
			File countryList = downloadFileToCache(
					"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-country-list.txt",
					"igra2-country-list.txt");
			File usStatesList = downloadFileToCache(
					"https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-us-states.txt",
					"igra2-us-states.txt");

			File lastDateUpdated = new File(cacheFolder + "last-date-updated.txt");
			try (PrintWriter lduPw = new PrintWriter(lastDateUpdated)) {
				DateTime time = DateTime.now();
				lduPw.printf("%04d-%02d-%02d", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth());
			}
			
			File caProvinces = loadResourceAsFile("res/caProvinces.txt");
			File fourLetterCodes = loadResourceAsFile("res/fourLetterCodes.txt");

			String[][] stationListArr = parseFixedWidth(stationList, 11, 9, 10, 7, 4, 30, 5, 6, 5);
			usStatesArr = parseFixedWidth(usStatesList, 2, 40);
			countryListArr = parseFixedWidth(countryList, 2, 40);
			String[][] fourLetterCodesArr = parseFixedWidth(fourLetterCodes, 11, 40);

			HashMap<String, String> usStatesMap = arrToHashMap(usStatesArr);
			HashMap<String, String> countryListMap = arrToHashMap(countryListArr);
			HashMap<String, String> fourLetterCodesMap = arrToHashMap(fourLetterCodesArr);
			
			sites = new ArrayList<>();

			for (String[] line : stationListArr) {
//				System.out.println(Arrays.toString(line));
//				System.out.println(sites.size());

				String internationalCode = line[0];

				if(internationalCode.isEmpty()) {
					continue;
				}

				double latitude = Double.parseDouble(line[1]);
				double longitude = Double.parseDouble(line[2]);
				double elevation = Double.parseDouble(line[3]);
				String state = usStatesMap.get(line[4]);
				String country = countryListMap.get(line[0].substring(0, 2));
				String city = line[5];
				int startYear = Integer.parseInt(line[6]);
				int endYear = Integer.parseInt(line[7]);
				int numRecords = Integer.parseInt(line[8]);
				
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
	
	public static String[] getCountries() {
		String[] countries = new String[countryListArr.length];
		
		countries[0] = "United States";
		
		boolean detectedUS = false;
		
		for(int i = 0; i < countries.length; i++) {
			if(detectedUS) {
				countries[i] = countryListArr[i][1];
			} else {
				if("United States".equals(countryListArr[i][1])) {
					detectedUS = true;
					continue;
				}
					
				countries[i + 1] = countryListArr[i][1];
			}
		}
		
		return countries;
	}
	
	public static String[] getUsStates() {
		String[] states = new String[usStatesArr.length];
		
		for(int i = 0; i < states.length; i++) {
			states[i] = usStatesArr[i][1];
		}
		
		return states;
	}
	
	public static RadiosondeSite[] getSitesInRegion(String country, String state, boolean current) {
		ArrayList<RadiosondeSite> sitesList = new ArrayList<>();
		
		DateTime nowMinus7 = DateTime.now(DateTimeZone.UTC).minusDays(7);
		
		for(int i = 0; i < sites.size(); i++) {
//			System.out.println(sites.get(i));
			if(country.equals(sites.get(i).getCountry()) && state.equals(sites.get(i).getState())) {
				if(!current || sites.get(i).getEndYear() >= nowMinus7.getYear()) {
					sitesList.add(sites.get(i));
				}
			}
			
//			System.out.println(sitesList.size());
		}
		
		RadiosondeSite[] sites = new RadiosondeSite[sitesList.size()];
		for(int i = 0; i < sitesList.size(); i++) {
			sites[i] = sitesList.get(i);
		}
		
		return sites;
	}

	public static File loadResourceAsFile(String urlStr) {
//		System.out.println(urlStr);
		URL url = RadiosondeSite.class.getResource(urlStr);
		InputStream is = RadiosondeSite.class.getResourceAsStream(urlStr);
//		System.out.println(url);
//		System.out.println(is);
		URL tilesObj = url;

		// System.out.println("Temp-file created.");

		File file = new File(dataFolder + "temp/" + urlStr + "");
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
