package com.ameliaWx.soundingViewer;

public class AcarsStation {
	public int gsdId;
	public String code;
	public int wmoId;
	public double lat;
	public double lon;
	public double elev;
	public String location;
	
	public AcarsStation(int gsdId, String code, int wmoId, double lat, double lon, double elev, String location) {
		this.gsdId = gsdId;
		this.code = code;
		this.wmoId = wmoId;
		this.lat = lat;
		this.lon = lon;
		this.elev = elev;
		this.location = location;
	}
}
