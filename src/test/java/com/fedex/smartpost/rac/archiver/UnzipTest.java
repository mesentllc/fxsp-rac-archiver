package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class UnzipTest {
	private static final String destination = "C:\\2015_Jul\\";

	private void explode(ZipInputStream zin, ZipEntry entry) throws Exception {
		byte[] buf = new byte[65536];

		System.out.println("UnZipping " + entry.getName());
		createDirectoryIfNecessary(destination);
		FileOutputStream fos = new FileOutputStream(destination + entry.getName());
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

	@Test
	public void unzip() throws Exception {
		ZipInputStream zin = new ZipInputStream(new FileInputStream("c:\\2015_Jul.zip"));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			long racId = Long.parseLong(name.substring(0,name.indexOf(".")));
			if (name.endsWith(".pkgdtl") && (racId > 2786)) {
				explode(zin, entry);
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
}
