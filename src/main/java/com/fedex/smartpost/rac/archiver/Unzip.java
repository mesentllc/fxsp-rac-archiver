package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;

public class Unzip {
	private void explode(ZipInputStream zin, ZipEntry entry, String directory) throws Exception {
		byte[] buf = new byte[65536];

		System.out.println("UnZipping " + entry.getName());
		createDirectoryIfNecessary(directory);
		if (!directory.endsWith(File.separator)) {
			directory += File.separator;
		}
		FileOutputStream fos = new FileOutputStream(directory + entry.getName());
		int len;
		try {
			while ((len = zin.read(buf, 0, 65536)) > -1) {
				fos.write(buf, 0, len);
			}
		}
		catch (ZipException ze) {
			System.out.println(ze.getMessage());
		}
		fos.close();
	}

	private void createDirectoryIfNecessary(String directory) throws Exception {
		File file = new File(directory);

		if (directory.endsWith(File.separator)) {
			directory = directory.substring(0,directory.length() - 1);
		}
		if (file.exists()) {
			if (file.isDirectory()) {
				return;
			}
			else {
				throw new Exception("A file with the name " + directory + " already exists, can't make the directory.");
			}
		}
		else {
			file.mkdir();
		}
	}

	private void unzipProcess(String zipFile, String directory, String racId) throws Exception {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			if (name.endsWith(".pkgdtl")) {
				if (StringUtils.isNotEmpty(racId)) {
					if (name.startsWith(racId)) {
						explode(zin, entry, directory);
					}
				}
				else {
					explode(zin, entry, directory);
				}
			}
			else {
				System.out.println("Skipping: " + name);
			}
			try {
				zin.closeEntry();
			}
			catch (ZipException ze) {
				System.out.println(ze.getMessage());
			}
			entry = zin.getNextEntry();
		}
		zin.close();
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("Usage: uzip {zipfile} {directory} [rac id]");
			System.exit(1);
		}
		Unzip unzip = new Unzip();
		unzip.unzipProcess(args[0], args[1], args[2]);
		System.exit(0);
	}
}
