package com.ameliaWx.soundingViewer;

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
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.joda.time.DateTime;

import com.ameliaWx.weatherUtils.ParcelPath;
import com.ameliaWx.weatherUtils.RecordAtLevel;
import com.ameliaWx.weatherUtils.WeatherUtils;

public class SoundingFrame extends JFrame {
	private static final long serialVersionUID = 396540838404275479L;

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
	private ArrayList<RecordAtLevel> parcelPathSurfaceBased;
	private ArrayList<RecordAtLevel> parcelPathMixedLayer50Mb;
	private ArrayList<RecordAtLevel> parcelPathMixedLayer100Mb;
	private ArrayList<RecordAtLevel> parcelPathMostUnstable;

	private ArrayList<RecordAtLevel> parcelPathDowndraft;

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

	private double muLpl = 0.0;

	private double[] dcape = new double[3];

	private double[][] inflowLayer = new double[3][2];

	private SoundingGraphics g;
	private MapInset mapInset;

	public SoundingFrame(String soundingSource, Sounding sounding0, DateTime time0, Sounding soundingM,
			DateTime timeM, Sounding sounding1, DateTime time1, double lat, double lon, double windOffsetAngle,
			MapInset mapInset) {
		this.lat = lat;
		this.lon = lon;
		this.timeM = timeM;

		this.mapInset = mapInset;

		this.windOffsetAngle = windOffsetAngle;

		String latStr = (lat > 0) ? String.format("%4.2fN", lat) : String.format("%4.2fS", -lat);
		String lonStr = (lon > 0) ? String.format("%4.2fE", lon) : String.format("%4.2fW", -lon);
		String timeStr = timeString(this.timeM);

		this.setSize(1750, 900);
		this.setLocationRelativeTo(null);

		if (soundingSource.length() > 0) {
			this.setTitle(soundingSource + " Sounding at <" + latStr + ", " + lonStr + "> | " + timeStr);
		} else {
			this.setTitle("Sounding at <" + latStr + ", " + lonStr + "> | " + timeStr);
		}

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

		parcelPathSurfaceBased = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.SURFACE_BASED, false);
		parcelPathMixedLayer50Mb = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MIXED_LAYER_50MB, false);
		parcelPathMixedLayer100Mb = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MIXED_LAYER_100MB, false);
		parcelPathMostUnstable = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MOST_UNSTABLE, false);

		parcelPathDowndraft = WeatherUtils.computeDcapeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint());

		double surfacePressure = soundingM.getPressureLevels()[soundingM.getPressureLevels().length - 1];

		sbcape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50cape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100cape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		mucape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbcinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50cinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100cinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		mucinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbLcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathSurfaceBased);
		ml50Lcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMixedLayer50Mb);
		ml100Lcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMixedLayer100Mb);
		muLcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMostUnstable);

		sbCcl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50Ccl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100Ccl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		muCcl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbLfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50Lfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100Lfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		muLfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbEl[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50El[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100El[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		muEl[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		dcape[1] = WeatherUtils.computeDcape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint());

		inflowLayer[1] = WeatherUtils.effectiveInflowLayer(soundingM.getPressureLevels(), soundingM.getHeight(),
				soundingM.getTemperature(), soundingM.getDewpoint());

		this.setVisible(true);
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
		this.soundingM = soundingM;

		this.lat = lat;
		this.lon = lon;
		this.timeM = timeM;

		this.mapInset = mapInset;

		String latStr = (lat > 0) ? String.format("%4.2fN", lat) : String.format("%4.2fS", -lat);
		String lonStr = (lon > 0) ? String.format("%4.2fE", lon) : String.format("%4.2fW", -lon);
		String timeStr = timeString(this.timeM);

		this.setSize(1750, 900);
		this.setLocationRelativeTo(null);

		if (soundingSource.length() > 0) {
			if(lat != -1024.0) {
				if(timeM != null) {
					this.setTitle(soundingSource + " Sounding at <" + latStr + ", " + lonStr + "> | " + timeStr);
				} else {
					this.setTitle(soundingSource + " Sounding at <" + latStr + ", " + lonStr + ">");
				}
			} else {
				if(timeM != null) {
					this.setTitle(soundingSource + " Sounding | " + timeStr);
				} else {
					this.setTitle(soundingSource + " Sounding");
				}
			}
		} else {
			if(lat != -1024.0) {
				if(timeM != null) {
					this.setTitle("Sounding at <" + latStr + ", " + lonStr + "> | " + timeStr);
				} else {
					this.setTitle("Sounding at <" + latStr + ", " + lonStr + ">");
				}
			} else {
				if(timeM != null) {
					this.setTitle("Sounding | " + timeStr);
				} else {
					this.setTitle("Sounding");
				}
			}
		}

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		g = new SoundingGraphics();
		this.add(g);

		this.addKeyListener(new SoundingKeyListener());

		parcelPathSurfaceBased = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.SURFACE_BASED, false);
		parcelPathMixedLayer50Mb = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MIXED_LAYER_50MB, false);
		parcelPathMixedLayer100Mb = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MIXED_LAYER_100MB, false);
		parcelPathMostUnstable = WeatherUtils.computeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MOST_UNSTABLE, false);

		parcelPathDowndraft = WeatherUtils.computeDcapeParcelPath(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint());

		double surfacePressure = soundingM.getPressureLevels()[soundingM.getPressureLevels().length - 1];

		sbcape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50cape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100cape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		mucape[1] = WeatherUtils.computeCape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbcinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50cinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100cinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		mucinh[1] = WeatherUtils.computeCinh(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbLcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathSurfaceBased);
		ml50Lcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMixedLayer50Mb);
		ml100Lcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMixedLayer100Mb);
		muLcl[1] = WeatherUtils.liftedCondensationLevel(surfacePressure, parcelPathMostUnstable);

		sbCcl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50Ccl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100Ccl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(),
				soundingM.getTemperature(), soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		muCcl[1] = WeatherUtils.convectiveCondensationLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbLfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50Lfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100Lfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		muLfc[1] = WeatherUtils.levelOfFreeConvection(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		sbEl[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathSurfaceBased);
		ml50El[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer50Mb);
		ml100El[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMixedLayer100Mb);
		muEl[1] = WeatherUtils.equilibriumLevel(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint(), parcelPathMostUnstable);

		dcape[1] = WeatherUtils.computeDcape(soundingM.getPressureLevels(), soundingM.getTemperature(),
				soundingM.getDewpoint());

		inflowLayer[1] = WeatherUtils.effectiveInflowLayer(soundingM.getPressureLevels(), soundingM.getHeight(),
				soundingM.getTemperature(), soundingM.getDewpoint());

		this.setVisible(true);
	}

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

			// skew-t frame
			g.drawRect((int) (50 * scale), (int) (50 * scale), (int) (800 * scale), (int) (800 * scale));

			// hodograph frame
			g.drawRect((int) (875 * scale), (int) (50 * scale), (int) (400 * scale), (int) (400 * scale));

			// map inset frame
			g.drawRect((int) (1300 * scale), (int) (50 * scale), (int) (400 * scale), (int) (400 * scale));

			// readout frame
			g.drawRect((int) (875 * scale), (int) (475 * scale), (int) (825 * scale), (int) (375 * scale));

			// maybe fit a vorticity frame into the hodograph frame

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

			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int) (12 * scale)));
			// hodograph label
			drawCenteredString("1 RING = 10 M S^-1", g, (int) (1075 * scale), (int) (460 * scale));

			// readouts
			double[] sbParcelPressure = new double[parcelPathSurfaceBased.size()];
			double[] sbParcelHeight = new double[parcelPathSurfaceBased.size()];
			double muLplPressure = parcelPathMostUnstable.get(0).getPressure();

			for (int i = 0; i < sbParcelPressure.length; i++) {
				sbParcelPressure[sbParcelPressure.length - 1 - i] = parcelPathSurfaceBased.get(i).getPressure();
				sbParcelHeight[sbParcelPressure.length - 1 - i] = parcelPathSurfaceBased.get(i).getHeight();
			}

			muLpl = logInterp(sbParcelPressure, sbParcelHeight, muLplPressure);

//			System.out.println("MU-LPP: " + muLplPressure);
//			System.out.println("MU-LPL: " + muLpl);
			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "", "SB", "ML-50MB", "ML-100MB", "MU"),
					(int) (890 * scale), (int) (490 * scale));
			g.drawString(String.format("%-9s%-9d%-9d%-9d%-9d", "CAPE", (int) sbcape[activeReadoutSet],
					(int) ml50cape[activeReadoutSet], (int) ml100cape[activeReadoutSet],
					(int) mucape[activeReadoutSet]), (int) (890 * scale), (int) (505 * scale));

			String sbcinh_ = (sbcape[activeReadoutSet] > 0) ? String.valueOf((int) sbcinh[activeReadoutSet]) : "-";
			String ml50cinh_ = (ml50cape[activeReadoutSet] > 0) ? String.valueOf((int) ml50cinh[activeReadoutSet])
					: "-";
			String ml100cinh_ = (ml100cape[activeReadoutSet] > 0) ? String.valueOf((int) ml100cinh[activeReadoutSet])
					: "-";
			String mucinh_ = (mucape[activeReadoutSet] > 0) ? String.valueOf((int) mucinh[activeReadoutSet]) : "-";

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "CINH", sbcinh_, ml50cinh_, ml100cinh_, mucinh_),
					(int) (890 * scale), (int) (520 * scale));

			String sbLcl_ = String.format("%5.2f", sbLcl[activeReadoutSet] / 1000.0);
			String ml50Lcl_ = String.format("%5.2f", ml50Lcl[activeReadoutSet] / 1000.0);
			String ml100Lcl_ = String.format("%5.2f", ml100Lcl[activeReadoutSet] / 1000.0);
			String muLcl_ = String.format("%5.2f", (muLcl[activeReadoutSet] + muLpl) / 1000.0);

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "LCL [km]", sbLcl_, ml50Lcl_, ml100Lcl_, muLcl_),
					(int) (890 * scale), (int) (535 * scale));

			String sbCcl_ = String.format("%5.2f", sbCcl[activeReadoutSet] / 1000.0);
			String ml50Ccl_ = String.format("%5.2f", ml50Ccl[activeReadoutSet] / 1000.0);
			String ml100Ccl_ = String.format("%5.2f", ml100Ccl[activeReadoutSet] / 1000.0);
			String muCcl_ = String.format("%5.2f", (muCcl[activeReadoutSet] + muLpl) / 1000.0);

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "CCL [km]", sbCcl_, ml50Ccl_, ml100Ccl_, muCcl_),
					(int) (890 * scale), (int) (550 * scale));

			String sbLfc_ = (sbcape[activeReadoutSet] > 0) ? String.format("%5.2f", sbLfc[activeReadoutSet] / 1000.0)
					: "-";
			String ml50Lfc_ = (ml50cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml50Lfc[activeReadoutSet] / 1000.0)
					: "-";
			String ml100Lfc_ = (ml100cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml100Lfc[activeReadoutSet] / 1000.0)
					: "-";
			String muLfc_ = (mucape[activeReadoutSet] > 0)
					? String.format("%5.2f", (muLfc[activeReadoutSet] + muLpl) / 1000.0)
					: "-";

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "LFC [km]", sbLfc_, ml50Lfc_, ml100Lfc_, muLfc_),
					(int) (890 * scale), (int) (565 * scale));

			String sbEl_ = (sbcape[activeReadoutSet] > 0) ? String.format("%5.2f", sbEl[activeReadoutSet] / 1000.0)
					: "-";
			String ml50El_ = (ml50cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml50El[activeReadoutSet] / 1000.0)
					: "-";
			String ml100El_ = (ml100cape[activeReadoutSet] > 0)
					? String.format("%5.2f", ml100El[activeReadoutSet] / 1000.0)
					: "-";
			String muEl_ = (mucape[activeReadoutSet] > 0)
					? String.format("%5.2f", (muEl[activeReadoutSet] + muLpl) / 1000.0)
					: "-";

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "EL [km]", sbEl_, ml50El_, ml100El_, muEl_),
					(int) (890 * scale), (int) (580 * scale));

			String stormMotionVectorText = "";

			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind());

			switch (stormMotionVector) {
			case 0:
				stormMotionVectorText = "[LM]";
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 1:
				stormMotionVectorText = "[MW]";
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 2:
				stormMotionVectorText = "[RM]";
				break;
			case 3:
				stormMotionVectorText = "[USER]";
				break;
			}

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", stormMotionVectorText, "0-500 m", "0-1 km", "0-3 km",
					"INFLOW"), (int) (890 * scale), (int) (610 * scale));

			double srh0_500 = WeatherUtils.stormRelativeHelicity(soundingM.getPressureLevels(), soundingM.getHeight(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 500);
			double srh0_1 = WeatherUtils.stormRelativeHelicity(soundingM.getPressureLevels(), soundingM.getHeight(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 1000);
			double srh0_3 = WeatherUtils.stormRelativeHelicity(soundingM.getPressureLevels(), soundingM.getHeight(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 3000);
			double srhInflow = WeatherUtils.stormRelativeHelicity(soundingM.getPressureLevels(), soundingM.getHeight(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, inflowLayer[activeReadoutSet][0],
					inflowLayer[activeReadoutSet][1]);

			String srh0_500_ = String.format("%6d", (int) srh0_500);
			String srh0_1_ = String.format("%6d", (int) srh0_1);
			String srh0_3_ = String.format("%6d", (int) srh0_3);
			String srhInflow_ = String.format("%6d", (int) srhInflow);

			srhInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : srhInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SRH", srh0_500_, srh0_1_, srh0_3_, srhInflow_),
					(int) (890 * scale), (int) (625 * scale));

			double[] srw0_500 = WeatherUtils.stormRelativeMeanWind(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), stormMotion, 0, 500);
			double[] srw0_1 = WeatherUtils.stormRelativeMeanWind(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), stormMotion, 0, 1000);
			double[] srw0_3 = WeatherUtils.stormRelativeMeanWind(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), stormMotion, 0, 3000);
			double[] srwInflow = WeatherUtils.stormRelativeMeanWind(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), stormMotion, inflowLayer[activeReadoutSet][0],
					inflowLayer[activeReadoutSet][1]);

			String srw0_500_ = windReading(srw0_500, -windOffsetAngle);
			String srwLm0_1_ = windReading(srw0_1, -windOffsetAngle);
			String srwLm0_3_ = windReading(srw0_3, -windOffsetAngle);
			String srwInflow_ = windReading(srwInflow, -windOffsetAngle);

			srwInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : srwInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SRW", srw0_500_, srwLm0_1_, srwLm0_3_, srwInflow_),
					(int) (890 * scale), (int) (640 * scale));

			double bulkShear0_500 = WeatherUtils.bulkShearMagnitude(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), 0, 500);
			double bulkShear0_1 = WeatherUtils.bulkShearMagnitude(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), 0, 1000);
			double bulkShear0_3 = WeatherUtils.bulkShearMagnitude(soundingM.getPressureLevels(), soundingM.getUWind(),
					soundingM.getVWind(), 0, 3000);
			double bulkShearInflow = WeatherUtils.bulkShearMagnitude(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), inflowLayer[activeReadoutSet][0],
					inflowLayer[activeReadoutSet][1]);

			String bulkShear0_500_ = String.format("%6.1f", bulkShear0_500);
			String bulkShear0_1_ = String.format("%6.1f", bulkShear0_1);
			String bulkShear0_3_ = String.format("%6.1f", bulkShear0_3);
			String bulkShearInflow_ = String.format("%6.1f", bulkShearInflow);

			bulkShearInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : bulkShearInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SHEAR", bulkShear0_500_, bulkShear0_1_, bulkShear0_3_,
					bulkShearInflow_), (int) (890 * scale), (int) (655 * scale));

			double streamwiseVort0_500 = WeatherUtils.streamwiseVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 500);
			double streamwiseVort0_1 = WeatherUtils.streamwiseVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 1000);
			double streamwiseVort0_3 = WeatherUtils.streamwiseVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 3000);
			double streamwiseVortInflow = WeatherUtils.streamwiseVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, inflowLayer[activeReadoutSet][0],
					inflowLayer[activeReadoutSet][1]);

			String streamwiseVort0_500_ = String.format("%6.3f", streamwiseVort0_500);
			String streamwiseVort0_1_ = String.format("%6.3f", streamwiseVort0_1);
			String streamwiseVort0_3_ = String.format("%6.3f", streamwiseVort0_3);
			String streamwiseVortInflow_ = String.format("%6.3f", streamwiseVortInflow);

			streamwiseVortInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : streamwiseVortInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SWV", streamwiseVort0_500_, streamwiseVort0_1_,
					streamwiseVort0_3_, streamwiseVortInflow_), (int) (890 * scale), (int) (670 * scale));

			double streamwiseness0_500 = WeatherUtils.streamwisenessOfVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 500);
			double streamwiseness0_1 = WeatherUtils.streamwisenessOfVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 1000);
			double streamwiseness0_3 = WeatherUtils.streamwisenessOfVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, 0, 3000);
			double streamwisenessInflow = WeatherUtils.streamwisenessOfVorticity(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind(), stormMotion, inflowLayer[activeReadoutSet][0],
					inflowLayer[activeReadoutSet][1]);

			String streamwiseness0_500_ = String.format("%5.1f", 100 * streamwiseness0_500) + "%";
			String streamwiseness0_1_ = String.format("%5.1f", 100 * streamwiseness0_1) + "%";
			String streamwiseness0_3_ = String.format("%5.1f", 100 * streamwiseness0_3) + "%";
			String streamwisenessInflow_ = String.format("%5.1f", 100 * streamwisenessInflow) + "%";

			streamwisenessInflow_ = (inflowLayer[activeReadoutSet][0] == -1024.0) ? "-" : streamwisenessInflow_;

			g.drawString(String.format("%-9s%-9s%-9s%-9s%-9s", "SW%", streamwiseness0_500_, streamwiseness0_1_,
					streamwiseness0_3_, streamwisenessInflow_), (int) (890 * scale), (int) (685 * scale));

			double threeCapeSb = WeatherUtils.computeThreeCape(soundingM.getPressureLevels(),
					soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.SURFACE_BASED);

			String threeCapeSb_ = String.format("%4d", (int) threeCapeSb);

			g.drawString(String.format("%-9s%-9s", "3CAPE-SB", threeCapeSb_), (int) (890 * scale), (int) (715 * scale));

			double threeCapeMl = WeatherUtils.computeThreeCape(soundingM.getPressureLevels(),
					soundingM.getTemperature(), soundingM.getDewpoint(), ParcelPath.MIXED_LAYER_100MB);

			String threeCapeMl_ = String.format("%4d", (int) threeCapeMl);

			g.drawString(String.format("%-9s%-9s", "3CAPE-ML", threeCapeMl_), (int) (890 * scale), (int) (730 * scale));

			String dcape_ = String.format("%4d", (int) dcape[activeReadoutSet]);

			g.drawString(String.format("%-9s%-9s", "DCAPE", dcape_), (int) (890 * scale), (int) (745 * scale));

			double[] ebwdVec = WeatherUtils.effectiveBulkWindDifference(soundingM.getPressureLevels(),
					soundingM.getHeight(), soundingM.getTemperature(), soundingM.getDewpoint(), soundingM.getUWind(),
					soundingM.getVWind());

			double ebwd = Math.hypot(ebwdVec[0], ebwdVec[1]);

			String ebwd_ = String.format("%4.1f", ebwd);

			g.drawString(String.format("%-9s%-9s", "EBWD", ebwd_), (int) (890 * scale), (int) (760 * scale));

			double scp = WeatherUtils.supercellComposite(soundingM.getPressureLevels(), soundingM.getHeight(),
					soundingM.getTemperature(), soundingM.getDewpoint(), soundingM.getUWind(), soundingM.getVWind(),
					stormMotion);

			String scp_ = String.format("%4.1f", scp);

			g.drawString(String.format("%-9s%-9s", "SCP", scp_), (int) (890 * scale), (int) (790 * scale));

			double stp = WeatherUtils.significantTornadoParameter(soundingM.getPressureLevels(), soundingM.getHeight(),
					soundingM.getTemperature(), soundingM.getDewpoint(), soundingM.getUWind(), soundingM.getVWind(),
					stormMotion);

			String stp_ = String.format("%4.1f", stp);

			g.drawString(String.format("%-9s%-9s", "SIGTOR", stp_), (int) (890 * scale), (int) (805 * scale));

			double ship = WeatherUtils.significantHailParameter(soundingM.getPressureLevels(),
					soundingM.getTemperature(), soundingM.getDewpoint(), soundingM.getUWind(), soundingM.getVWind());

			String ship_ = String.format("%4.1f", ship);

			g.drawString(String.format("%-9s%-9s", "SHIP", ship_), (int) (890 * scale), (int) (820 * scale));

			double devtor = WeatherUtils.deviantTornadoParameter(stp, stormMotion, WeatherUtils.deviantTornadoMotion(
					soundingM.getPressureLevels(), soundingM.getHeight(), soundingM.getUWind(), soundingM.getVWind(), stormMotion));

			System.out.println(Arrays.toString(WeatherUtils.deviantTornadoMotion(
					soundingM.getPressureLevels(), soundingM.getHeight(), soundingM.getUWind(), soundingM.getVWind(), stormMotion)));
			
			String devtor_ = String.format("%4.1f", devtor);

			g.drawString(String.format("%-9s%-9s", "DEVTOR", devtor_), (int) (890 * scale), (int) (835 * scale));

			return chart;
		}

		private BufferedImage drawSkewT(double scale) {
			return drawSkewT(scale, pathType);
		}

		private BufferedImage drawSkewT(double scale, ParcelPath pathType) {
			BufferedImage skewT = new BufferedImage((int) (800 * scale), (int) (800 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = skewT.createGraphics();

			double[] pressure = soundingM.getPressureLevels();
			double[] temperature = soundingM.getTemperature();
			double[] wetbulb = soundingM.getWetbulb();
			double[] dewpoint = soundingM.getDewpoint();

			double[] virtTemp = new double[dewpoint.length];

			for (int i = 0; i < virtTemp.length; i++) {
				virtTemp[i] = WeatherUtils.virtualTemperature(temperature[i], dewpoint[i], pressure[i]);
			}

			g.setStroke(thickStroke);

			// choosing active storm motion vector
			String stormMotionVectorText = "";
			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind());

			switch (stormMotionVector) {
			case 0:
				stormMotionVectorText = "[LM]";
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 1:
				stormMotionVectorText = "[MW]";
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 2:
				stormMotionVectorText = "[RM]";
				break;
			case 3:
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
				double[] parcelPressure = new double[parcelPathDowndraft.size()];
				double[] parcelTemperature = new double[parcelPathDowndraft.size()];
				double[] parcelDewpoint = new double[parcelPathDowndraft.size()];
				double[] parcelVirtualTemperature = new double[parcelPathDowndraft.size()];

				for (int i = 0; i < parcelPathDowndraft.size(); i++) {
					parcelPressure[i] = parcelPathDowndraft.get(i).getPressure();
					parcelTemperature[i] = parcelPathDowndraft.get(i).getTemperature();
					parcelDewpoint[i] = parcelPathDowndraft.get(i).getDewpoint();
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

			g.setColor(new Color(127, 127, 255));
			{
				int x = (int) linScale(-50, 50, 0, 800 * scale, 0);
				g.drawLine((int) (x + 400 * scale), 0, x, (int) (800 * scale));
			}

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

				double srhRmInflow = WeatherUtils.stormRelativeHelicity(soundingM.getPressureLevels(),
						soundingM.getHeight(), soundingM.getUWind(), soundingM.getVWind(), stormMotion,
						inflowLayer[activeReadoutSet][0], inflowLayer[activeReadoutSet][1]);

				String effInflowHlcy = String.format("%4d m²/s² " + stormMotionVectorText, (int) srhRmInflow);
				drawLeftCenterAlignedString(effInflowHlcy, g, (int) ((xInf + 5) * scale),
						(int) ((yTop + yBtm) / 2 * scale));
			}

			// parcel path
			ArrayList<RecordAtLevel> parcelPath = null;

			switch (pathType) {
			case SURFACE_BASED:
				parcelPath = parcelPathSurfaceBased;
				break;
			case MIXED_LAYER_50MB:
				parcelPath = parcelPathMixedLayer50Mb;
				break;
			case MIXED_LAYER_100MB:
				parcelPath = parcelPathMixedLayer100Mb;
				break;
			case MOST_UNSTABLE:
				parcelPath = parcelPathMostUnstable;
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

			return skewT;
		}

		private BufferedImage drawHodograph(double scale) {
			BufferedImage hodograph = new BufferedImage((int) (400 * scale), (int) (400 * scale),
					BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = hodograph.createGraphics();
			g.setStroke(thickStroke);

			double[] pressure = soundingM.getPressureLevels();
			double[] height = soundingM.getHeight();
			double[] uWind = soundingM.getUWind();
			double[] vWind = soundingM.getVWind();

			int maxRadius = 0; // m s^-1

			assert uWind.length == vWind.length;
			for (int i = 0; i < uWind.length; i++) {
				if (pressure[i] > 10000) {
					double windSpeed = Math.hypot(uWind[i], vWind[i]);
					int windSpeedIntDiv10 = (int) Math.ceil(windSpeed / 10.0);

					maxRadius = Integer.max(maxRadius, windSpeedIntDiv10 * 10);
				}
			}

			double[] stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(soundingM.getPressureLevels(),
					soundingM.getUWind(), soundingM.getVWind());

			// choosing active storm motion vector
			switch (stormMotionVector) {
			case 0:
				stormMotion = WeatherUtils.stormMotionBunkersIDLeftMoving(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 1:
				stormMotion = WeatherUtils.stormMotionBunkersIDMeanWindComponent(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 2:
				stormMotion = WeatherUtils.stormMotionBunkersIDRightMoving(soundingM.getPressureLevels(),
						soundingM.getUWind(), soundingM.getVWind());
				break;
			case 3:
				break;
			}

			// rings
			g.setColor(new Color(64, 64, 64));
			for (int i = 0; i <= maxRadius; i += 10) {
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
			if (inflowLayer[1][0] != -1024.0) {
				double lowerInflowPressure = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1],
						inflowLayer[1][0]);
				double upperInflowPressure = WeatherUtils.pressureAtHeight(pressure[pressure.length - 1],
						inflowLayer[1][1]);

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

			double[] stormMotionRightMover = WeatherUtils.stormMotionBunkersIDRightMoving(pressure, uWind, vWind);

			double eWindRight = Math.cos(-windOffsetAngle) * stormMotionRightMover[0]
					- Math.sin(-windOffsetAngle) * stormMotionRightMover[1];
			double nWindRight = Math.sin(-windOffsetAngle) * stormMotionRightMover[0]
					+ Math.cos(-windOffsetAngle) * stormMotionRightMover[1];

			double xRight = linScale(-maxRadius, maxRadius, 0, 400, eWindRight);
			double yRight = linScale(-maxRadius, maxRadius, 0, 400, -nWindRight);

			// mark RM
			drawCenteredOutlinedString("RM", g, (int) (xRight * scale), (int) (yRight * scale),
					new Color(255, 128, 128));

			double[] stormMotionLeftMover = WeatherUtils.stormMotionBunkersIDLeftMoving(pressure, uWind, vWind);

			double eWindLeft = Math.cos(-windOffsetAngle) * stormMotionLeftMover[0]
					- Math.sin(-windOffsetAngle) * stormMotionLeftMover[1];
			double nWindLeft = Math.sin(-windOffsetAngle) * stormMotionLeftMover[0]
					+ Math.cos(-windOffsetAngle) * stormMotionLeftMover[1];

			double xLeft = linScale(-maxRadius, maxRadius, 0, 400, eWindLeft);
			double yLeft = linScale(-maxRadius, maxRadius, 0, 400, -nWindLeft);

			drawCenteredOutlinedString("LM", g, (int) (xLeft * scale), (int) (yLeft * scale), new Color(128, 128, 255));

			double[] stormMotionMeanWind = WeatherUtils.stormMotionBunkersIDMeanWindComponent(pressure, uWind, vWind);

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

			System.out.println(key);

			switch (key) {
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

	private static final String[] stormMotionVectorOptions = { "Left Moving", "Mean Wind", "Right Moving" };

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
	}

	private static String timeString(DateTime time) {
		return String.format("%04d-%02d-%02d %02d:%02dZ", time.getYear(), time.getMonthOfYear(), time.getDayOfMonth(),
				time.getHourOfDay(), time.getMinuteOfHour());
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

	// turns a motion vector into a direction/magnitude reading
	// offset in radians
	private static String windReading(double[] vector, double offset) {
		double direction = Math.toDegrees(Math.atan2(-vector[0], -vector[1])) + 360;
		direction %= 360;

		double magnitude = Math.hypot(vector[0], vector[1]);

		return String.format("%3d/%02d", (int) direction, (int) Math.round(magnitude));
	}
}
