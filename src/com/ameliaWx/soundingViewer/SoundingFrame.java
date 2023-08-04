package com.ameliaWx.soundingViewer;

import static com.ameliaWx.soundingViewer.HazType.BLIZZARD;
import static com.ameliaWx.soundingViewer.HazType.DEV_TOR;
import static com.ameliaWx.soundingViewer.HazType.EXTREME_COLD;
import static com.ameliaWx.soundingViewer.HazType.EXTREME_HEAT;
import static com.ameliaWx.soundingViewer.HazType.FLASH_FLOOD;
import static com.ameliaWx.soundingViewer.HazType.MRGL_SVR;
import static com.ameliaWx.soundingViewer.HazType.MRGL_TOR;
import static com.ameliaWx.soundingViewer.HazType.NONE;
import static com.ameliaWx.soundingViewer.HazType.PDS_DEV_TOR;
import static com.ameliaWx.soundingViewer.HazType.PDS_TOR;
import static com.ameliaWx.soundingViewer.HazType.SVR;
import static com.ameliaWx.soundingViewer.HazType.TOR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.joda.time.DateTime;

import com.ameliaWx.weatherUtils.ParcelPath;
import com.ameliaWx.weatherUtils.PrecipitationType;
import com.ameliaWx.weatherUtils.PtypeAlgorithms;
import com.ameliaWx.weatherUtils.RecordAtLevel;
import com.ameliaWx.weatherUtils.WeatherUtils;

public class SoundingFrame extends JFrame {
	private static final long serialVersionUID = 396540838404275479L;

	private String soundingSource;

	private Sounding sounding0;
	private DateTime time0;
	private Sounding soundingM;
	private DateTime timeM;
	private Sounding sounding1;
	private DateTime time1;

	private double lat;
	private double lon;

	private double windOffsetAngle; // corrects for U and V not matching up with east and north, radians

	private ParcelPath pathType = ParcelPath.MIXED_LAYER_100MB;

	private boolean parcelPathVisible = true;
	@SuppressWarnings("unchecked")
	private ArrayList<RecordAtLevel>[] parcelPathSurfaceBased = new ArrayList[3];
	@SuppressWarnings("unchecked")
	private ArrayList<RecordAtLevel>[] parcelPathMixedLayer50Mb = new ArrayList[3];
	@SuppressWarnings("unchecked")
	private ArrayList<RecordAtLevel>[] parcelPathMixedLayer100Mb = new ArrayList[3];
	@SuppressWarnings("unchecked")
	private ArrayList<RecordAtLevel>[] parcelPathMostUnstable = new ArrayList[3];

	@SuppressWarnings("unchecked")
	private ArrayList<RecordAtLevel>[] parcelPathDowndraft = new ArrayList[3];

	// readout parameters (write once read many) (0=sounding0, 1=soundingM,
	// 2=sounding1)
	private double[] sbcape = new double[3];
	private double[] ml50cape = new double[3];
	private double[] ml100cape = new double[3];
	private double[] mucape = new double[3];

	private double[] sbcinh = new double[3];
	private double[] ml50cinh = new double[3];
	private double[] ml100cinh = new double[3];
	private double[] mucinh = new double[3];

	private double[] sbLcl = new double[3];
	private double[] ml50Lcl = new double[3];
	private double[] ml100Lcl = new double[3];
	private double[] muLcl = new double[3];

	private double[] sbCcl = new double[3];
	private double[] ml50Ccl = new double[3];
	private double[] ml100Ccl = new double[3];
	private double[] muCcl = new double[3];

	private double[] sbLfc = new double[3];
	private double[] ml50Lfc = new double[3];
	private double[] ml100Lfc = new double[3];
	private double[] muLfc = new double[3];

	private double[] sbEl = new double[3];
	private double[] ml50El = new double[3];
	private double[] ml100El = new double[3];
	private double[] muEl = new double[3];

	private double[] muLpl = new double[3];

	private double[] dcape = new double[3];

	private double[][] inflowLayer = new double[3][2];

	private double[] srh0_500 = new double[3];
	private double[] srh0_1 = new double[3];
	private double[] srh0_3 = new double[3];
	private double[] srhInflow = new double[3];

	private double[][] srw0_500 = new double[3][2];
	private double[][] srw0_1 = new double[3][2];
	private double[][] srw0_3 = new double[3][2];
	private double[][] srwInflow = new double[3][2];

	private double[] bulkShear0_500 = new double[3];
	private double[] bulkShear0_1 = new double[3];
	private double[] bulkShear0_3 = new double[3];
	private double[] bulkShearInflow = new double[3];

	private double[] streamwiseVort0_500 = new double[3];
	private double[] streamwiseVort0_1 = new double[3];
	private double[] streamwiseVort0_3 = new double[3];
	private double[] streamwiseVortInflow = new double[3];

	private double[] streamwiseness0_500 = new double[3];
	private double[] streamwiseness0_1 = new double[3];
	private double[] streamwiseness0_3 = new double[3];
	private double[] streamwisenessInflow = new double[3];

	private double[] threeCapeSb = new double[3];
	private double[] threeCapeMl = new double[3];
	private double[] ebwd = new double[3];

	private double[] scp = new double[3];
	private double[] stpE = new double[3];
	private double[] stpF = new double[3];
	private double[] ship = new double[3];
	private double[] devtor = new double[3];

	private double[] pwat = new double[3];

	private double[] omega0_1 = new double[3];
	private double[] omega1_3 = new double[3];
	private double[] omega0_3 = new double[3];
	private double[] omegaCap = new double[3];
	private double[] omegaInflow = new double[3];

	private double[] freezingLevel = new double[3];
	private double[][] dgzLayer = new double[3][2];
	private double[] dgzOmega = new double[3];
	private double[] dgzRH = new double[3];

	private PrecipitationType[] cantinBachand = new PrecipitationType[3];
	private PrecipitationType[] ramer = new PrecipitationType[3];
	private PrecipitationType[] bourgouin = new PrecipitationType[3];
	private PrecipitationType[] bourgouinRevised = new PrecipitationType[3];
	private PrecipitationType[] bourgouinRevExt = new PrecipitationType[3];

	private HazType hazType;

	private SoundingGraphics g;
	private MapInset mapInset;

	private static HashMap<PrecipitationType, String> ptypeNames = new HashMap<>();
	private static HashMap<PrecipitationType, Color> ptypeColors = new HashMap<>();
	static {
		ptypeNames.put(PrecipitationType.RAIN, "Rain");
		ptypeColors.put(PrecipitationType.RAIN, new Color(0, 196, 0));

		ptypeNames.put(PrecipitationType.FREEZING_RAIN, "Freezing Rain");
		ptypeColors.put(PrecipitationType.FREEZING_RAIN, new Color(255, 129, 255));

		ptypeNames.put(PrecipitationType.FREEZING_RAIN_SURFACE, "FRZR-Surface");
		ptypeColors.put(PrecipitationType.FREEZING_RAIN_SURFACE, new Color(255, 170, 170));

		ptypeNames.put(PrecipitationType.FREEZING_RAIN_ELEVATED, "FRZR-Elevated");
		ptypeColors.put(PrecipitationType.FREEZING_RAIN_ELEVATED, new Color(255, 70, 70));

		ptypeNames.put(PrecipitationType.FREEZING_RAIN, "Freezing Rain");
		ptypeColors.put(PrecipitationType.FREEZING_RAIN, new Color(255, 129, 192));

		ptypeNames.put(PrecipitationType.ICE_PELLETS, "Ice Pellets");
		ptypeColors.put(PrecipitationType.ICE_PELLETS, new Color(192, 128, 255));

		ptypeNames.put(PrecipitationType.SNOW, "Snow");
		ptypeColors.put(PrecipitationType.SNOW, new Color(76, 192, 255));

		ptypeNames.put(PrecipitationType.WET_SNOW, "Wet Snow");
		ptypeColors.put(PrecipitationType.WET_SNOW, new Color(128, 150, 255));

		ptypeNames.put(PrecipitationType.DRY_SNOW, "Dry Snow");
		ptypeColors.put(PrecipitationType.DRY_SNOW, new Color(128, 255, 255));

		ptypeNames.put(PrecipitationType.VERY_DRY_SNOW, "Very Dry Snow");
		ptypeColors.put(PrecipitationType.VERY_DRY_SNOW, new Color(192, 255, 255));
		
		ptypeNames.put(PrecipitationType.FRZR_ICEP_MIX, "ZR/IP Mix");
		ptypeColors.put(PrecipitationType.FRZR_ICEP_MIX, new Color(255, 75, 255));
		
		ptypeNames.put(PrecipitationType.FRZR_SNOW_MIX, "ZR/SN Mix");
		ptypeColors.put(PrecipitationType.FRZR_SNOW_MIX, new Color(194, 123, 123));
		
		ptypeNames.put(PrecipitationType.ICEP_SNOW_MIX, "IP/SN Mix");
		ptypeColors.put(PrecipitationType.ICEP_SNOW_MIX, new Color(255, 128, 0));
		
		ptypeNames.put(PrecipitationType.RAIN_ICEP_MIX, "RN/IP Mix");
		ptypeColors.put(PrecipitationType.RAIN_ICEP_MIX, new Color(203, 173, 153));
		
		ptypeNames.put(PrecipitationType.RAIN_SNOW_MIX, "RN/SN Mix");
		ptypeColors.put(PrecipitationType.RAIN_SNOW_MIX, new Color(153, 173, 203));
	}

	public SoundingFrame(String soundingSource, Sounding sounding0, DateTime time0, Sounding soundingM, DateTime timeM,
			Sounding sounding1, DateTime time1, double lat, double lon, double windOffsetAngle, MapInset mapInset) {
		this.soundingSource = soundingSource;

		this.lat = lat;
		this.lon = lon;
		this.timeM = timeM;

		this.mapInset = mapInset;

		this.windOffsetAngle = windOffsetAngle;

		this.setSize(1750, 900);
		this.setLocationRelativeTo(null);

		this.setTitle(generateTitle(this.timeM));

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		g = new SoundingGraphics();
		this.add(g);

		this.addKeyListener(new SoundingKeyListener());

		this.sounding0 = sounding0;
		this.soundingM = soundingM;
		this.sounding1 = sounding1;

		this.time0 = time0;
		this.timeM = timeM;
		this.time1 = time1;

		for (int i = 0; i < 3; i++) {
			if (i == 1 || sounding1 != null) {
				Sounding activeSounding = null;

				switch (i) {
				case 0:
					activeSounding = sounding0;

					double surfaceHeight = sounding0.getHeight()[sounding0.getHeight().length - 1];
					for (int z = 0; z < sounding0.getHeight().length; z++) {
						sounding0.getHeight()[z] -= surfaceHeight;
					}

					break;
				case 1:
					activeSounding = soundingM;

					surfaceHeight = soundingM.getHeight()[soundingM.getHeight().length - 1];
					for (int z = 0; z < soundingM.getHeight().length; z++) {
						soundingM.getHeight()[z] -= surfaceHeight;
					}

					break;
				case 2:
					activeSounding = sounding1;

					surfaceHeight = sounding1.getHeight()[sounding1.getHeight().length - 1];
					for (int z = 0; z < sounding1.getHeight().length; z++) {
						sounding1.getHeight()[z] -= surfaceHeight;
					}

					break;
				}

				parcelPathSurfaceBased[i] = WeatherUtils.computeParcelPath(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), ParcelPath.SURFACE_BASED, false);
				parcelPathMixedLayer50Mb[i] = WeatherUtils.computeParcelPath(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), ParcelPath.MIXED_LAYER_50MB,
						false);
				parcelPathMixedLayer100Mb[i] = WeatherUtils.computeParcelPath(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), ParcelPath.MIXED_LAYER_100MB,
						false);
				parcelPathMostUnstable[i] = WeatherUtils.computeParcelPath(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), ParcelPath.MOST_UNSTABLE, false);

				parcelPathDowndraft[i] = WeatherUtils.computeDcapeParcelPath(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint());

				double surfacePressure = activeSounding.getPressureLevels()[activeSounding.getPressureLevels().length
						- 1];

				double[] sbParcelPressure = new double[parcelPathSurfaceBased[i].size()];
				double[] sbParcelHeight = new double[parcelPathSurfaceBased[i].size()];
				double muLplPressure = parcelPathMostUnstable[i].get(0).getPressure();

				for (int j = 0; j < sbParcelPressure.length; j++) {
					sbParcelPressure[sbParcelPressure.length - 1 - j] = parcelPathSurfaceBased[i].get(j).getPressure();
					sbParcelHeight[sbParcelPressure.length - 1 - j] = parcelPathSurfaceBased[i].get(j).getHeight();
				}

				muLpl[activeReadoutSet] = logInterp(sbParcelPressure, sbParcelHeight, muLplPressure);

				sbcape[i] = WeatherUtils.computeCape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathSurfaceBased[i]);
				ml50cape[i] = WeatherUtils.computeCape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer50Mb[i]);
				ml100cape[i] = WeatherUtils.computeCape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer100Mb[i]);
				mucape[i] = WeatherUtils.computeCape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMostUnstable[i]);

				sbcinh[i] = WeatherUtils.computeCinh(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathSurfaceBased[i]);
				ml50cinh[i] = WeatherUtils.computeCinh(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer50Mb[i]);
				ml100cinh[i] = WeatherUtils.computeCinh(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer100Mb[i]);
				mucinh[i] = WeatherUtils.computeCinh(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMostUnstable[i]);

				sbLcl[i] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathSurfaceBased[i]);
				ml50Lcl[i] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMixedLayer50Mb[i]);
				ml100Lcl[i] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMixedLayer100Mb[i]);
				muLcl[i] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMostUnstable[i]);

				sbCcl[i] = WeatherUtils.convectiveCondensationLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathSurfaceBased[i]);
				ml50Ccl[i] = WeatherUtils.convectiveCondensationLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer50Mb[i]);
				ml100Ccl[i] = WeatherUtils.convectiveCondensationLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer100Mb[i]);
				muCcl[i] = WeatherUtils.convectiveCondensationLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMostUnstable[i]);

				sbLfc[i] = WeatherUtils.levelOfFreeConvection(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathSurfaceBased[i]);
				ml50Lfc[i] = WeatherUtils.levelOfFreeConvection(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer50Mb[i]);
				ml100Lfc[i] = WeatherUtils.levelOfFreeConvection(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer100Mb[i]);
				muLfc[i] = WeatherUtils.levelOfFreeConvection(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMostUnstable[i]);

				sbEl[i] = WeatherUtils.equilibriumLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathSurfaceBased[i]);
				ml50El[i] = WeatherUtils.equilibriumLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer50Mb[i]);
				ml100El[i] = WeatherUtils.equilibriumLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMixedLayer100Mb[i]);
				muEl[i] = WeatherUtils.equilibriumLevel(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), parcelPathMostUnstable[i]);

				dcape[i] = WeatherUtils.computeDcape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint());

				inflowLayer[i] = WeatherUtils.effectiveInflowLayer(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint());

				double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

				srh0_500[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				srh0_1[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				srh0_3[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				srhInflow[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				srw0_500[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				srw0_1[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				srw0_3[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				srwInflow[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				bulkShear0_500[i] = WeatherUtils.bulkShearMagnitude(activeSounding.getHeight(),
						activeSounding.getUWind(), activeSounding.getVWind(), 0, 500);
				bulkShear0_1[i] = WeatherUtils.bulkShearMagnitude(activeSounding.getHeight(), activeSounding.getUWind(),
						activeSounding.getVWind(), 0, 1000);
				bulkShear0_3[i] = WeatherUtils.bulkShearMagnitude(activeSounding.getHeight(), activeSounding.getUWind(),
						activeSounding.getVWind(), 0, 3000);
				bulkShearInflow[i] = WeatherUtils.bulkShearMagnitude(activeSounding.getHeight(),
						activeSounding.getUWind(), activeSounding.getVWind(), inflowLayer[i][0], inflowLayer[i][1]);

				streamwiseVort0_500[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				streamwiseVort0_1[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				streamwiseVort0_3[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				streamwiseVortInflow[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				streamwiseness0_500[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				streamwiseness0_1[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				streamwiseness0_3[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				streamwisenessInflow[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				threeCapeSb[i] = WeatherUtils.computeThreeCape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), ParcelPath.SURFACE_BASED);
				threeCapeMl[i] = WeatherUtils.computeThreeCape(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), ParcelPath.MIXED_LAYER_100MB);

				double[] ebwdVec = WeatherUtils.effectiveBulkWindDifference(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());

				ebwd[i] = Math.hypot(ebwdVec[0], ebwdVec[1]);

				scp[i] = WeatherUtils.supercellComposite(activeSounding.getPressureLevels(), activeSounding.getHeight(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), activeSounding.getUWind(),
						activeSounding.getVWind(), stormMotion);
				stpE[i] = WeatherUtils.significantTornadoParameterEffective(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind(), stormMotion);
				stpF[i] = WeatherUtils.significantTornadoParameterFixed(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind(), stormMotion);
				ship[i] = WeatherUtils.significantHailParameter(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				devtor[i] = WeatherUtils.deviantTornadoParameter(stpE[i], stormMotion,
						WeatherUtils.deviantTornadoMotion(activeSounding.getPressureLevels(),
								activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(),
								stormMotion));

				pwat[i] = WeatherUtils.precipitableWater(activeSounding.getHeight(), activeSounding.getTemperature(),
						activeSounding.getDewpoint());

				omega0_1[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(),
						activeSounding.getWWind(), 0, 1000);
				omega1_3[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(),
						activeSounding.getWWind(), 1000, 3000);
				omega0_3[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(),
						activeSounding.getWWind(), 0, 3000);
				omegaCap[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(),
						activeSounding.getWWind(), 0, ml100Lcl[i]);
				omegaInflow[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(),
						activeSounding.getWWind(), inflowLayer[i][0], inflowLayer[i][1]);

				double surfaceTemperature = activeSounding.getTemperature()[activeSounding.getTemperature().length - 1];
				double surfaceHeight = activeSounding.getHeight()[activeSounding.getHeight().length - 1];

				double[] relativeHumidity = new double[activeSounding.getDewpoint().length];
				for (int z = 0; z < relativeHumidity.length; z++) {
					relativeHumidity[z] = WeatherUtils.relativeHumidity(activeSounding.getTemperature()[z],
							activeSounding.getDewpoint()[z]);

//					System.out.printf("%5.0f\t%5.1f\t%5.1f\t%4.1f\n", activeSounding.getHeight()[z] - surfaceHeight, 
//							activeSounding.getTemperature()[z], 
//							activeSounding.getDewpoint()[z], 100 * relativeHumidity[z]);
				}

				dgzLayer[i] = WeatherUtils.dendriticGrowthZoneLayer(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint());
				freezingLevel[i] = WeatherUtils.freezingLevel(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint());
				dgzOmega[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(),
						activeSounding.getWWind(), dgzLayer[i][0], dgzLayer[i][1]);
				dgzRH[i] = WeatherUtils.averageParameterOverLayer(activeSounding.getHeight(), relativeHumidity,
						dgzLayer[i][0], dgzLayer[i][1]);

				cantinBachand[i] = PtypeAlgorithms.cantinBachandMethod(activeSounding.getPressureLevels(),
						activeSounding.getHeight(),
						activeSounding.getTemperature()[activeSounding.getTemperature().length - 2],
						surfaceTemperature);
				ramer[i] = PtypeAlgorithms.ramerMethod(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), activeSounding.getHeight(),
						surfacePressure, surfaceHeight);
				bourgouin[i] = PtypeAlgorithms.bourgouinMethod(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), activeSounding.getHeight(),
						surfacePressure, surfaceHeight, true);
				bourgouinRevised[i] = PtypeAlgorithms.bourgouinRevisedMethod(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), activeSounding.getHeight(),
						surfacePressure, surfaceHeight, true);
				bourgouinRevExt[i] = PtypeAlgorithms.bourgouinRevisedExtendedMethod(activeSounding.getPressureLevels(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), activeSounding.getHeight(),
						surfacePressure, surfaceHeight, surfaceTemperature, true);
			}
		}

		this.setVisible(true);

		this.activeSounding = soundingM;

		hazType = determineHazType();
	}

	public SoundingFrame(Sounding soundingM) {
		this("", soundingM, null, -1024.0, -1024.0, 0, null);
	}

	public SoundingFrame(Sounding soundingM, DateTime timeM, double lat, double lon) {
		this("", soundingM, timeM, lat, lon, 0, null);
	}

	public SoundingFrame(String soundingSource, Sounding soundingM) {
		this(soundingSource, soundingM, null, -1024.0, -1024.0, 0, null);
	}

	public SoundingFrame(String soundingSource, Sounding soundingM, DateTime timeM, double lat, double lon) {
		this(soundingSource, soundingM, timeM, lat, lon, 0, null);
	}

	public SoundingFrame(String soundingSource, Sounding soundingM, DateTime timeM, double lat, double lon,
			MapInset mapInset) {
		this(soundingSource, soundingM, timeM, lat, lon, 0, mapInset);
	}

	public SoundingFrame(String soundingSource, Sounding soundingM, DateTime timeM, double lat, double lon,
			double windOffsetAngle, MapInset mapInset) {
		this(soundingSource, null, null, soundingM, timeM, null, null, lat, lon, windOffsetAngle, mapInset);
	}

	private Sounding activeSounding;
	private int activeReadoutSet = 1;

	private int stormMotionVector = 2; // 0 = LM, 1 = MW, 2 = RM

	private class SoundingGraphics extends JComponent {
		private static final long serialVersionUID = 1649550301315411744L;

		private final BasicStroke thickStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		private final BasicStroke thinStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		public final Font CAPTION_FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			int width = this.getWidth();
			int height = this.getHeight();

			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, width, height);

			BufferedImage chart = drawSoundingChart(width, height);

			g2d.drawImage(chart, (width - chart.getWidth()) / 2, (height - chart.getHeight()) / 2, null);
		}

		private BufferedImage drawSoundingChart(int width, int height) {
			double scaleW = width / 1750.0;
			double scaleH = height / 900.0;

			double scale = Double.min(scaleW, scaleH);

			BufferedImage chart = new BufferedImage((int) (1750 * scale), (int) (900 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = chart.createGraphics();
			g.setStroke(thickStroke);
			g.setFont(CAPTION_FONT);

			g.setColor(Color.WHITE);

			BufferedImage skewT = drawSkewT(scale);
			g.drawImage(skewT, (int) (50 * scale), (int) (50 * scale), (int) (850 * scale), (int) (850 * scale), 0, 0,
					skewT.getWidth(), skewT.getHeight(), null);

			BufferedImage hodograph = drawHodograph(scale);
			g.drawImage(hodograph, (int) (875 * scale), (int) (50 * scale), (int) (1275 * scale), (int) (450 * scale),
					0, 0, hodograph.getWidth(), hodograph.getHeight(), null);

			if (mapInset != null) {
				BufferedImage mapInsetImg = mapInset.drawMapInset(lat, lon, 1, (int) (400 * scale));
				g.drawImage(mapInsetImg, (int) (1300 * scale), (int) (50 * scale), (int) (1700 * scale),
						(int) (450 * scale), 0, 0, mapInsetImg.getWidth(), mapInsetImg.getHeight(), null);
			}

			BufferedImage stormSlinky = drawStormSlinky(scale);
			g.drawImage(stormSlinky, (int) (875 * scale), (int) (475 * scale), (int) (1062.5 * scale),
					(int) (650 * scale), 0, 0, stormSlinky.getWidth(), stormSlinky.getHeight(), null);

			BufferedImage streamwiseVorticity = drawVorticityPlot(scale);
			g.drawImage(streamwiseVorticity, (int) (875 * scale), (int) (675 * scale), (int) (1062.5 * scale),
					(int) (850 * scale), 0, 0, streamwiseVorticity.getWidth(), streamwiseVorticity.getHeight(), null);

			BufferedImage severeReadouts = drawSevereReadouts(scale);
			g.drawImage(severeReadouts, (int) (1087.5 * scale), (int) (475 * scale), (int) (1487.5 * scale),
					(int) (850 * scale), 0, 0, severeReadouts.getWidth(), severeReadouts.getHeight(), null);

			BufferedImage winterReadouts = drawWinterReadouts(scale);
			g.drawImage(winterReadouts, (int) (1512.5 * scale), (int) (475 * scale), (int) (1700 * scale),
					(int) (650 * scale), 0, 0, winterReadouts.getWidth(), winterReadouts.getHeight(), null);

			BufferedImage hazardType = drawHazardType(scale);
			g.drawImage(hazardType, (int) (1512.5 * scale), (int) (675 * scale), (int) (1700 * scale),
					(int) (850 * scale), 0, 0, hazardType.getWidth(), hazardType.getHeight(), null);

			// skew-t frame
			g.drawRect((int) (50 * scale), (int) (50 * scale), (int) (800 * scale), (int) (800 * scale));

			// hodograph frame
			g.drawRect((int) (875 * scale), (int) (50 * scale), (int) (400 * scale), (int) (400 * scale));

			// map inset frame
			g.drawRect((int) (1300 * scale), (int) (50 * scale), (int) (400 * scale), (int) (400 * scale));

			// storm slinky frame
			g.drawRect((int) (875 * scale), (int) (475 * scale), (int) (187.5 * scale), (int) (175 * scale));

			// streamwise vorticity frame
			g.drawRect((int) (875 * scale), (int) (675 * scale), (int) (187.5 * scale), (int) (175 * scale));

			// severe readout frame
			g.drawRect((int) (1087.5 * scale), (int) (475 * scale), (int) (400 * scale), (int) (375 * scale));

			// winter readout frame
			g.drawRect((int) (1512.5 * scale), (int) (475 * scale), (int) (187.5 * scale), (int) (175 * scale));

			// haz type frame
			g.drawRect((int) (1512.5 * scale), (int) (675 * scale), (int) (187.5 * scale), (int) (175 * scale));

			// skew-t labels
			if (scale > 0.693) {
				g.setColor(new Color(255, 255, 255));
				for (int i = 10000; i < 110000; i += 10000) {
					int y = (int) linScale(Math.log(10000), Math.log(110000), 50 * scale, 850 * scale, Math.log(i));
					drawRightAlignedString(i / 100 + "mb", g, (int) (48 * scale), y);
				}
				for (int i = -50; i <= 50; i += 10) {
					int x = (int) linScale(-50, 50, 50 * scale, 850 * scale, i);
					drawCenteredString(i + "C", g, x, (int) (860 * scale));
				}
			}

			// hodograph label
			drawCenteredString("1 RING = 10 KT", g, (int) (1075 * scale), (int) (460 * scale));

			// time labels
			g.setColor(Color.WHITE);
			if (time1 != null) {
				double fontSize = 12 * scale;

				Font normalFont = new Font(Font.MONOSPACED, Font.BOLD, (int) (fontSize));
				Font highlightFont = new Font(Font.MONOSPACED, Font.BOLD, (int) (fontSize * 2.0));

				for (int i = 0; i < 3; i++) {
					DateTime time = null;

					switch (i) {
					case 0:
						time = time0;
						break;
					case 1:
						time = timeM;
						break;
					case 2:
						time = time1;
						break;
					}

					if (i == activeReadoutSet) {
						g.setFont(highlightFont);
					} else {
						g.setFont(normalFont);
					}

					drawCenteredString(timeString(time), g, (int) ((1700 * scale * (i + 1)) / 4), (int) (25 * scale));
				}
			}

			return chart;
		}

		private BufferedImage drawSkewT(double scale) {
			return drawSkewT(scale, pathType);
		}

		private BufferedImage drawSkewT(double scale, ParcelPath pathType) {
			BufferedImage skewT = new BufferedImage((int) (800 * scale), (int) (800 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = skewT.createGraphics();

			double[] pressure = activeSounding.getPressureLevels();
			double[] temperature = activeSounding.getTemperature();
			double[] wetbulb = activeSounding.getWetbulb();
			double[] dewpoint = activeSounding.getDewpoint();

			double[] virtTemp = new double[dewpoint.length];

			for (int i = 0; i < virtTemp.length; i++) {
				virtTemp[i] = WeatherUtils.virtualTemperature(temperature[i], dewpoint[i], pressure[i]);
			}

			g.setStroke(thickStroke);

			// choosing active storm motion vector
			String stormMotionVectorText = "";
			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
					activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

			switch (stormMotionVector) {
			case 0:
				stormMotionVectorText = "[LM]";
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 1:
				stormMotionVectorText = "[MW]";
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 2:
				stormMotionVectorText = "[RM]";
				break;
			case 3:
				stormMotionVectorText = "[UP]";
				stormMotion = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 4:
				stormMotionVectorText = "[DN]";
				stormMotion = WeatherUtils.corfidiDownshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 5:
				stormMotionVectorText = "[USER]";
				break;
			}

			// dry adiabats
			final double ITER_HEIGHT_CHANGE = 20; // change per iter for drawing adiabats [meter]
			g.setStroke(thinStroke);
			g.setColor(new Color(64, 0, 0));
			for (double temp = 223.15; temp <= 423.15; temp += 10) {
				double parcelTemp = temp;
				double parcelPres = 100000;

				while (parcelPres > 10000) {
//					System.out.printf("%8.1f Pa %8.1 C");

					double parcelTempNew = parcelTemp + (-0.0098 * ITER_HEIGHT_CHANGE); // DALR * 10 m
					double parcelPresNew = WeatherUtils.pressureAtHeight(parcelPres, ITER_HEIGHT_CHANGE, parcelTempNew);

					double y1D = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPres));
					double y2D = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPresNew));

					double skew1 = linScale(0, 800, 400, 0, y1D);
					double skew2 = linScale(0, 800, 400, 0, y2D);

					double x1D = linScale(223.15, 323.15, 0, 800, parcelTemp);
					double x2D = linScale(223.15, 323.15, 0, 800, parcelTempNew);

					g.drawLine((int) ((x1D + skew1) * scale), (int) (y1D * scale), (int) ((x2D + skew2) * scale),
							(int) (y2D * scale));

					parcelTemp = parcelTempNew;
					parcelPres = parcelPresNew;
				}
			}

			// moist adiabats
			g.setStroke(thinStroke);
			g.setColor(new Color(0, 64, 0));
			for (double temp = 263.15; temp <= 313.15; temp += 5) {
				double parcelTemp = temp;
				double parcelPres = 100000;

				while (parcelPres > 20000) {
//					System.out.printf("%8.1f Pa %8.1 C");

					double parcelTempNew = parcelTemp
							+ (-WeatherUtils.moistAdiabaticLapseRate(parcelTemp, parcelPres) * ITER_HEIGHT_CHANGE); // DALR
																													// *
																													// 10
																													// m
					double parcelPresNew = WeatherUtils.pressureAtHeight(parcelPres, ITER_HEIGHT_CHANGE, parcelTempNew);

					double y1D = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPres));
					double y2D = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPresNew));

					double skew1 = linScale(0, 800, 400, 0, y1D);
					double skew2 = linScale(0, 800, 400, 0, y2D);

					double x1D = linScale(223.15, 323.15, 0, 800, parcelTemp);
					double x2D = linScale(223.15, 323.15, 0, 800, parcelTempNew);

					g.drawLine((int) ((x1D + skew1) * scale), (int) (y1D * scale), (int) ((x2D + skew2) * scale),
							(int) (y2D * scale));

					parcelTemp = parcelTempNew;
					parcelPres = parcelPresNew;
				}
			}

			g.setStroke(thickStroke);
			// pressure lines
			g.setColor(new Color(64, 64, 64));
			for (int i = 10000; i < 110000; i += 10000) {
				int y = (int) linScale(Math.log(10000), Math.log(110000), 0, 800 * scale, Math.log(i));
				g.drawLine(0, y, (int) (800 * scale), y);
			}

			// temperature lines
			g.setColor(new Color(64, 64, 64));
			for (int i = -100; i < 50; i += 10) {
				int x = (int) linScale(-50, 50, 0, 800 * scale, i);
				g.drawLine((int) (x + 400 * scale), 0, x, (int) (800 * scale));
			}

			if (parcelPathVisible) {
				double[] parcelPressure = new double[parcelPathDowndraft[activeReadoutSet].size()];
				double[] parcelTemperature = new double[parcelPathDowndraft[activeReadoutSet].size()];
				double[] parcelDewpoint = new double[parcelPathDowndraft[activeReadoutSet].size()];
				double[] parcelVirtualTemperature = new double[parcelPathDowndraft[activeReadoutSet].size()];

				for (int i = 0; i < parcelPathDowndraft[activeReadoutSet].size(); i++) {
					parcelPressure[i] = parcelPathDowndraft[activeReadoutSet].get(i).getPressure();
					parcelTemperature[i] = parcelPathDowndraft[activeReadoutSet].get(i).getTemperature();
					parcelDewpoint[i] = parcelPathDowndraft[activeReadoutSet].get(i).getDewpoint();
					parcelVirtualTemperature[i] = WeatherUtils.virtualTemperature(parcelTemperature[i],
							parcelDewpoint[i], parcelPressure[i]);
				}

				g.setStroke(thinStroke);
				for (int i = 0; i < parcelPressure.length - 1; i++) {
					double y1 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPressure[i]));
					double y2 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPressure[i + 1]));

					double skew1 = linScale(0, 800, 400, 0, y1);
					double skew2 = linScale(0, 800, 400, 0, y2);

					double x1V = linScale(223.15, 323.15, 0, 800, parcelTemperature[i]);
					double x2V = linScale(223.15, 323.15, 0, 800, parcelTemperature[i + 1]);

					g.setColor(new Color(255, 128, 255));
					g.drawLine((int) ((x1V + skew1) * scale), (int) (y1 * scale), (int) ((x2V + skew2) * scale),
							(int) (y2 * scale));
				}
			}

			g.setStroke(thinStroke);
			g.setColor(new Color(127, 127, 255));
			{
				int x = (int) linScale(-50, 50, 0, 800 * scale, 0);
				g.drawLine((int) (x + 400 * scale), 0, x, (int) (800 * scale));
			}

			g.setStroke(thickStroke);
			for (int i = 0; i < temperature.length - 1; i++) {
				boolean drawDpt = (i < dewpoint.length - 1);

				double y1 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(pressure[i]));
				double y2 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(pressure[i + 1]));

				double skew1 = linScale(0, 800, 400, 0, y1);
				double skew2 = linScale(0, 800, 400, 0, y2);

				if (drawDpt) {
					g.setStroke(thinStroke);
					g.setColor(new Color(0, 255, 255));
					double x1W = linScale(223.15, 323.15, 0, 800, wetbulb[i]);
					double x2W = linScale(223.15, 323.15, 0, 800, wetbulb[i + 1]);

					g.drawLine((int) ((x1W + skew1) * scale), (int) (y1 * scale), (int) ((x2W + skew2) * scale),
							(int) (y2 * scale));

					g.setStroke(thickStroke);
					g.setColor(new Color(0, 255, 0));
					double x1D = linScale(223.15, 323.15, 0, 800, dewpoint[i]);
					double x2D = linScale(223.15, 323.15, 0, 800, dewpoint[i + 1]);

					g.drawLine((int) ((x1D + skew1) * scale), (int) (y1 * scale), (int) ((x2D + skew2) * scale),
							(int) (y2 * scale));

					g.setStroke(thickStroke);
					g.setColor(new Color(255, 0, 0));
					double x1V = linScale(223.15, 323.15, 0, 800, virtTemp[i]);
					double x2V = linScale(223.15, 323.15, 0, 800, virtTemp[i + 1]);

					drawDashedLine(g, (int) ((x1V + skew1) * scale), (int) (y1 * scale), (int) ((x2V + skew2) * scale),
							(int) (y2 * scale), 10);
				}

				g.setStroke(thickStroke);
				g.setColor(new Color(255, 0, 0));
				double x1T = linScale(223.15, 323.15, 0, 800, temperature[i]);
				double x2T = linScale(223.15, 323.15, 0, 800, temperature[i + 1]);

				g.drawLine((int) ((x1T + skew1) * scale), (int) (y1 * scale), (int) ((x2T + skew2) * scale),
						(int) (y2 * scale));
			}

			// effective inflow layer
			if (inflowLayer[activeReadoutSet][0] != -1024.0) {
				g.setFont(CAPTION_FONT);
				g.setColor(new Color(128, 255, 255));

				double btmInflowLayerPres = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1],
						inflowLayer[activeReadoutSet][0]);
				double topInflowLayerPres = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1],
						inflowLayer[activeReadoutSet][1]);

				double yBtm = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(btmInflowLayerPres));
				double yTop = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(topInflowLayerPres));

				double xInf = linScale(223.15, 323.15, 0, 800, 243.15);

				g.drawLine((int) (xInf * scale), (int) (yTop * scale), (int) (xInf * scale), (int) (yBtm * scale));

				g.drawLine((int) ((xInf - 40) * scale), (int) (yTop * scale), (int) ((xInf + 40) * scale),
						(int) (yTop * scale));
				g.drawLine((int) ((xInf - 40) * scale), (int) (yBtm * scale), (int) ((xInf + 40) * scale),
						(int) (yBtm * scale));

				String effInflowBtm = (inflowLayer[activeReadoutSet][0] > 3)
						? String.format("%5d m", (int) inflowLayer[activeReadoutSet][0])
						: "SFC";

				drawRightAlignedString(effInflowBtm, g, (int) ((xInf - 45) * scale), (int) (yBtm * scale));
				drawRightAlignedString(String.format("%5d m", (int) inflowLayer[activeReadoutSet][1]), g,
						(int) ((xInf - 45) * scale), (int) (yTop * scale));

				double srhRmInflow = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[activeReadoutSet][0], inflowLayer[activeReadoutSet][1]);

				String effInflowHlcy = String.format("%4d m²/s² " + stormMotionVectorText, (int) srhRmInflow);
				drawLeftCenterAlignedString(effInflowHlcy, g, (int) ((xInf + 5) * scale),
						(int) ((yTop + yBtm) / 2 * scale));
			}

			// parcel path
			ArrayList<RecordAtLevel> parcelPath = null;

			switch (pathType) {
			case SURFACE_BASED:
				parcelPath = parcelPathSurfaceBased[activeReadoutSet];
				break;
			case MIXED_LAYER_50MB:
				parcelPath = parcelPathMixedLayer50Mb[activeReadoutSet];
				break;
			case MIXED_LAYER_100MB:
				parcelPath = parcelPathMixedLayer100Mb[activeReadoutSet];
				break;
			case MOST_UNSTABLE:
				parcelPath = parcelPathMostUnstable[activeReadoutSet];
				break;
			}

			if (parcelPathVisible) {
				double[] parcelPressure = new double[parcelPath.size()];
				double[] parcelTemperature = new double[parcelPath.size()];
				double[] parcelDewpoint = new double[parcelPath.size()];
				double[] parcelVirtualTemperature = new double[parcelPath.size()];

				for (int i = 0; i < parcelPressure.length; i++) {
					parcelPressure[i] = parcelPath.get(i).getPressure();
					parcelTemperature[i] = parcelPath.get(i).getTemperature();
					parcelDewpoint[i] = parcelPath.get(i).getDewpoint();
					parcelVirtualTemperature[i] = WeatherUtils.virtualTemperature(parcelTemperature[i],
							parcelDewpoint[i], parcelPressure[i]);
				}

				double accumuLengthVirtTemp = 0.0;

				g.setStroke(thinStroke);
				for (int i = 0; i < parcelPressure.length - 1; i++) {
					double y1 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPressure[i]));
					double y2 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(parcelPressure[i + 1]));

					double skew1 = linScale(0, 800, 400, 0, y1);
					double skew2 = linScale(0, 800, 400, 0, y2);

					double x1V = linScale(223.15, 323.15, 0, 800, parcelVirtualTemperature[i]);
					double x2V = linScale(223.15, 323.15, 0, 800, parcelVirtualTemperature[i + 1]);

					double lengthVirtTempSegment = Math.hypot(((x1V + skew1) * scale) - ((x2V + skew2) * scale),
							(y1 * scale) - (y2 * scale));
					accumuLengthVirtTemp += lengthVirtTempSegment;

					if (accumuLengthVirtTemp % 8 < 4) {
						g.setColor(new Color(255, 255, 255));
						g.drawLine((int) ((x1V + skew1) * scale), (int) (y1 * scale), (int) ((x2V + skew2) * scale),
								(int) (y2 * scale));
					}

					double x1T = linScale(223.15, 323.15, 0, 800, parcelTemperature[i]);
					double x2T = linScale(223.15, 323.15, 0, 800, parcelTemperature[i + 1]);

					g.setColor(new Color(255, 255, 255));
					g.drawLine((int) ((x1T + skew1) * scale), (int) (y1 * scale), (int) ((x2T + skew2) * scale),
							(int) (y2 * scale));
				}
			}

			// omega pole
//			System.out.println("OMEGA POLE");
//			System.out.println(activeSounding.getWWind().length);
			if (activeSounding.getWWind().length != 0) {
				g.setStroke(thickStroke);
				g.setColor(new Color(255, 255, 255));

				double x1 = linScale(223.15, 323.15, 0, 800, 273.15 - 45);

				double y1 = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(10000));
				double y2 = linScale(Math.log(10000), Math.log(110000), 0, 800,
						Math.log(activeSounding.getPressureLevels()[activeSounding.getPressureLevels().length - 1]));

//				System.out.println(x1);
//				System.out.println(y1);
//				System.out.println(y2);

				g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x1 * scale), (int) (y2 * scale));

				for (int i = 0; i < activeSounding.getWWind().length; i++) {
					double pressureW = activeSounding.getPressureLevels()[i];
					double omegaW = activeSounding.getWWind()[i];

					double yW = linScale(Math.log(10000), Math.log(110000), 0, 800, Math.log(pressureW));

					double x2 = x1 + linScale(0, -1, 0, 40, omegaW);

					if (omegaW > 1.0) {
						g.setColor(new Color(96, 64, 150));
					} else if (omegaW > 0.25) {
						g.setColor(new Color(128, 128, 255));
					} else if (omegaW > 0) {
						g.setColor(new Color(128, 255, 192));
					} else if (omegaW > -0.25) {
						g.setColor(new Color(255, 192, 64));
					} else if (omegaW > -1.0) {
						g.setColor(new Color(255, 0, 0));
					} else {
						g.setColor(new Color(255, 128, 255));
					}

//					System.out.println(pressureW + " Pa");
//					System.out.println(omegaW + " Pa s^-1");
//					System.out.println(x2 - x1);
//					System.out.println();

					g.drawLine((int) (x1 * scale), (int) (yW * scale), (int) (x2 * scale), (int) (yW * scale));
				}
			}

			return skewT;
		}

		private BufferedImage drawHodograph(double scale) {
			BufferedImage hodograph = new BufferedImage((int) (400 * scale), (int) (400 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = hodograph.createGraphics();
			g.setStroke(thickStroke);

			double[] pressure = activeSounding.getPressureLevels();
			double[] height = activeSounding.getHeight();
			double[] uWind = activeSounding.getUWind();
			double[] vWind = activeSounding.getVWind();

			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
					activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

			// choosing active storm motion vector
			switch (stormMotionVector) {
			case 0:
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 1:
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 2:
				stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 3:
				stormMotion = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 4:
				stormMotion = WeatherUtils.corfidiDownshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 5:
				break;
			}

			// corfidi vectors
			double[] corfidiUpshear = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
					activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
					activeSounding.getUWind(), activeSounding.getVWind());
			double[] corfidiDownshear = WeatherUtils.corfidiDownshear(activeSounding.getPressureLevels(),
					activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
					activeSounding.getUWind(), activeSounding.getVWind());

			// computing maximum radius of hodograph
			double maxRadius = 0; // m s^-1

			assert uWind.length == vWind.length;
			for (int i = 0; i < uWind.length; i++) {
				if (pressure[i] > 20000) {
					double windSpeed = Math.hypot(uWind[i], vWind[i]);
					int windSpeedIntDiv10 = (int) Math.ceil(windSpeed / 5.144444);

					maxRadius = Double.max(maxRadius, windSpeedIntDiv10 * 5.144444);
				}
			}

			{
				double windSpeed = Math.hypot(corfidiDownshear[0], corfidiDownshear[1]);
				int windSpeedIntDiv10 = (int) Math.ceil(windSpeed / 5.144444);

				maxRadius = Double.max(maxRadius, windSpeedIntDiv10 * 5.144444);
			}

			// rings
			g.setColor(new Color(64, 64, 64));
			for (int i = 0; i <= maxRadius; i += 5.144444) {
				double radius = linScale(0, maxRadius, 0, 200, i);
				g.drawOval((int) ((200 - radius) * scale), (int) ((200 - radius) * scale), (int) (2 * radius * scale),
						(int) (2 * radius * scale));
			}

			// crosshair
			g.setColor(new Color(128, 128, 128));
			g.drawLine(0, (int) (200 * scale), (int) (400 * scale), (int) (200 * scale));
			g.drawLine((int) (200 * scale), 0, (int) (200 * scale), (int) (400 * scale));

			// storm motion vectors
			g.setFont(CAPTION_FONT);

			double[] stormMotionActive = stormMotion;

			double eWindActive = Math.cos(-windOffsetAngle) * stormMotionActive[0]
					- Math.sin(-windOffsetAngle) * stormMotionActive[1];
			double nWindActive = Math.sin(-windOffsetAngle) * stormMotionActive[0]
					+ Math.cos(-windOffsetAngle) * stormMotionActive[1];

			double xActive = linScale(-maxRadius, maxRadius, 0, 400, eWindActive);
			double yActive = linScale(-maxRadius, maxRadius, 0, 400, -nWindActive);

			// sub: marking eff inflow lines
			if (inflowLayer[activeReadoutSet][0] != -1024.0) {
				double lowerInflowPressure = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1],
						inflowLayer[activeReadoutSet][0]);
				double upperInflowPressure = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1],
						inflowLayer[activeReadoutSet][1]);

				double lowerInflowU = logInterp(pressure, uWind, lowerInflowPressure);
				double lowerInflowV = logInterp(pressure, vWind, lowerInflowPressure);

				double eLowerInflow = Math.cos(-windOffsetAngle) * lowerInflowU
						- Math.sin(-windOffsetAngle) * lowerInflowV;
				double nLowerInflow = Math.sin(-windOffsetAngle) * lowerInflowU
						+ Math.cos(-windOffsetAngle) * lowerInflowV;

				double xLowerInflow = linScale(-maxRadius, maxRadius, 0, 400, eLowerInflow);
				double yLowerInflow = linScale(-maxRadius, maxRadius, 0, 400, -nLowerInflow);

				double upperInflowU = logInterp(pressure, uWind, upperInflowPressure);
				double upperInflowV = logInterp(pressure, vWind, upperInflowPressure);

				double eUpperInflow = Math.cos(-windOffsetAngle) * upperInflowU
						- Math.sin(-windOffsetAngle) * upperInflowV;
				double nUpperInflow = Math.sin(-windOffsetAngle) * upperInflowU
						+ Math.cos(-windOffsetAngle) * upperInflowV;

				double xUpperInflow = linScale(-maxRadius, maxRadius, 0, 400, eUpperInflow);
				double yUpperInflow = linScale(-maxRadius, maxRadius, 0, 400, -nUpperInflow);

				g.setColor(new Color(64, 128, 128));
				g.drawLine((int) (xActive * scale), (int) (yActive * scale), (int) (xLowerInflow * scale),
						(int) (yLowerInflow * scale));
				g.drawLine((int) (xActive * scale), (int) (yActive * scale), (int) (xUpperInflow * scale),
						(int) (yUpperInflow * scale));
			}

			double[] stormMotionRightMover = WeatherUtils.stormMotionBunkersIDRightMoving(pressure, height, uWind,
					vWind);

			double eWindRight = Math.cos(-windOffsetAngle) * stormMotionRightMover[0]
					- Math.sin(-windOffsetAngle) * stormMotionRightMover[1];
			double nWindRight = Math.sin(-windOffsetAngle) * stormMotionRightMover[0]
					+ Math.cos(-windOffsetAngle) * stormMotionRightMover[1];

			double xRight = linScale(-maxRadius, maxRadius, 0, 400, eWindRight);
			double yRight = linScale(-maxRadius, maxRadius, 0, 400, -nWindRight);

			// mark RM
			drawCenteredOutlinedString("RM", g, (int) (xRight * scale), (int) (yRight * scale),
					new Color(255, 128, 128));

			double[] stormMotionLeftMover = WeatherUtils.stormMotionBunkersIDLeftMoving(pressure, height, uWind, vWind);

			double eWindLeft = Math.cos(-windOffsetAngle) * stormMotionLeftMover[0]
					- Math.sin(-windOffsetAngle) * stormMotionLeftMover[1];
			double nWindLeft = Math.sin(-windOffsetAngle) * stormMotionLeftMover[0]
					+ Math.cos(-windOffsetAngle) * stormMotionLeftMover[1];

			double xLeft = linScale(-maxRadius, maxRadius, 0, 400, eWindLeft);
			double yLeft = linScale(-maxRadius, maxRadius, 0, 400, -nWindLeft);

			drawCenteredOutlinedString("LM", g, (int) (xLeft * scale), (int) (yLeft * scale), new Color(128, 128, 255));

			double[] stormMotionMeanWind = WeatherUtils.stormMotionBunkersIDMeanWindComponent(pressure, height, uWind,
					vWind);

			double eWindMw = Math.cos(-windOffsetAngle) * stormMotionMeanWind[0]
					- Math.sin(-windOffsetAngle) * stormMotionMeanWind[1];
			double nWindMw = Math.sin(-windOffsetAngle) * stormMotionMeanWind[0]
					+ Math.cos(-windOffsetAngle) * stormMotionMeanWind[1];

			double xMw = linScale(-maxRadius, maxRadius, 0, 400, eWindMw);
			double yMw = linScale(-maxRadius, maxRadius, 0, 400, -nWindMw);

			drawCenteredOutlinedString("MW", g, (int) (xMw * scale), (int) (yMw * scale), new Color(128, 255, 128));

			double[] devTorMotion = WeatherUtils.deviantTornadoMotion(pressure, height, uWind, vWind, stormMotion);

			double eWindDtm = Math.cos(-windOffsetAngle) * devTorMotion[0]
					- Math.sin(-windOffsetAngle) * devTorMotion[1];
			double nWindDtm = Math.sin(-windOffsetAngle) * devTorMotion[0]
					+ Math.cos(-windOffsetAngle) * devTorMotion[1];

			double xDtm = linScale(-maxRadius, maxRadius, 0, 400, eWindDtm);
			double yDtm = linScale(-maxRadius, maxRadius, 0, 400, -nWindDtm);

			drawCenteredOutlinedString("DTM", g, (int) (xDtm * scale), (int) (yDtm * scale), new Color(255, 128, 255));

			if(mucape[activeReadoutSet] > 0) {
				double eWindUp = Math.cos(-windOffsetAngle) * corfidiUpshear[0]
						- Math.sin(-windOffsetAngle) * corfidiUpshear[1];
				double nWindUp = Math.sin(-windOffsetAngle) * corfidiUpshear[0]
						+ Math.cos(-windOffsetAngle) * corfidiUpshear[1];
	
				double xUp = linScale(-maxRadius, maxRadius, 0, 400, eWindUp);
				double yUp = linScale(-maxRadius, maxRadius, 0, 400, -nWindUp);
	
				drawCenteredOutlinedString("UP", g, (int) (xUp * scale), (int) (yUp * scale), new Color(192, 192, 192));
	
				double eWindDn = Math.cos(-windOffsetAngle) * corfidiDownshear[0]
						- Math.sin(-windOffsetAngle) * corfidiDownshear[1];
				double nWindDn = Math.sin(-windOffsetAngle) * corfidiDownshear[0]
						+ Math.cos(-windOffsetAngle) * corfidiDownshear[1];
	
				double xDn = linScale(-maxRadius, maxRadius, 0, 400, eWindDn);
				double yDn = linScale(-maxRadius, maxRadius, 0, 400, -nWindDn);
	
				drawCenteredOutlinedString("DN", g, (int) (xDn * scale), (int) (yDn * scale), new Color(192, 192, 192));
			}

			// trace
//			System.out.println(windOffsetAngle);
			for (int i = 0; i < uWind.length - 1; i++) {
				double uWind1 = uWind[i];
				double vWind1 = -vWind[i];
				double uWind2 = uWind[i + 1];
				double vWind2 = -vWind[i + 1];

				double eWind1 = Math.cos(windOffsetAngle) * uWind1 - Math.sin(windOffsetAngle) * vWind1;
				double nWind1 = Math.sin(windOffsetAngle) * uWind1 + Math.cos(windOffsetAngle) * vWind1;
				double eWind2 = Math.cos(windOffsetAngle) * uWind2 - Math.sin(windOffsetAngle) * vWind2;
				double nWind2 = Math.sin(windOffsetAngle) * uWind2 + Math.cos(windOffsetAngle) * vWind2;

				int x1 = (int) linScale(-maxRadius, maxRadius, 0, 400, eWind1);
				int y1 = (int) linScale(-maxRadius, maxRadius, 0, 400, nWind1);
				int x2 = (int) linScale(-maxRadius, maxRadius, 0, 400, eWind2);
				int y2 = (int) linScale(-maxRadius, maxRadius, 0, 400, nWind2);

				double heightAGL1 = (height[i]) - height[height.length - 1];
				double heightAGL2 = (height[i + 1]) - height[height.length - 1];

				double surfacePressure = pressure[pressure.length - 1];

				if (heightAGL1 < 500 && heightAGL2 < 500) {
					g.setColor(new Color(255, 127, 255));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 >= 500 && heightAGL2 < 500) {
					double uWindI = logInterp(pressure, uWind, WeatherUtils.pressureAtHeight(surfacePressure, 500));
					double vWindI = -logInterp(pressure, vWind, WeatherUtils.pressureAtHeight(surfacePressure, 500));

					double eWindI = Math.cos(windOffsetAngle) * uWindI - Math.sin(windOffsetAngle) * vWindI;
					double nWindI = Math.sin(windOffsetAngle) * uWindI + Math.cos(windOffsetAngle) * vWindI;

					int xI = (int) linScale(-maxRadius, maxRadius, 0, 400, eWindI);
					int yI = (int) linScale(-maxRadius, maxRadius, 0, 400, nWindI);

					g.setColor(new Color(255, 0, 0));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (xI * scale), (int) (yI * scale));

					g.setColor(new Color(255, 127, 255));
					g.drawLine((int) (xI * scale), (int) (yI * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 < 1000 && heightAGL2 < 1000) {
					g.setColor(new Color(255, 0, 0));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 >= 1000 && heightAGL2 < 1000) {
					double uWindI = logInterp(pressure, uWind, WeatherUtils.pressureAtHeight(surfacePressure, 1000));
					double vWindI = -logInterp(pressure, vWind, WeatherUtils.pressureAtHeight(surfacePressure, 1000));

					double eWindI = Math.cos(windOffsetAngle) * uWindI - Math.sin(windOffsetAngle) * vWindI;
					double nWindI = Math.sin(windOffsetAngle) * uWindI + Math.cos(windOffsetAngle) * vWindI;

					int xI = (int) linScale(-maxRadius, maxRadius, 0, 400, eWindI);
					int yI = (int) linScale(-maxRadius, maxRadius, 0, 400, nWindI);

					g.setColor(new Color(255, 127, 0));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (xI * scale), (int) (yI * scale));

					g.setColor(new Color(255, 0, 0));
					g.drawLine((int) (xI * scale), (int) (yI * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 < 3000 && heightAGL2 < 3000) {
					g.setColor(new Color(255, 127, 0));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 >= 3000 && heightAGL2 < 3000) {
					double uWindI = logInterp(pressure, uWind, WeatherUtils.pressureAtHeight(surfacePressure, 3000));
					double vWindI = -logInterp(pressure, vWind, WeatherUtils.pressureAtHeight(surfacePressure, 3000));

					double eWindI = Math.cos(windOffsetAngle) * uWindI - Math.sin(windOffsetAngle) * vWindI;
					double nWindI = Math.sin(windOffsetAngle) * uWindI + Math.cos(windOffsetAngle) * vWindI;

					int xI = (int) linScale(-maxRadius, maxRadius, 0, 400, eWindI);
					int yI = (int) linScale(-maxRadius, maxRadius, 0, 400, nWindI);

					g.setColor(new Color(255, 255, 127));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (xI * scale), (int) (yI * scale));

					g.setColor(new Color(255, 127, 0));
					g.drawLine((int) (xI * scale), (int) (yI * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 < 6000 && heightAGL2 < 6000) {
					g.setColor(new Color(255, 255, 127));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 >= 6000 && heightAGL2 < 6000) {
					double uWindI = logInterp(pressure, uWind, WeatherUtils.pressureAtHeight(surfacePressure, 6000));
					double vWindI = -logInterp(pressure, vWind, WeatherUtils.pressureAtHeight(surfacePressure, 6000));

					double eWindI = Math.cos(windOffsetAngle) * uWindI - Math.sin(windOffsetAngle) * vWindI;
					double nWindI = Math.sin(windOffsetAngle) * uWindI + Math.cos(windOffsetAngle) * vWindI;

					int xI = (int) linScale(-maxRadius, maxRadius, 0, 400, eWindI);
					int yI = (int) linScale(-maxRadius, maxRadius, 0, 400, nWindI);

					g.setColor(new Color(0, 192, 0));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (xI * scale), (int) (yI * scale));

					g.setColor(new Color(255, 255, 127));
					g.drawLine((int) (xI * scale), (int) (yI * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 < 9000 && heightAGL2 < 9000) {
					g.setColor(new Color(0, 192, 0));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else if (heightAGL1 >= 9000 && heightAGL2 < 9000) {
					double uWindI = logInterp(pressure, uWind, WeatherUtils.pressureAtHeight(surfacePressure, 9000));
					double vWindI = -logInterp(pressure, vWind, WeatherUtils.pressureAtHeight(surfacePressure, 9000));

					double eWindI = Math.cos(windOffsetAngle) * uWindI - Math.sin(windOffsetAngle) * vWindI;
					double nWindI = Math.sin(windOffsetAngle) * uWindI + Math.cos(windOffsetAngle) * vWindI;

					int xI = (int) linScale(-maxRadius, maxRadius, 0, 400, eWindI);
					int yI = (int) linScale(-maxRadius, maxRadius, 0, 400, nWindI);

					g.setColor(new Color(128, 192, 255));
					g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (xI * scale), (int) (yI * scale));

					g.setColor(new Color(0, 192, 0));
					g.drawLine((int) (xI * scale), (int) (yI * scale), (int) (x2 * scale), (int) (y2 * scale));
				} else {
					g.setColor(new Color(128, 192, 255));

					if (pressure[i + 1] > 20000) {
						g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
					}
				}
			}

			double[] heightRev = new double[vWind.length];
			double[] uWindRev = new double[vWind.length];
			double[] vWindRev = new double[vWind.length];
			for (int i = 0; i < heightRev.length; i++) {
				heightRev[heightRev.length - 1 - i] = height[i];
				uWindRev[heightRev.length - 1 - i] = uWind[i];
				vWindRev[heightRev.length - 1 - i] = vWind[i];
			}

			double surfaceHeight = heightRev[0];

			double[] heights = { 500, 1000, 3000, 6000, 9000 };
			String[] labels = { ".5", "1", "3", "6", "9" };
			for (int i = 0; i < heights.length; i++) {
//				double markerU = logInterp(pressure, uWind,
//						WeatherUtils.pressureAtHeight(pressure[pressure.length - 1], heights[i]));
//				double markerV = logInterp(pressure, vWind,
//						WeatherUtils.pressureAtHeight(pressure[pressure.length - 1], heights[i]));

				double markerU = linearInterp(heightRev, uWindRev, heights[i] + surfaceHeight);
				double markerV = linearInterp(heightRev, vWindRev, heights[i] + surfaceHeight);

//				System.out.println(Arrays.toString(heightRev));
//				System.out.println(heights[i]);
//				System.out.println(markerU);
//				System.out.println(markerV);

				double markerE = Math.cos(-windOffsetAngle) * markerU - Math.sin(-windOffsetAngle) * markerV;
				double markerN = Math.sin(-windOffsetAngle) * markerU + Math.cos(-windOffsetAngle) * markerV;

				double markerX = linScale(-maxRadius, maxRadius, 0, 400, markerE);
				double markerY = linScale(-maxRadius, maxRadius, 0, 400, -markerN);

				drawCenteredOutlinedString(labels[i], g, (int) (scale * markerX), (int) (scale * markerY), Color.WHITE);
			}

			return hodograph;
		}

		private BufferedImage drawSevereReadouts(double scale) {
			BufferedImage severeReadouts = new BufferedImage((int) (400 * scale), (int) (375 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = severeReadouts.createGraphics();

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * scale)));

//			System.out.println("MU-LPP: " + muLplPressure);                        
//			System.out.println("MU-LPL: " + muLpl);
			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "", "SB", "ML-50MB", "ML-100MB", "MU"),
					(int) (45 * scale), (int) (15 * scale));
			g.drawString(String.format("%-9s%-9d%-9d%-9d%-9d", "CAPE", (int) sbcape[activeReadoutSet],
					(int) ml50cape[activeReadoutSet], (int) ml100cape[activeReadoutSet],
					(int) mucape[activeReadoutSet]), (int) (45 * scale), (int) (30 * scale));

			String sbcinh_ = (sbcape[activeReadoutSet] > 0) ? String.valueOf((int) sbcinh[activeReadoutSet]) : "-";
			String ml50cinh_ = (ml50cape[activeReadoutSet] > 0) ? String.valueOf((int) ml50cinh[activeReadoutSet])
					: "-";
			String ml100cinh_ = (ml100cape[activeReadoutSet] > 0) ? String.valueOf((int) ml100cinh[activeReadoutSet])
					: "-";
			String mucinh_ = (mucape[activeReadoutSet] > 0) ? String.valueOf((int) mucinh[activeReadoutSet]) : "-";

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "CINH", sbcinh_, ml50cinh_, ml100cinh_, mucinh_),
					(int) (45 * scale), (int) (45 * scale));

			String sbLcl_ = String.format("%5.2f", sbLcl[activeReadoutSet] / 1000.0);
			String ml50Lcl_ = String.format("%5.2f", ml50Lcl[activeReadoutSet] / 1000.0);
			String ml100Lcl_ = String.format("%5.2f", ml100Lcl[activeReadoutSet] / 1000.0);
			String muLcl_ = String.format("%5.2f", (muLcl[activeReadoutSet] + muLpl[activeReadoutSet]) / 1000.0);

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "LCL [km]", sbLcl_, ml50Lcl_, ml100Lcl_, muLcl_),
					(int) (45 * scale), (int) (60 * scale));

			String sbCcl_ = String.format("%5.2f", sbCcl[activeReadoutSet] / 1000.0);
			String ml50Ccl_ = String.format("%5.2f", ml50Ccl[activeReadoutSet] / 1000.0);
			String ml100Ccl_ = String.format("%5.2f", ml100Ccl[activeReadoutSet] / 1000.0);
			String muCcl_ = String.format("%5.2f", (muCcl[activeReadoutSet] + muLpl[activeReadoutSet]) / 1000.0);

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "CCL [km]", sbCcl_, ml50Ccl_, ml100Ccl_, muCcl_),
					(int) (45 * scale), (int) (75 * scale));

			String sbLfc_ = (sbcape[activeReadoutSet] > 0) ? String.format("%5.2f", sbLfc[activeReadoutSet] / 1000.0)
					: "-";
			String ml50Lfc_ = (ml50cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml50Lfc[activeReadoutSet] / 1000.0)
					: "-";
			String ml100Lfc_ = (ml100cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml100Lfc[activeReadoutSet] / 1000.0)
					: "-";
			String muLfc_ = (mucape[activeReadoutSet] > 0)
					? String.format("%5.2f", (muLfc[activeReadoutSet] + muLpl[activeReadoutSet]) / 1000.0)
					: "-";

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "LFC [km]", sbLfc_, ml50Lfc_, ml100Lfc_, muLfc_),
					(int) (45 * scale), (int) (90 * scale));

			String sbEl_ = (sbcape[activeReadoutSet] > 0) ? String.format("%5.2f", sbEl[activeReadoutSet] / 1000.0)
					: "-";
			String ml50El_ = (ml50cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml50El[activeReadoutSet] / 1000.0)
					: "-";
			String ml100El_ = (ml100cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml100El[activeReadoutSet] / 1000.0)
					: "-";
			String muEl_ = (mucape[activeReadoutSet] > 0)
					? String.format("%5.2f", (muEl[activeReadoutSet] + muLpl[activeReadoutSet]) / 1000.0)
					: "-";

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "EL [km]", sbEl_, ml50El_, ml100El_, muEl_),
					(int) (45 * scale), (int) (105 * scale));

			String stormMotionVectorText = "";

			switch (stormMotionVector) {
			case 0:
				stormMotionVectorText = "[LM]";
				break;
			case 1:
				stormMotionVectorText = "[MW]";
				break;
			case 2:
				stormMotionVectorText = "[RM]";
				break;
			case 3:
				stormMotionVectorText = "[UP]";
				break;
			case 4:
				stormMotionVectorText = "[DN]";
				break;
			case 5:
				stormMotionVectorText = "[USER]";
				break;
			}

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", stormMotionVectorText, "0-500 m", "0-1 km", "0-3 km",
					"INFLOW"), (int) (45 * scale), (int) (135 * scale));

			String srh0_500_ = String.format("%6d", (int) srh0_500[activeReadoutSet]);
			String srh0_1_ = String.format("%6d", (int) srh0_1[activeReadoutSet]);
			String srh0_3_ = String.format("%6d", (int) srh0_3[activeReadoutSet]);
			String srhInflow_ = String.format("%6d", (int) srhInflow[activeReadoutSet]);

			srhInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : srhInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SRH", srh0_500_, srh0_1_, srh0_3_, srhInflow_),
					(int) (45 * scale), (int) (150 * scale));

			String srw0_500_ = windReading(srw0_500[activeReadoutSet], -windOffsetAngle);
			String srwLm0_1_ = windReading(srw0_1[activeReadoutSet], -windOffsetAngle);
			String srwLm0_3_ = windReading(srw0_3[activeReadoutSet], -windOffsetAngle);
			String srwInflow_ = windReading(srwInflow[activeReadoutSet], -windOffsetAngle);

			srwInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : srwInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SRW [kt]", srw0_500_, srwLm0_1_, srwLm0_3_, srwInflow_),
					(int) (45 * scale), (int) (165 * scale));

			String bulkShear0_500_ = String.format("%6.1f", bulkShear0_500[activeReadoutSet] / 0.5144444);
			String bulkShear0_1_ = String.format("%6.1f", bulkShear0_1[activeReadoutSet] / 0.5144444);
			String bulkShear0_3_ = String.format("%6.1f", bulkShear0_3[activeReadoutSet] / 0.5144444);
			String bulkShearInflow_ = String.format("%6.1f", bulkShearInflow[activeReadoutSet] / 0.5144444);

			bulkShearInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : bulkShearInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SHEAR", bulkShear0_500_, bulkShear0_1_, bulkShear0_3_,
					bulkShearInflow_), (int) (45 * scale), (int) (180 * scale));

			String streamwiseVort0_500_ = String.format("%6.3f", streamwiseVort0_500[activeReadoutSet]);
			String streamwiseVort0_1_ = String.format("%6.3f", streamwiseVort0_1[activeReadoutSet]);
			String streamwiseVort0_3_ = String.format("%6.3f", streamwiseVort0_3[activeReadoutSet]);
			String streamwiseVortInflow_ = String.format("%6.3f", streamwiseVortInflow[activeReadoutSet]);

			streamwiseVortInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : streamwiseVortInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SWV", streamwiseVort0_500_, streamwiseVort0_1_,
					streamwiseVort0_3_, streamwiseVortInflow_), (int) (45 * scale), (int) (195 * scale));

			String streamwiseness0_500_ = String.format("%5.1f", 100 * streamwiseness0_500[activeReadoutSet]) + "%";
			String streamwiseness0_1_ = String.format("%5.1f", 100 * streamwiseness0_1[activeReadoutSet]) + "%";
			String streamwiseness0_3_ = String.format("%5.1f", 100 * streamwiseness0_3[activeReadoutSet]) + "%";
			String streamwisenessInflow_ = String.format("%5.1f", 100 * streamwisenessInflow[activeReadoutSet]) + "%";

			streamwisenessInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : streamwisenessInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SW%", streamwiseness0_500_, streamwiseness0_1_,
					streamwiseness0_3_, streamwisenessInflow_), (int) (45 * scale), (int) (210 * scale));

			String threeCapeSb_ = String.format("%4d", (int) threeCapeSb[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "3CAPE-SB", threeCapeSb_), (int) (45 * scale), (int) (240 * scale));

			String threeCapeMl_ = String.format("%4d", (int) threeCapeMl[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "3CAPE-ML", threeCapeMl_), (int) (45 * scale), (int) (255 * scale));

			String dcape_ = String.format("%4d", (int) dcape[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "DCAPE", dcape_), (int) (45 * scale), (int) (270 * scale));

			String ebwd_ = String.format("%4.1f", ebwd[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "EBWD", ebwd_), (int) (45 * scale), (int) (285 * scale));

			String scp_ = String.format("%4.1f", scp[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "SCP", scp_), (int) (45 * scale), (int) (315 * scale));

			String stp_ = String.format("%4.1f", stpE[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "SIGTOR", stp_), (int) (45 * scale), (int) (330 * scale));

			String ship_ = String.format("%4.1f", ship[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "SHIP", ship_), (int) (45 * scale), (int) (345 * scale));

//			System.out.println(Arrays.toString(WeatherUtils.deviantTornadoMotion(
//					activeSounding.getPressureLevels(), activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion)));

			String devtor_ = String.format("%4.1f", devtor[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "DEVTOR", devtor_), (int) (45 * scale), (int) (360 * scale));

			String pwat_ = String.format("%4.1f", pwat[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "PWAT-mm", pwat_), (int) (245 * scale), (int) (240 * scale));

			String pwatIn_ = String.format("%4.2f", pwat[activeReadoutSet] / 25.4);

			g.drawString(String.format("%-9s%-9s", "PWAT-in", pwatIn_), (int) (245 * scale), (int) (255 * scale));

			g.drawString(String.format("%-9s%-9s", "", "OMEGA"), (int) (245 * scale), (int) (285 * scale));

			String omega0_1_ = (activeSounding.getWWind().length > 0)
					? String.format("%4.2f", omega0_1[activeReadoutSet])
					: "-";

			g.drawString(String.format("%-9s%-9s", "0-1 km", omega0_1_), (int) (245 * scale), (int) (300 * scale));

			String omega1_3_ = (activeSounding.getWWind().length > 0)
					? String.format("%4.2f", omega1_3[activeReadoutSet])
					: "-";

			g.drawString(String.format("%-9s%-9s", "1-3 km", omega1_3_), (int) (245 * scale), (int) (315 * scale));

			String omega0_3_ = (activeSounding.getWWind().length > 0)
					? String.format("%4.2f", omega0_3[activeReadoutSet])
					: "-";

			g.drawString(String.format("%-9s%-9s", "0-3 km", omega0_3_), (int) (245 * scale), (int) (330 * scale));

			String omegaCap_ = (activeSounding.getWWind().length > 0 && ml100cape[activeReadoutSet] > 0)
					? String.format("%4.2f", omegaCap[activeReadoutSet])
					: "-";

			g.drawString(String.format("%-9s%-9s", "CAP", omegaCap_), (int) (245 * scale), (int) (345 * scale));

			String omegaInflow_ = (activeSounding.getWWind().length > 0 && inflowLayer[activeReadoutSet][0] > -1023.0)
					? String.format("%4.2f", omegaInflow[activeReadoutSet])
					: "-";

			g.drawString(String.format("%-9s%-9s", "INFLOW", omegaInflow_), (int) (245 * scale), (int) (360 * scale));

			return severeReadouts;
		}

		private BufferedImage drawWinterReadouts(double scale) {
			BufferedImage winterReadouts = new BufferedImage((int) (187.5 * scale), (int) (175 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = winterReadouts.createGraphics();

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * scale)));

//			drawCenteredString("WINTER READOUTS", g, (int) (93.75 * scale), (int) (87.5 * scale));

			String frzLvl = String.format("%4d",
					(int) (freezingLevel[activeReadoutSet] > -1023 ? freezingLevel[activeReadoutSet] : 0));

			g.drawString(String.format("%-11s%-4s m", "0°C LEVEL", frzLvl), (int) (5 * scale), (int) (15 * scale));

			String dgzDepth = String.format("%4d",
					(int) (dgzLayer[activeReadoutSet][1] - dgzLayer[activeReadoutSet][0]));
			String dgzTop = String.format("%4d", (int) (dgzLayer[activeReadoutSet][1]));
			String dgzBtm = String.format("%4d", (int) (dgzLayer[activeReadoutSet][0]));

			g.drawString(String.format("%-11s%-4s m", "DGZ DEPTH", dgzDepth), (int) (5 * scale), (int) (30 * scale));
			g.drawString(String.format("%-6s(%-4s m - %-4s m)", "", dgzBtm, dgzTop), (int) (5 * scale),
					(int) (45 * scale));

			String dgzOmega_ = (activeSounding.getWWind().length > 0
					? String.format("%5.2f Pa/s", dgzOmega[activeReadoutSet])
					: "-");
			g.drawString(String.format("%-11s%-13s", "DGZ OMEGA", dgzOmega_), (int) (5 * scale), (int) (60 * scale));

			String dgzRH_ = String.format("%4.1f", (100.0 * dgzRH[activeReadoutSet]));
			g.drawString(String.format("%-11s%-4s", "DGZ RH", dgzRH_) + " %", (int) (5 * scale), (int) (75 * scale));

			g.drawString(String.format("%-11s%-11s", "ALGORITHM", "P-TYPE"), (int) (5 * scale), (int) (95 * scale));

			// https://journals.ametsoc.org/view/journals/apme/61/9/JAMC-D-21-0202.1.xml?tab_body=pdf
			// contains description of both cantin/bachand and ramer methods
			g.drawString(String.format("%-11s", "CANTIN"), (int) (5 * scale), (int) (110 * scale));
			g.drawString(String.format("%-11s", "RAMER"), (int) (5 * scale), (int) (125 * scale));
			g.drawString(String.format("%-11s", "BOURGOUIN"), (int) (5 * scale), (int) (140 * scale));
			g.drawString(String.format("%-11s", "BG REVISED"), (int) (5 * scale), (int) (155 * scale));
			g.drawString(String.format("%-11s", "BG REV EXT"), (int) (5 * scale), (int) (170 * scale));

			int ptypeOffset = getFontMetrics(g.getFont()).stringWidth(String.format("%-11s", ""));

			PrecipitationType cantin = cantinBachand[activeReadoutSet];
			g.setColor(ptypeColors.get(cantin));
			g.drawString(String.format("%-11s", ptypeNames.get(cantin)).toUpperCase(),
					(int) (5 * scale) + ptypeOffset, (int) (110 * scale));
			
			PrecipitationType rmr = ramer[activeReadoutSet];
			g.setColor(ptypeColors.get(rmr));
			g.drawString(String.format("%-11s", ptypeNames.get(rmr)).toUpperCase(),
					(int) (5 * scale) + ptypeOffset, (int) (125 * scale));
			
			PrecipitationType brgn = bourgouin[activeReadoutSet];
			g.setColor(ptypeColors.get(brgn));
			g.drawString(String.format("%-11s", ptypeNames.get(brgn)).toUpperCase(),
					(int) (5 * scale) + ptypeOffset, (int) (140 * scale));
			
			PrecipitationType bgRevised = bourgouinRevised[activeReadoutSet];
			g.setColor(ptypeColors.get(bgRevised));
			g.drawString(String.format("%-11s", ptypeNames.get(bgRevised)).toUpperCase(),
					(int) (5 * scale) + ptypeOffset, (int) (155 * scale));

			PrecipitationType bgRevExt = bourgouinRevExt[activeReadoutSet];
			g.setColor(ptypeColors.get(bgRevExt));
			g.drawString(String.format("%-11s", ptypeNames.get(bgRevExt)).toUpperCase(),
					(int) (5 * scale) + ptypeOffset, (int) (170 * scale));
			
//			System.out.println(cantin);
//			System.out.println(rmr);
//			System.out.println(brgn);
//			System.out.println(bgRevised);
//			System.out.println(bgRevExt);

			return winterReadouts;
		}

		private BufferedImage drawVorticityPlot(double scale) {
			BufferedImage vorticityPlot = new BufferedImage((int) (187.5 * scale), (int) (175 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = vorticityPlot.createGraphics();
			g.setStroke(thickStroke);

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * scale)));
//			
//			drawCenteredString("STREAMWISE VORTICITY", g, (int) (93.75 * scale), (int) (87.5 * scale));

			// choosing active storm motion vector
			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
					activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

			switch (stormMotionVector) {
			case 0:
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 1:
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 2:
				break;
			case 3:
				stormMotion = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 4:
				stormMotion = WeatherUtils.corfidiDownshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
			case 5:
				break;
			}

			double[] height = activeSounding.getHeight();
			double[] uWind = activeSounding.getUWind();
			double[] vWind = activeSounding.getVWind();

			double[] totalVorticity = new double[activeSounding.getUWind().length];
			double[] streamwiseVorticity = new double[activeSounding.getUWind().length];

			for (int i = 0; i < uWind.length; i++) {
				int iB = i - 1;
				int iA = i + 1;

				if (iB < 0)
					iB = 0;
				if (iA >= uWind.length)
					iA = uWind.length - 1;

				double heightB = height[iB];
				double uWindB = uWind[iB];
				double vWindB = vWind[iB];

				double heightC = height[i];
				double uWindC = uWind[i];
				double vWindC = vWind[i];

				double heightA = height[iA];
				double uWindA = uWind[iA];
				double vWindA = vWind[iA];

				double vortU = 0.0;
				double vortV = 0.0;

				double stormInflowU = uWindC - stormMotion[0];
				double stormInflowV = vWindC - stormMotion[1];

				double stormInflowNormU = stormInflowU / Math.hypot(stormInflowU, stormInflowV);
				double stormInflowNormV = stormInflowV / Math.hypot(stormInflowU, stormInflowV);

				if (heightC == heightB) {
					double vortU_A = -(vWindC - vWindA) / (heightC - heightA);
					double vortV_A = (uWindC - uWindA) / (heightC - heightA);

					vortU = vortU_A;
					vortV = vortV_A;
				} else if (heightA == heightC) {
					double vortU_B = -(vWindB - vWindC) / (heightB - heightC);
					double vortV_B = (uWindB - uWindC) / (heightB - heightC);

					vortU = vortU_B;
					vortV = vortV_B;
				} else {
					double vortU_B = -(vWindB - vWindC) / (heightB - heightC);
					double vortV_B = (uWindB - uWindC) / (heightB - heightC);

					double vortU_A = -(vWindC - vWindA) / (heightC - heightA);
					double vortV_A = (uWindC - uWindA) / (heightC - heightA);

					double weight1 = (heightB - heightC) / (heightB - heightA);
					double weight2 = (heightC - heightA) / (heightB - heightA);

					vortU = weight1 * vortU_B + weight2 * vortU_A;
					vortV = weight1 * vortV_B + weight2 * vortV_A;
				}

				double vortNormU = vortU / Math.hypot(vortU, vortV);
				double vortNormV = vortV / Math.hypot(vortU, vortV);

				double streamwiseness = vortNormU * stormInflowNormU + vortNormV * stormInflowNormV;

				totalVorticity[i] = Math.hypot(vortU, vortV);
				streamwiseVorticity[i] = totalVorticity[i] * streamwiseness;
			}

			double totalVorticityMaxTick = 0.0;
			double totalVorticityTickIntv = 0.0;

			double totalVorticityMax = 0.0;

			for (int i = 0; i < totalVorticity.length; i++) {
				if (i == 0) {
					totalVorticityMax = Double.max(totalVorticityMax, totalVorticity[i]);
				} else {
					if (height[i - 1] - height[i] > 100) {
						totalVorticityMax = Double.max(totalVorticityMax, totalVorticity[i]);
					}
				}
			}

			if (totalVorticityMax < 0.001) {
				totalVorticityTickIntv = 0.0002;
			} else if (totalVorticityMax < 0.03) {
				totalVorticityTickIntv = 0.005;
			} else if (totalVorticityMax < 0.006) {
				totalVorticityTickIntv = 0.001;
			} else if (totalVorticityMax < 0.01) {
				totalVorticityTickIntv = 0.002;
			} else if (totalVorticityMax < 0.03) {
				totalVorticityTickIntv = 0.005;
			} else if (totalVorticityMax < 0.06) {
				totalVorticityTickIntv = 0.01;
			} else if (totalVorticityMax < 0.1) {
				totalVorticityTickIntv = 0.02;
			} else {
				totalVorticityTickIntv = 0.05;
			}

			totalVorticityMaxTick = Math.ceil(totalVorticityMax / totalVorticityTickIntv) * totalVorticityTickIntv;

			// draw total vorticity trace
			g.setColor(new Color(128, 128, 128));
			for (int i = 0; i < totalVorticity.length - 1; i++) {
				double x1 = linScale(0, totalVorticityMaxTick, 0, 187.5, totalVorticity[i]);
				double x2 = linScale(0, totalVorticityMaxTick, 0, 187.5, totalVorticity[i + 1]);
				double y1 = linScale(0, 3000, 175, 0, height[i] - height[height.length - 1]);
				double y2 = linScale(0, 3000, 175, 0, height[i + 1] - height[height.length - 1]);

				g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
			}
			drawRightAlignedString("TOTAL HORIZ. VORT.", g, (int) (182.5 * scale), (int) (45 * scale));

			// draw streamwise vorticity trace
			g.setColor(new Color(255, 0, 0));
			for (int i = 0; i < streamwiseVorticity.length - 1; i++) {
				double x1 = linScale(0, totalVorticityMaxTick, 0, 187.5, streamwiseVorticity[i]);
				double x2 = linScale(0, totalVorticityMaxTick, 0, 187.5, streamwiseVorticity[i + 1]);
				double y1 = linScale(0, 3000, 175, 0, height[i] - height[height.length - 1]);
				double y2 = linScale(0, 3000, 175, 0, height[i + 1] - height[height.length - 1]);

				g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
			}
			drawRightAlignedString("STREAMWISE VORT.", g, (int) (182.5 * scale), (int) (15 * scale));

			// draw anti-streamwise vorticity trace
			g.setColor(new Color(64, 64, 255));
			for (int i = 0; i < streamwiseVorticity.length - 1; i++) {
				double x1 = linScale(0, totalVorticityMaxTick, 0, 187.5, -streamwiseVorticity[i]);
				double x2 = linScale(0, totalVorticityMaxTick, 0, 187.5, -streamwiseVorticity[i + 1]);
				double y1 = linScale(0, 3000, 175, 0, height[i] - height[height.length - 1]);
				double y2 = linScale(0, 3000, 175, 0, height[i + 1] - height[height.length - 1]);

				g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
			}
			drawRightAlignedString("ANTISTREAMWISE VORT.", g, (int) (182.5 * scale), (int) (30 * scale));

			// draw y axis ticks
			g.setColor(Color.WHITE);
			for (double z = 0.5; z < 3; z += 0.5) {
				double y = linScale(0, 3, 175, 0, z);
				double x = 10;

				if (z % 1 == 0) {
					drawCenteredString(String.valueOf((int) z), g, (int) ((x + 5) * scale), (int) (y * scale));
				}

				g.drawLine(0, (int) (y * scale), (int) (x * scale), (int) (y * scale));
			}

			// draw x axis ticks
			g.setStroke(thickStroke);
			for (double v = totalVorticityTickIntv; v < totalVorticityMaxTick; v += totalVorticityTickIntv) {
				double x = linScale(0, totalVorticityMaxTick, 0, 187.5, v);
				double y1 = 165;
				double y2 = 175;

				if (v <= 2 * totalVorticityMaxTick) {
					drawCenteredString(String.format("%3.3f", v).substring(1), g, (int) ((x) * scale),
							(int) ((y1 - 5) * scale));
				}

				g.drawLine((int) (x * scale), (int) (y1 * scale), (int) (x * scale), (int) (y2 * scale));
			}

			return vorticityPlot;
		}

		private BufferedImage drawStormSlinky(double scale) {
			BufferedImage stormSlinky = new BufferedImage((int) (187.5 * scale), (int) (175 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = stormSlinky.createGraphics();

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * scale)));
			g.setStroke(thinStroke);

			g.setColor(new Color(128, 128, 128));
			g.drawLine((int) (0 * scale), (int) (87.5 * scale), (int) (187.5 * scale), (int) (87.5 * scale));
			g.drawLine((int) (93.75 * scale), (int) (0 * scale), (int) (93.75 * scale), (int) (175 * scale));

			g.setColor(new Color(255, 255, 255));
			drawCenteredString("STORM SLINKY", g, (int) (93.25 * scale), (int) (168 * scale));

			if (mucape[activeReadoutSet] <= 0)
				return stormSlinky;

			g.setColor(new Color(128, 128, 128));

//			drawCenteredString("STORM SLINKY", g, (int) (93.75 * scale), (int) (87.5 * scale));
			double[] pressure = activeSounding.getPressureLevels();
			double[] height = activeSounding.getHeight();
			double[] temperature = activeSounding.getTemperature();
			double[] dewpoint = activeSounding.getDewpoint();
			double[] uWind = activeSounding.getUWind();
			double[] vWind = activeSounding.getVWind();

			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
					activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

			switch (stormMotionVector) {
			case 0:
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(pressure, height, uWind, vWind);
				break;
			case 1:
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(pressure, height, uWind, vWind);
				break;
			case 2:
				break;
			case 3:
				stormMotion = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
				break;
			case 4:
				stormMotion = WeatherUtils.corfidiDownshear(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind());
			case 5:
				break;
			}

			// storm motion
			double stormMotionMag = Math.hypot(stormMotion[0], stormMotion[1]);
			double stormMotionNormU = stormMotion[0] / stormMotionMag;
			double stormMotionNormV = stormMotion[1] / stormMotionMag;

			double x1 = 93.25;
			double y1 = 87.5;
			double x2 = x1 + 20 * stormMotionNormU;
			double y2 = y1 - 20 * stormMotionNormV;

			g.setStroke(thickStroke);
			g.setColor(new Color(255, 255, 255));
			g.drawLine((int) (x1 * scale), (int) (y1 * scale), (int) (x2 * scale), (int) (y2 * scale));
			g.setStroke(thinStroke);

			double[] parcelPressure = new double[parcelPathMostUnstable[activeReadoutSet].size()];
			double[] parcelHeight = new double[parcelPathMostUnstable[activeReadoutSet].size()];
			double[] parcelTemperature = new double[parcelPathMostUnstable[activeReadoutSet].size()];
			double[] parcelDewpoint = new double[parcelPathMostUnstable[activeReadoutSet].size()];
			double[] parcelVirtualTemperature = new double[parcelPathMostUnstable[activeReadoutSet].size()];

			for (int i = 0; i < parcelPathMostUnstable[activeReadoutSet].size(); i++) {
				parcelPressure[i] = parcelPathMostUnstable[activeReadoutSet].get(i).getPressure();
				parcelHeight[i] = parcelPathMostUnstable[activeReadoutSet].get(i).getHeight();
				parcelTemperature[i] = parcelPathMostUnstable[activeReadoutSet].get(i).getTemperature();
				parcelDewpoint[i] = parcelPathMostUnstable[activeReadoutSet].get(i).getDewpoint();
				parcelVirtualTemperature[i] = WeatherUtils.virtualTemperature(parcelTemperature[i], parcelDewpoint[i],
						parcelPressure[i]);
			}

			double[] pressureRev = new double[vWind.length];
			double[] heightRev = new double[vWind.length];
			double[] temperatureRev = new double[vWind.length];
			double[] dewpointRev = new double[vWind.length];
			double[] uWindRev = new double[vWind.length];
			double[] vWindRev = new double[vWind.length];
			for (int i = 0; i < heightRev.length; i++) {
				pressureRev[heightRev.length - 1 - i] = pressure[i];
				heightRev[heightRev.length - 1 - i] = height[i];
				temperatureRev[heightRev.length - 1 - i] = temperature[i];
				dewpointRev[heightRev.length - 1 - i] = dewpoint[i];
				uWindRev[heightRev.length - 1 - i] = uWind[i];
				vWindRev[heightRev.length - 1 - i] = vWind[i];
			}

			final double dT = 1;
			final double dZ = parcelHeight[1] - parcelHeight[0];

			double lpl = logInterp(pressure, height, parcelPressure[0]);
//			System.out.println("MU-LPL: " + lpl + " m");
//			System.out.println("dZ: " + dZ);

			double x = 0.0;
			double y = 0.0;
			double z = muLfc[activeReadoutSet] + dZ;
			double w = 5.0;

//			System.out.println("MU-LFC: " + (lpl + z) + " m");

			ArrayList<Double> zArr = new ArrayList<>();
			ArrayList<Double> xArr = new ArrayList<>();
			ArrayList<Double> yArr = new ArrayList<>();

			double presAtLvl = linearInterp(parcelHeight, parcelPressure, z);

//			System.out.println("LFC PRES: " + presAtLvl + " Pa");

			double tempEnv = logInterp(pressure, temperature, presAtLvl);
			double dwptEnv = logInterp(pressure, dewpoint, presAtLvl);

			double tempParcel = linearInterp(parcelHeight, parcelTemperature, z);
			double dwptParcel = linearInterp(parcelHeight, parcelDewpoint, z);

			double vTempParcel = WeatherUtils.virtualTemperature(tempParcel, dwptParcel, presAtLvl);
			double vTempEnv = WeatherUtils.virtualTemperature(tempEnv, dwptEnv, presAtLvl);

//			System.out.printf("%7.2f\t%7.2f\t%7.2f\t%7.2f\n", z, w, vTempParcel, vTempEnv);

			while (vTempParcel >= vTempEnv) {
//			while (w >= 0) {
//				System.out.printf("%7.1f\t%7.1f\t%7.1f\n", z, u, v);

				zArr.add(z);
				xArr.add(x);
				yArr.add(y);

				double energyAdded = 9.81 * (vTempParcel - vTempEnv) / vTempEnv;
				w += energyAdded * dT;
				z += w * dT;

//				System.out.printf("%7.2f\t%7.2f\t%7.2f\t%7.2f\t%7.3f\n", z, w, vTempParcel, vTempEnv, energyAdded);

				presAtLvl = linearInterp(parcelHeight, parcelPressure, z);

				tempEnv = logInterp(pressure, temperature, presAtLvl);
				dwptEnv = logInterp(pressure, dewpoint, presAtLvl);

				tempParcel = linearInterp(parcelHeight, parcelTemperature, z);
				dwptParcel = linearInterp(parcelHeight, parcelDewpoint, z);

				vTempParcel = WeatherUtils.virtualTemperature(tempParcel, dwptParcel, presAtLvl);
				vTempEnv = WeatherUtils.virtualTemperature(tempEnv, dwptEnv, presAtLvl);

				double uWindEnv = logInterp(pressure, uWind, presAtLvl) - stormMotion[0];
				double vWindEnv = logInterp(pressure, vWind, presAtLvl) - stormMotion[1];

				double eEWind = Math.cos(-windOffsetAngle) * uWindEnv - Math.sin(-windOffsetAngle) * vWindEnv;
				double eNWind = Math.sin(-windOffsetAngle) * uWindEnv + Math.cos(-windOffsetAngle) * vWindEnv;

				x += eEWind * dT;
				y += eNWind * dT;
			}

			// integration time
			drawLeftCenterAlignedString((int) (xArr.size() * dT) + " s", g, (int) (5 * scale), (int) (8 * scale));

			// updraft tilt
			double updraftHeight = zArr.get(zArr.size() - 1) - zArr.get(0);

			double deltaX = xArr.get(xArr.size() - 1) - xArr.get(0);
			double deltaY = yArr.get(yArr.size() - 1) - yArr.get(0);
			double updraftTopOffset = Math.hypot(deltaX, deltaY);

			double updraftTilt = Math.toDegrees(Math.atan2(updraftTopOffset, updraftHeight));

			drawRightAlignedString((int) updraftTilt + " deg", g, (int) (182.5 * scale), (int) (8 * scale));

			double maxRadius = 0.0;

			for (int i = 0; i < xArr.size(); i++) {
				maxRadius = Double.max(maxRadius, Math.hypot(xArr.get(i), yArr.get(i)));
			}

			for (int i = 0; i < xArr.size() - 1; i++) {
				double xCurr = linScale(-1.25 * maxRadius, 1.25 * maxRadius, 6.25, 180.75, xArr.get(i));
				double yCurr = linScale(-1.25 * maxRadius, 1.25 * maxRadius, 175, 0, yArr.get(i));
//				double xPrev = linScale(-1.25 * maxRadius, 1.25 * maxRadius, 6.25, 180.75, xArr.get(i + 1));
//				double yPrev = linScale(-1.25 * maxRadius, 1.25 * maxRadius, 175, 0, yArr.get(i + 1));

				double z_ = lpl + zArr.get(i);
				if (z_ < 500) {
					g.setColor(new Color(255, 127, 255));
				} else if (z_ < 1000) {
					g.setColor(new Color(255, 0, 0));
				} else if (z_ < 3000) {
					g.setColor(new Color(255, 127, 0));
				} else if (z_ < 6000) {
					g.setColor(new Color(255, 255, 127));
				} else if (z_ < 9000) {
					g.setColor(new Color(0, 192, 0));
				} else if (z_ < 12000) {
					g.setColor(new Color(128, 192, 255));
				} else {
					g.setColor(new Color(192, 0, 192));
				}
//				g.drawLine((int) (xCurr * scale), (int) (yCurr * scale), (int) (xPrev * scale), (int) (yPrev * scale));

				if (i % 20 == 0) {
					g.drawOval((int) ((xCurr - 5) * scale), (int) ((yCurr - 5) * scale), (int) (10 * scale),
							(int) (10 * scale));
				}
			}

			return stormSlinky;
		}

		private BufferedImage drawHazardType(double scale) {
			BufferedImage winterReadouts = new BufferedImage((int) (187.5 * scale), (int) (175 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = winterReadouts.createGraphics();

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * scale)));

			drawCenteredString("POSSIBLE HAZARD TYPE", g, (int) (93.75 * scale), (int) (7.5 * scale));

			g.drawLine((int) (0 * scale), (int) (15 * scale), (int) (187.5 * scale), (int) (15 * scale));

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (24 * scale)));

			switch (hazType) {
			case PDS_DEV_TOR:
				g.setColor(new Color(255, 128, 255));
				drawCenteredString("PDS DEV TOR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case PDS_TOR:
				g.setColor(new Color(255, 128, 255));
				drawCenteredString("PDS TOR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case DEV_TOR:
				g.setColor(new Color(255, 128, 255));
				drawCenteredString("DEV TOR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case TOR:
				g.setColor(new Color(255, 0, 0));
				drawCenteredString("TOR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case MRGL_TOR:
				g.setColor(new Color(255, 128, 128));
				drawCenteredString("MRGL TOR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case SVR:
				g.setColor(new Color(255, 255, 128));
				drawCenteredString("SVR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case MRGL_SVR:
				g.setColor(new Color(128, 128, 255));
				drawCenteredString("MRGL SVR", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case FLASH_FLOOD:
				g.setColor(new Color(0, 196, 0));
				drawCenteredString("FLASH FLOOD", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case BLIZZARD:
				g.setColor(new Color(128, 255, 255));
				drawCenteredString("BLIZZARD", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case EXTREME_HEAT:
				g.setColor(new Color(255, 150, 92));
				drawCenteredString("EXTREME HEAT", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case EXTREME_COLD:
				g.setColor(new Color(128, 255, 255));
				drawCenteredString("EXTREME COLD", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			case NONE:
				g.setColor(new Color(196, 196, 196));
				drawCenteredString("NONE", g, (int) (93.75 * scale), (int) (87.5 * scale));
				break;
			default:
				break;
			}

			return winterReadouts;
		}

		private void drawDashedLine(Graphics2D g, int x1, int y1, int x2, int y2, double dashPeriod) {
			double length = Math.hypot(x2 - x1, y2 - y1);
			int dashes = (int) Math.ceil(length / dashPeriod);

			for (double prog = 0; prog + 0.5 / dashes < 1; prog += 1.0 / dashes) {
				double prog1 = prog + 0.5 / dashes;
				double prog2 = prog + 1.0 / dashes;

				int x1_ = (int) ((1 - prog1) * x1 + prog1 * x2);
				int y1_ = (int) ((1 - prog1) * y1 + prog1 * y2);
				int x2_ = (int) ((1 - prog2) * x1 + prog2 * x2);
				int y2_ = (int) ((1 - prog2) * y1 + prog2 * y2);

				g.drawLine(x1_, y1_, x2_, y2_);
			}
		}
	}

	private class SoundingKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();

//			System.out.println(key);

			switch (key) {
			case KeyEvent.VK_1:
				if (sounding1 != null) {
					activeSounding = sounding0;
					activeReadoutSet = 0;

					setTitle(generateTitle(time0));

					hazType = determineHazType();

					g.repaint();
				}
				break;
			case KeyEvent.VK_2:
				if (sounding1 != null) {
					activeSounding = soundingM;
					activeReadoutSet = 1;

					setTitle(generateTitle(timeM));

					hazType = determineHazType();

					g.repaint();
				}
				break;
			case KeyEvent.VK_3:
				if (sounding1 != null) {
					activeSounding = sounding1;
					activeReadoutSet = 2;

					setTitle(generateTitle(time1));

					hazType = determineHazType();

					g.repaint();
				}
				break;
			case KeyEvent.VK_P:
				selectParcelPathType();
				g.repaint();
				break;
			case KeyEvent.VK_S:
				selectStormMotionVector();
				g.repaint();
				break;
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}

	private static final String[] parcelPathOptions = { "None", "Surface Based", "Mixed Layer (50 mb)",
			"Mixed Layer (100 mb)", "Most Unstable" };

	private void selectParcelPathType() {
		String fieldChoice = (String) JOptionPane.showInputDialog(null, "What type of parcel path would you like?",
				"Choose Field", JOptionPane.QUESTION_MESSAGE, null, parcelPathOptions,
				parcelPathVisible ? pathType.ordinal() + 1 : 0);

		int choiceId = 0;

		for (int i = 0; i < parcelPathOptions.length; i++) {
			if (parcelPathOptions[i].equals(fieldChoice)) {
				choiceId = i;
			}
		}

		if (choiceId == 0) {
			parcelPathVisible = false;
		} else {
			parcelPathVisible = true;
			pathType = ParcelPath.values()[choiceId - 1];
		}
	}

	private static final String[] stormMotionVectorOptions = { "Left Moving", "Mean Wind", "Right Moving",
			"Corfidi Upshear", "Corfidi Downshear" };

	private void selectStormMotionVector() {
		String fieldChoice = (String) JOptionPane.showInputDialog(null,
				"Which storm motion vector would you like to use for the kinematics readouts?",
				"Choose Storm Motion Vector", JOptionPane.QUESTION_MESSAGE, null, stormMotionVectorOptions,
				stormMotionVector);

		int choiceId = 0;

		for (int i = 0; i < stormMotionVectorOptions.length; i++) {
			if (stormMotionVectorOptions[i].equals(fieldChoice)) {
				choiceId = i;
			}
		}

		stormMotionVector = choiceId;

		for (int i = 0; i < 3; i++) {
			if (i == 1 || sounding1 != null) {
				Sounding activeSounding = null;

				switch (i) {
				case 0:
					activeSounding = sounding0;
					break;
				case 1:
					activeSounding = soundingM;
					break;
				case 2:
					activeSounding = sounding1;
					break;
				}

				double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

				switch (stormMotionVector) {
				case 0:
					stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(activeSounding.getPressureLevels(),
							activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
					break;
				case 1:
					stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(activeSounding.getPressureLevels(),
							activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());
					break;
				case 2:
					break;
				case 3:
					stormMotion = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
							activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
							activeSounding.getUWind(), activeSounding.getVWind());
					break;
				case 4:
					stormMotion = WeatherUtils.corfidiDownshear(activeSounding.getPressureLevels(),
							activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
							activeSounding.getUWind(), activeSounding.getVWind());
					break;
				case 5:
					break;
				}

				srh0_500[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				srh0_1[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				srh0_3[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				srhInflow[i] = WeatherUtils.stormRelativeHelicity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				srw0_500[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				srw0_1[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				srw0_3[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				srwInflow[i] = WeatherUtils.stormRelativeMeanWind(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				streamwiseVort0_500[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				streamwiseVort0_1[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				streamwiseVort0_3[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				streamwiseVortInflow[i] = WeatherUtils.streamwiseVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				streamwiseness0_500[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 500);
				streamwiseness0_1[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 1000);
				streamwiseness0_3[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						0, 3000);
				streamwisenessInflow[i] = WeatherUtils.streamwisenessOfVorticity(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(), stormMotion,
						inflowLayer[i][0], inflowLayer[i][1]);

				scp[i] = WeatherUtils.supercellComposite(activeSounding.getPressureLevels(), activeSounding.getHeight(),
						activeSounding.getTemperature(), activeSounding.getDewpoint(), activeSounding.getUWind(),
						activeSounding.getVWind(), stormMotion);
				stpE[i] = WeatherUtils.significantTornadoParameterEffective(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind(), stormMotion);
				stpF[i] = WeatherUtils.significantTornadoParameterFixed(activeSounding.getPressureLevels(),
						activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
						activeSounding.getUWind(), activeSounding.getVWind(), stormMotion);
				devtor[i] = WeatherUtils.deviantTornadoParameter(stpE[i], stormMotion,
						WeatherUtils.deviantTornadoMotion(activeSounding.getPressureLevels(),
								activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind(),
								stormMotion));

				hazType = determineHazType();
			}
		}
	}

	private static String timeString(DateTime time) {
		return String.format("%02d:%02dZ", time.getHourOfDay(), time.getMinuteOfHour());
	}

	private static String dateTimeString(DateTime time) {
		return String.format("%04d-%02d-%02d %02d:%02dZ", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
				time.getHourOfDay(), time.getMinuteOfHour());
	}

	private HazType determineHazType() {
		double[] pressure = activeSounding.getPressureLevels();
		double[] height = activeSounding.getHeight();
		double[] temperature = activeSounding.getTemperature();
		double[] dewpoint = activeSounding.getDewpoint();
		double[] uWind = activeSounding.getUWind();
		double[] vWind = activeSounding.getVWind();

		int mesocycloneSign = (stormMotionVector == 0) ? -1 : 1;

		double[] relativeHumidity = new double[dewpoint.length];
		for (int i = 0; i < relativeHumidity.length; i++) {
			relativeHumidity[i] = WeatherUtils.dewpoint(temperature[i], dewpoint[i]);
		}

		double[] heightRev = new double[vWind.length];
		double[] temperatureRev = new double[vWind.length];
		for (int i = 0; i < heightRev.length; i++) {
			heightRev[heightRev.length - 1 - i] = height[i];
			temperatureRev[heightRev.length - 1 - i] = temperature[i];
		}

		double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(activeSounding.getPressureLevels(),
				activeSounding.getHeight(), activeSounding.getUWind(), activeSounding.getVWind());

		switch (stormMotionVector) {
		case 0:
			stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(pressure, height, uWind, vWind);
			break;
		case 1:
			stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(pressure, height, uWind, vWind);
			break;
		case 2:
			break;
		case 3:
			break;
		}

		double[] srw4_6Vec = WeatherUtils.stormRelativeMeanWind(pressure, height, uWind, vWind, stormMotion, 4000,
				6000);
		double srw4_6 = Math.hypot(srw4_6Vec[0], srw4_6Vec[1]);

		double bwd0_6 = WeatherUtils.bulkShearMagnitude(height, uWind, vWind, 0, 6000);
		double bwd0_8 = WeatherUtils.bulkShearMagnitude(height, uWind, vWind, 0, 8000);

		double temperature8km = linearInterp(heightRev, temperatureRev, 8000 + heightRev[0]);
		double temperature3km = linearInterp(heightRev, temperatureRev, 3000 + heightRev[0]);
		double temperature1km = linearInterp(heightRev, temperatureRev, 1000 + heightRev[0]);
		double temperature0km = linearInterp(heightRev, temperatureRev, heightRev[0]);

		double lapseRate0_1km = (temperature0km - temperature1km) / (1000);
		double lapseRate0_3km = (temperature0km - temperature3km) / (3000);
		double lapseRate3_8km = (temperature3km - temperature8km) / (8000);

		double[] mw1000_3500Vec = WeatherUtils.stormRelativeMeanWind(pressure, height, uWind, vWind,
				new double[] { 0, 0 }, 1000, 3500);
		double mw1000_3500 = Math.hypot(mw1000_3500Vec[0], mw1000_3500Vec[1]);

		double[] mw3_12Vec = WeatherUtils.stormRelativeMeanWind(pressure, height, uWind, vWind, new double[] { 0, 0 },
				3000, 12000);
		double mw3_12 = Math.hypot(mw3_12Vec[0], mw3_12Vec[1]);

		double wndg_mlcapeTerm = ml100cape[activeReadoutSet] / 2000;
		double wndg_lr0_3Term = lapseRate0_3km / 0.009;
		double wndg_mwTerm = mw1000_3500 / 15;
		double wndg_mlcinhTerm = (50 + ml100cinh[activeReadoutSet]) / 40;

		double lowRH = WeatherUtils.averageParameterOverLayer(height, relativeHumidity, 0, 3000);
		double midRH = WeatherUtils.averageParameterOverLayer(height, relativeHumidity, 3000, 8000);

		if (lapseRate0_3km < 0.007)
			wndg_lr0_3Term = 0;
		if (ml100cinh[activeReadoutSet] < -50)
			wndg_mlcinhTerm = 0;

		double sigSvr = ml100cape[activeReadoutSet] * bwd0_6;

		double mmp = 0;
		if (mucape[activeReadoutSet] >= 100) {
			mmp = 1 / 1 + Math.exp(
					13.0 - 4.59 * bwd0_8 - 1.16 * lapseRate3_8km - 61700 * mucape[activeReadoutSet] - 0.17 * mw3_12);
		}

		double wndg = wndg_mlcapeTerm * wndg_lr0_3Term * wndg_mwTerm * wndg_mlcinhTerm;

		double[] corfidiUpshearVector = WeatherUtils.corfidiUpshear(activeSounding.getPressureLevels(),
				activeSounding.getHeight(), activeSounding.getTemperature(), activeSounding.getDewpoint(),
				activeSounding.getUWind(), activeSounding.getVWind());

		double corfidiUpshear = Math.hypot(corfidiUpshearVector[0], corfidiUpshearVector[1]);

		if (stpE[activeReadoutSet] >= 3 && stpF[activeReadoutSet] >= 3 && srh0_1[activeReadoutSet] >= 200
				&& srhInflow[activeReadoutSet] >= 200 && srw4_6 >= 15 * 0.5144444 && bwd0_8 >= 45 * 0.5144444
				&& sbLcl[activeReadoutSet] < 1000 && ml100Lcl[activeReadoutSet] < 1200 && lapseRate0_1km >= 0.005
				&& ml100cinh[activeReadoutSet] >= -50 && inflowLayer[activeReadoutSet][0] >= 0) {
			if (Math.abs(devtor[activeReadoutSet]) >= 0.5) {
				return PDS_DEV_TOR;
			} else {
				return PDS_TOR;
			}
		} else if ((stpE[activeReadoutSet] >= 3 || stpF[activeReadoutSet] >= 4) && ml100cinh[activeReadoutSet] >= -125
				&& inflowLayer[activeReadoutSet][0] >= 0) {
			if (Math.abs(devtor[activeReadoutSet]) >= 0.5) {
				return DEV_TOR;
			} else {
				return TOR;
			}
		} else if ((stpE[activeReadoutSet] >= 1 || stpF[activeReadoutSet] >= 1)
				&& (srw4_6 >= 15 * 0.5144444 || bwd0_8 >= 40 * 0.5144444) && ml100cinh[activeReadoutSet] >= -50
				&& inflowLayer[activeReadoutSet][0] >= 0) {
			if (Math.abs(devtor[activeReadoutSet]) >= 0.5) {
				return DEV_TOR;
			} else {
				return TOR;
			}
		} else if ((stpE[activeReadoutSet] >= 1 || stpF[activeReadoutSet] >= 1) && (lowRH + midRH) / 2 >= 0.6
				&& lapseRate0_1km >= 0.005 && ml100cinh[activeReadoutSet] >= -50
				&& inflowLayer[activeReadoutSet][0] >= 0) {
			if (Math.abs(devtor[activeReadoutSet]) >= 0.5) {
				return DEV_TOR;
			} else {
				return TOR;
			}
		} else if ((stpE[activeReadoutSet] >= 1 || stpF[activeReadoutSet] >= 1) && ml100cinh[activeReadoutSet] >= -150
				&& inflowLayer[activeReadoutSet][0] >= 0) {
			return MRGL_TOR;
		} else if ((stpE[activeReadoutSet] >= 1 && srhInflow[activeReadoutSet] >= 150)
				|| (stpF[activeReadoutSet] >= 0.5 && srh0_1[activeReadoutSet] >= 150)
						&& ml100cinh[activeReadoutSet] >= -50 && inflowLayer[activeReadoutSet][0] >= 0) {
			return MRGL_TOR;
		} else if ((stpF[activeReadoutSet] >= 1 || mesocycloneSign * scp[activeReadoutSet] >= 4
				|| stpE[activeReadoutSet] >= 1) && mucinh[activeReadoutSet] >= -50) {
			return SVR;
		} else if (((mesocycloneSign * scp[activeReadoutSet] >= 2 && ship[activeReadoutSet] >= 1)
				|| dcape[activeReadoutSet] >= 750) && mucinh[activeReadoutSet] >= -50) {
			return SVR;
		} else if (sigSvr >= 30000 && mmp >= 0.6 && mucinh[activeReadoutSet] >= -50) {
			return SVR;
		} else if ((wndg >= 0.5 || ship[activeReadoutSet] >= 0.5 || mesocycloneSign * scp[activeReadoutSet] >= 0.5)
				&& mucinh[activeReadoutSet] >= -75) {
			return MRGL_SVR;
		} else if (pwat[activeReadoutSet] >= 40 && corfidiUpshear < 25 * 0.5144444) {
			return FLASH_FLOOD;
		} else if (Math.hypot(uWind[uWind.length - 1], vWind[vWind.length - 1]) >= 15.6464
				&& temperature[temperature.length - 1] <= 273.15
				&& PtypeAlgorithms.bourgouinRevisedMethod(pressure, temperature, dewpoint, height,
						pressure[pressure.length - 1], height[height.length - 1]) == PrecipitationType.SNOW) {
			return BLIZZARD;
		} else if (WeatherUtils.heatIndex(temperature[temperature.length - 1],
				dewpoint[dewpoint.length - 1]) >= 313.705) {
			return EXTREME_HEAT;
		} else if (WeatherUtils.windChill(temperature[temperature.length - 1],
				Math.hypot(uWind[uWind.length - 1], vWind[vWind.length - 1])) <= 258.15) {
			return EXTREME_COLD;
		} else {
			return NONE;
		}
	}

	public static void drawCenteredString(String s, Graphics2D g, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getAscent() + fm.getDescent();
		int width = fm.stringWidth(s);
		g.drawString(s, x - width / 2, y + (fm.getAscent() - ht / 2));
	}

	public static void drawCenteredOutlinedString(String s, Graphics2D g, int x, int y, Color c) {
		g.setColor(Color.BLACK);
		drawCenteredString(s, g, x - 1, y - 1);
		drawCenteredString(s, g, x - 1, y);
		drawCenteredString(s, g, x - 1, y + 1);
		drawCenteredString(s, g, x, y - 1);
		drawCenteredString(s, g, x, y + 1);
		drawCenteredString(s, g, x + 1, y - 1);
		drawCenteredString(s, g, x + 1, y);
		drawCenteredString(s, g, x + 1, y + 1);

		g.setColor(c);
		drawCenteredString(s, g, x, y);
	}

	public static void drawRightAlignedString(String s, Graphics2D g, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getAscent() + fm.getDescent();
		int width = fm.stringWidth(s);
		g.drawString(s, x - width, y + (fm.getAscent() - ht / 2));
	}

	public static void drawLeftCenterAlignedString(String s, Graphics2D g, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		int ht = fm.getAscent() + fm.getDescent();
		g.drawString(s, x, y + (fm.getAscent() - ht / 2));
	}

	private double linScale(double preMin, double preMax, double postMin, double postMax, double value) {
		double slope = (postMax - postMin) / (preMax - preMin);

		return slope * (value - preMin) + postMin;
	}

	// inputArr assumed to already be sorted and increasing
	private static double linearInterp(double[] inputArr, double[] outputArr, double input) {
		if (input < inputArr[0]) {
			return outputArr[0];
		} else if (input >= inputArr[inputArr.length - 1]) {
			return outputArr[outputArr.length - 1];
		} else {
			for (int i = 0; i < inputArr.length - 1; i++) {
				if (i + 1 == outputArr.length) {
					return outputArr[outputArr.length - 1];
				}

				double input1 = inputArr[i];
				double input2 = inputArr[i + 1];

				if (input == input1) {
					return outputArr[i];
				} else if (input < input2) {
					double output1 = outputArr[i];
					double output2 = outputArr[i + 1];

					double weight1 = (input2 - input) / (input2 - input1);
					double weight2 = (input - input1) / (input2 - input1);

					return output1 * weight1 + output2 * weight2;
				} else {
					continue;
				}
			}

			return -1024.0;
		}
	}

	// inputArr assumed to already be sorted and increasing
	private static double logInterp(double[] inputArr, double[] outputArr, double input) {
		if (input < inputArr[0]) {
			return outputArr[0];
		} else if (input >= inputArr[inputArr.length - 1]) {
			return outputArr[outputArr.length - 1];
		} else {
			for (int i = 0; i < inputArr.length - 1; i++) {
				if (i + 1 == outputArr.length) {
					return outputArr[outputArr.length - 1];
				}

				double input1 = inputArr[i];
				double input2 = inputArr[i + 1];

				if (input == input1) {
					return outputArr[i];
				} else if (input < input2) {
					double logInput1 = Math.log(input1);
					double logInput2 = Math.log(input2);
					double logInput = Math.log(input);

					double output1 = outputArr[i];
					double output2 = outputArr[i + 1];

					double weight1 = (logInput2 - logInput) / (logInput2 - logInput1);
					double weight2 = (logInput - logInput1) / (logInput2 - logInput1);

					return output1 * weight1 + output2 * weight2;
				} else {
					continue;
				}
			}

			return -1024.0;
		}
	}

	private String generateTitle(DateTime time) {
		String latStr = (lat > 0) ? String.format("%4.2fN", lat) : String.format("%4.2fS", -lat);
		String lonStr = (lon > 0) ? String.format("%4.2fE", lon) : String.format("%4.2fW", -lon);
		String timeStr = dateTimeString(time);

		if (soundingSource.length() > 0) {
			if (lat != -1024) {
				if (time != null) {
					return soundingSource + " Sounding at <" + latStr + ", " + lonStr + "> | " + timeStr;
				} else {
					return soundingSource + " Sounding at <" + latStr + ", " + lonStr + ">";
				}
			} else {
				if (time != null) {
					return soundingSource + " Sounding | " + timeStr;
				} else {
					return soundingSource + " Sounding";
				}
			}
		} else {
			if (lat != -1024) {
				if (time != null) {
					return "Sounding at <" + latStr + ", " + lonStr + "> | " + timeStr;
				} else {
					return "Sounding at <" + latStr + ", " + lonStr + ">";
				}
			} else {
				if (time != null) {
					return "Sounding | " + timeStr;
				} else {
					return "Sounding";
				}
			}
		}
	}

	// turns a motion vector into a direction/magnitude reading
	// offset in radians
	private static String windReading(double[] vector, double offset) {
		double direction = Math.toDegrees(Math.atan2(-vector[0], -vector[1])) + 360;
		direction %= 360;

		double magnitude = Math.hypot(vector[0], vector[1]) / 0.5144444;

		return String.format("%3d/%02d", (int) direction, (int) Math.round(magnitude));
	}
}
