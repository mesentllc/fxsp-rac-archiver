package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtilities {
	private boolean willBeUpdated(String filename, List<File> files) {
		boolean found = false;
		for (File f : files) {
			if (f.getName().equals(filename)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	private void movePreExistingFilesToZip(ZipOutputStream zout, File tempFile, List<File> files) throws FileNotFoundException, IOException {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		byte[] buf = new byte[1024];
		
	    ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String filename = entry.getName();
			if (!willBeUpdated(filename, files)) {
	            zout.putNextEntry(new ZipEntry(filename));
	            int len;
		        while ((len = zin.read(buf)) > 0) {
			        zout.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		zin.close();
	}
	
	private void loadFilesToZip(ZipOutputStream zout, List<File> files) throws FileNotFoundException, IOException {
		byte[] buf = new byte[1024];

		for (File file : files) {
			System.out.println("Zipping " + file.getName());
		    InputStream in = new FileInputStream(file);
	        zout.putNextEntry(new ZipEntry(file.getName()));
	        int len;
		    while ((len = in.read(buf)) > 0) {
			    zout.write(buf, 0, len);
			}
	        zout.closeEntry();
		    in.close();
		}
	}
	
	public void addFilesToExistingZip(File zipFile, List<File> files) throws IOException {
		// Build a temp filename, we'll need to delete the file, and just to keep the handle
		// so we can rename the old zip file to this temporary file handle, which will be used
		// to maintain the previous zip file contents, since ZipOutputStream wipes out any
		// previous file with the same name... Then we can move the old contents back into the
		// new zip file.
		File tempFile = File.createTempFile(zipFile.getName(), null);
	    tempFile.delete();

		if (!zipFile.renameTo(tempFile)) {
			throw new RuntimeException("Could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
		movePreExistingFilesToZip(zout, tempFile, files);
		loadFilesToZip(zout, files);
		zout.finish();
		zout.close();
	    tempFile.delete();
	}
	
	public void createNewZip(File zipFile, List<File> files) throws IOException {
		ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));
		loadFilesToZip(zout, files);
		zout.finish();
		zout.close();
	}
}
