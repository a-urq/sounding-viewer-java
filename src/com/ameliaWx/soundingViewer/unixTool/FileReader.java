package com.ameliaWx.soundingViewer.unixTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.ameliaWx.soundingViewer.Sounding;

public class FileReader {
	public static Sounding readBufkit(File bufkitFile) throws FileNotFoundException {
		Scanner sc = new Scanner(bufkitFile);
		
		for(int i = 0; i < 4; i++) {
			sc.nextLine();
		}
		
		String stidTimeLine = sc.nextLine();
		
		String stid = stidTimeLine.substring(7, 11);
		String time = stidTimeLine.substring(33, 44);
		
		for(int i = 0; i < 14; i++) {
			sc.nextLine();
		}
		
		sc.close();
		
		Sounding sounding = new Sounding(new double[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {}, new double[] {});
		
		sounding.addMetadata("stationId", stid);
		sounding.addMetadata("time", time);
		
		return sounding;
	}
}
