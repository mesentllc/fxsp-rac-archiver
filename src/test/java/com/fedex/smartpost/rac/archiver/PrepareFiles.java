package com.fedex.smartpost.rac.archiver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class PrepareFiles {
	private void prepareDirectory() {
		File directory = new File("/tmp/fxsp-rac-archiver-test");
		
		if (directory.exists()) {
			if (directory.isDirectory()) {
				File[] files = directory.listFiles();
				for (File f : files) {
					f.delete();
				}
			}
			else {
				directory.delete();
				directory.mkdir();
			}
		}
		else {
			directory.mkdirs();
		}
	}
	
	private void buildFileSet(int fileSeq, Calendar calendar) {
		File file;
		BufferedWriter bw;
		String[] postfix = {".pkgdtl", ".mandtl", ".taxdtl"};
		
		for (String string : postfix) {
			file = new File("/tmp/fxsp-rac-archiver-test", fileSeq + string);
			try {
				bw = new BufferedWriter(new FileWriter(file));
				bw.close();
			}
			catch (IOException ioe) {
			}
			file.setLastModified(calendar.getTimeInMillis());
		}
	}
	
	private void buildFiles() {
		Calendar cal = Calendar.getInstance();
		int dayOfMonth = cal.get(Calendar.DATE);

		cal.add(Calendar.MONTH, -10);
		for (int i = 0; i < 40; i++) {
			if (i < 10) {
				buildFileSet(i, cal);
				cal.add(Calendar.MONTH, 1);
			}
			else {
				if (i == 10) {
					cal.set(Calendar.DATE, dayOfMonth);
					cal.add(Calendar.DATE, -30);
				}
				buildFileSet(i, cal);
				cal.add(Calendar.DATE, 1);
			}
		}
	}
	
	public void setup() {
		prepareDirectory();
		buildFiles();
	}
}
