# sounding-viewer-java
A Java program that visualizes a vertical profile of the atmosphere as a skew-T diagram, a hodograph, and a set of readouts such as CAPE, storm-relative helicity, and wind shear.

# Dependencies
joda-time

WeatherUtils (https://github.com/a-urq/weather-utils-java)

# How to use in your own Java projects
Step 1: Enter your data into arrays of type `double[]` using SI units. Currently, the needed arrays are pressure, height, temperature, dewpoint, zonal (east-west) wind, and meridional (north-south) wind. Enter your data in the arrays in order of increasing pressure. The lowest pressure (highest altitude) record should be contained in the first index of each array and the highest pressure (lowest altitude) record should be contained in the last index.

Take care not to use relative humidity in the dewpoint array. If you need, WeatherUtils.relativeHumidity(double temperature, double dewpoint) can convert your data. I will likely soon be adding vertical velocity.

Step 2: Create a `Sounding` object using those arrays.

Step 3: Create a `Sounding Frame` object using that `Sounding` object and optionally additional information. This will show a window displaying the sounding chart.

```java
int numRecords = ...; // Number of records in your sounding data.

double[] pressure = new double[numRecords]; // Units: Pascals
double[] height = new double[numRecords]; // Units: Meters (both Above Sea Level and Above Ground Level will work)
double[] temperature = new double[numRecords]; // Units: Kelvins
double[] dewpoint = new double[numRecords]; // Units: Kelvins
double[] uWind = new double[numRecords]; // Units: m s^-1
double[] vWind = new double[numRecords]; // Units: m s^-1

// your code for entering the data into the arrays goes here

Sounding sounding = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind);
new SoundingFrame(sounding);
```

Alternatively, you can enter additional data into your sounding frame, which affects the title of the window. All following combinations work.

```java		
DateTime time = new DateTime(2023, 05, 19, 00, 0, DateTimeZone.UTC);

new SoundingFrame(sounding, time);

new SoundingFrame(sounding, 35.2331, -101.7092);

new SoundingFrame(sounding, time, 35.2331, -101.7092);

new SoundingFrame("Amarillo TX Weather Balloon", sounding);

new SoundingFrame("Amarillo TX Weather Balloon", sounding, time);

new SoundingFrame("Amarillo TX Weather Balloon", sounding, 35.2331, -101.7092);

new SoundingFrame("Amarillo TX Weather Balloon", sounding, time, 35.2331, -101.7092);
```

I have also made a MapInset interface that allows for a map to be shown in the upper-rightmost frame. RadarView uses it to display radar data near the sounding to give context for the storm's environment. I plan to write a guide on how to use that interface later on.

![image](https://github.com/a-urq/sounding-viewer-java/assets/114271919/034994f7-5952-470c-a72c-bf3367d174dd)
