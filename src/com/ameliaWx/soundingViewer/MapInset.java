package com.ameliaWx.soundingViewer;

import java.awt.image.BufferedImage;

public interface MapInset {
	public BufferedImage drawMapInset(double lat, double lon, double extent, int size);
}
