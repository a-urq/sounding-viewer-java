# sounding-viewer-java
A Java program that visualizes a vertical profile of the atmosphere as a skew-T diagram, a hodograph, and a set of readouts such as CAPE, storm-relative helicity, and wind shear. Exists both as a standalone program and as an API. 

To download, follow instructions at https://github.com/a-urq/sounding-viewer-java/releases/tag/v1.0.0.

Be advised that integration with IGRA2 soundings from outside of the US is currently having issues.

# Dependencies
joda-time

WeatherUtils (https://github.com/a-urq/weather-utils-java)

# How to use the GUI
Double-click the JAR file or run it in your command prompt or terminal with no arguments and follow the dialog boxes.

# How to use the CLI
Run the JAR file in your command prompt or terminal using arguments. The arguments to be used are documented below.

`-c`: Specifies that the program should pull current data. This argument should always be used first.

`-h`: Specifies that the program should pull historical data. This argument should always be used first.

`-s`: Indicates that the following argument specifies a radiosonde site. The codes for each site can be found in <a href="https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-station-list.txt">the IGRA2 station list</a> or on <a href="https://www.spc.noaa.gov/exper/soundings/">the SPC Experimental Soundings page</a>.

`-d`: For historical data, specifies that the following argument is a date. The format YYYYMMDD-HH should be used. Always use UTC time.

If you want to get the current sounding for Fort Worth, Texas, type `java -jar RadiosondeViewer.jar -c -s KFWD`.

If you want to get the historical sounding for Norman, Oklahoma on February 27, 2022 at 03Z, type `java -jar RadiosondeViewer.jar -h -s KOUN -d 20230227-03`.

# How to use the API in your own Java projects

Include the JAR file in your project's build path. It is the same JAR file as the GUI and the CLI.

First, select a radiosonde site with `RadiosondeSite.findSite(String code);`, where the codes for each site can be found in <a href="https://www.ncei.noaa.gov/data/integrated-global-radiosonde-archive/doc/igra2-station-list.txt">the IGRA2 station list</a> or on <a href="https://www.spc.noaa.gov/exper/soundings/">the SPC Experimental Soundings page</a>.

To get a current sounding from within the Contiguous US, use `RadiosondeWrapper.displaySpcExperSounding(RadiosondeSite site)` and the program will display the most current sounding from the SPC Experimental Soundings page.

To get a historical sounding from anywhere in the world, use `RadiosondeWrapper.displaySounding(RadiosondeSite site, int year, int month, int day, int hour)` and the program will display the sounding from that site at either that time or the closest time found near the requested time. If no sounding is found, the method will throw a `RadiosondeNotFoundException` so you may write your own error handling. No specific error handling is needed, but it gives the programmer the option to show a GUI dialog box, a console message, or any other handling.

# How to Create soundings using your own data
Step 1: Enter your data into arrays of type `double[]` using SI units. Currently, the needed arrays are pressure, height, temperature, dewpoint, zonal (east-west) wind, and meridional (north-south) wind. Vertical velocity may be included but is optional. Enter your data in the arrays in order of increasing pressure. The lowest pressure (highest altitude) record should be contained in the first index of each array and the highest pressure (lowest altitude) record should be contained in the last index.

Take care not to use relative humidity in the dewpoint array. If you need, WeatherUtils.relativeHumidity(double temperature, double dewpoint) can convert your data.

Vertical velocity uses pressure coordinates rather than vertical distance directly, in accordance with the conventions of display used in other visualizers such as SharpPY. Ensure that your data is in units of Pa s^-1 instead of m s^-1. Positive values should correspond with downward motion and negative values should correspond with upward motion.

Step 2: Create a `Sounding` object using those arrays.

Step 3: Create a `SoundingFrame` object using that `Sounding` object and optionally additional information. This will show a window displaying the sounding chart.

```java
int numRecords = ...; // Number of records in your sounding data.

double[] pressure = new double[numRecords]; // Units: Pascals
double[] height = new double[numRecords]; // Units: Meters (both Above Sea Level and Above Ground Level will work)
double[] temperature = new double[numRecords]; // Units: Kelvins
double[] dewpoint = new double[numRecords]; // Units: Kelvins
double[] uWind = new double[numRecords]; // Units: m s^-1
double[] vWind = new double[numRecords]; // Units: m s^-1
double[] omega = new double[numRecords]; // Units: Pa s^-1, may be omitted if data is not present

// your code for entering the data into the arrays goes here
// remember to enter the lowest pressure (highest altitude) data in the first index and the highest pressure (lowest altitude) data in the last index.

Sounding sounding = new Sounding(pressure, temperature, dewpoint, height, uWind, vWind, omega);
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

![image](https://github.com/a-urq/sounding-viewer-java/assets/114271919/c3752d74-35af-473c-9182-737f31c87b67)

