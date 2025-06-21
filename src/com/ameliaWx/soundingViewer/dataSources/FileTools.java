package com.ameliaWx.soundingViewer.dataSources;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ameliaWx.soundingViewer.GlobalVars.cacheFolder;
import static com.ameliaWx.soundingViewer.GlobalVars.dataFolder;

public class FileTools {
    public static File downloadFile(String url, String fileName) throws IOException {
        URL dataURL = new URL(url);

        File dataDir = new File(dataFolder);
        dataDir.mkdirs();
        InputStream is = dataURL.openStream();

        OutputStream os = Files.newOutputStream(Paths.get(dataFolder + fileName));
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

    public static File downloadFileToCache(String url, String fileName) throws IOException {
        URL dataURL = new URL(url);

        File dataDir = new File(cacheFolder);
        dataDir.mkdirs();
        InputStream is = dataURL.openStream();

        OutputStream os = Files.newOutputStream(Paths.get(cacheFolder + fileName));
        byte[] buffer = new byte[16 * 1024];
        int transferredBytes = is.read(buffer);
        while (transferredBytes > -1) {
            os.write(buffer, 0, transferredBytes);
            transferredBytes = is.read(buffer);
        }
        is.close();
        os.close();

        return new File(cacheFolder + fileName);
    }

    /*
     * Example of reading Zip archive using ZipFile class
     */

    public static void readUsingZipFile(String fileName, String outputDir) throws IOException {
        new File(outputDir).mkdirs();
        final ZipFile file = new ZipFile(fileName);
        // System.out.println("Iterating over zip file : " + fileName);

        try {
            final Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                extractEntry(entry, file.getInputStream(entry), outputDir);
            }
            // System.out.printf("Zip file %s extracted successfully in %s",
            // fileName, outputDir);
        } finally {
            file.close();
        }
    }

    /*
     * Utility method to read data from InputStream
     */

    public static void extractEntry(final ZipEntry entry, InputStream is, String outputDir) throws IOException {
        String exractedFile = outputDir + entry.getName();
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(exractedFile);
            final byte[] buf = new byte[8192];
            int length;

            while ((length = is.read(buf, 0, buf.length)) >= 0) {
                fos.write(buf, 0, length);
            }

        } catch (IOException ioex) {
            fos.close();
        }

    }

    public static String usingBufferedReader(File filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static List<String> listFilesInWebFolder(String url, int amt) throws IOException {
        File index = downloadFile(url, "index.html");

        ArrayList<String> files = new ArrayList<String>();

        Pattern p = Pattern.compile("<td><a href=.*?>");
        Matcher m = p.matcher(usingBufferedReader(index));

        while (m.find()) {
            String group = m.group();
            String filename = group.substring(13, m.group().length() - 2);

            if (filename.endsWith(".zip")) {
                files.add(filename);
            }

            if (files.size() >= amt && amt != -1) {
                break;
            }
        }

        index.delete();

        return files;
    }
}
