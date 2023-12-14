package com.ameliaWx.soundingViewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class AcarsStations {
	private static HashMap<String, AcarsStation> stations = new HashMap<>();

	static {
		try {
			File f = SoundingFrame.loadResourceAsFile("res/acars-key.txt");

			Scanner sc = new Scanner(f);

			sc.nextLine();

			while (sc.hasNextLine()) {
				String line = sc.nextLine();
//				System.out.println(line);
				
				int gsdId = Integer.valueOf(line.substring(0, 5).trim());
				String name = line.substring(5, 10).trim();
				int wmoId = Integer.valueOf(line.substring(11, 16).trim());
				double lat = Double.valueOf(line.substring(16, 24).trim());
				double lon = Double.valueOf(line.substring(24, 32).trim());
				double elev = Double.valueOf(line.substring(32, 37).trim());
				String location = line.substring(37).trim();
				
				AcarsStation station = new AcarsStation(gsdId, name, wmoId, lat, lon, elev, location);
				stations.put(name, station);
			}
			
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static AcarsStation acarsStationFromCode(String key) {
		return stations.get(key);
	}
}
