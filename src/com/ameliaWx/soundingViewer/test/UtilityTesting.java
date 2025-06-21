package com.ameliaWx.soundingViewer.test;

import com.ameliaWx.soundingViewer.dataSources.acars.Acars;

import java.io.IOException;

public class UtilityTesting {
    public static void main(String[] args) throws IOException {
//        Acars.getAcarsFilesByDayAndStation(new DateTime(2025, 06, 20, 0, 0, DateTimeZone.UTC), "DAL");
        Acars.getRecentAcarsFilesByStation("DAL");
    }
}
