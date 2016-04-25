package com.home.croaton.followme.instrumentation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// http://www.jondev.net/articles/Zipping_Files_with_Android_(Programmatically)
public class ZipUnZip {

    private static final int BUFFER = 2048;

    public static void zip(String[] filesToZipNames, String zipFileName) {
        try  {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for(int i=0; i < filesToZipNames.length; i++) {
                FileInputStream fi = new FileInputStream(filesToZipNames[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(filesToZipNames[i].substring(filesToZipNames[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void unzip(String zipFile, String location) {
        dirChecker("", location);

        try  {
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                if (ze.isDirectory()) {
                    dirChecker(ze.getName(), location);
                } else {
                    FileOutputStream fout = new FileOutputStream(location + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        } catch(Exception e) {
        }

    }

    private static void dirChecker(String dir, String location) {
        File f = new File(location + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }
}
