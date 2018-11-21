package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class App {
	private final ZipUtilities zipUtilities = new ZipUtilities();
	private boolean verbose = false;

	private static boolean lockInstance(final String lockFile) {
		try {
			final File file = new File(lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							fileLock.release();
							randomAccessFile.close();
							file.delete();
						} 
						catch (IOException e) {
							System.err.println("Unable to remove lock file: " + lockFile + e.getMessage());
						}
					}
				});
				return true;
			}
		} 
		catch (IOException e) {
			System.err.println("Unable to create and/or lock file: " + lockFile + e.getMessage());
		}
		return false;
	}
	
	private void showHelp() {
		System.out.println("Usage: fxsp-rac-archiver -d xxx -m y <base path>");
		System.out.println("\txxx = Number of days of history to keep before archiving");
		System.out.println("\ty = Number of months of history to keep before deleting");
		System.out.println("\t<base path> = Path where the files are");
		System.exit(-1);
	}
	
	private boolean inputValid(String[] args) {
		boolean daysFound = false;
		boolean monthFound = false;
		int argPtr = 0;

		try {
			while (argPtr < args.length) {
				if ("-v".equals(args[argPtr])) {
					verbose = true;
				}
				if ("-d".equals(args[argPtr])) {
					if (Integer.parseInt(args[argPtr++]) > 0) {
						daysFound = true;
						argPtr++;
					}
				}
				if ("-m".equals(args[argPtr])) {
					if (Integer.parseInt(args[argPtr++]) > 0) {
						monthFound = true;
						argPtr++;
					}
				}
				argPtr++;
			}
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return daysFound && monthFound;
	}
	
	private int getNumberFromArgs(String key, String[] args) {
		int argPtr = 0;
		
		while (argPtr < args.length) {
			if (key.equals(args[argPtr])) {
				return Integer.parseInt(args[argPtr + 1]);
			}
			argPtr++;
		}
		showHelp();
		return Integer.MAX_VALUE;
	}
	
	private Map<String,List<File>> buildZipMap(File[] files) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMM");
		Map<String,List<File>> zipMap = new HashMap<String,List<File>>();
		List<File> fileList;
		
		if ((files != null) && (files.length > 0)) {
			for (File file : files) {
				String zipFileName = sdf.format(file.lastModified());
				if (zipMap.containsKey(zipFileName)) {
					fileList = zipMap.get(zipFileName);
				}
				else {
					fileList = new ArrayList<File>();
					zipMap.put(zipFileName, fileList);
				}
				fileList.add(file);
			}
		}
		return zipMap;
	}
	
	private void zipToMonthZipFiles(String rootPath, Map<String,List<File>> zipFileMap) {
		for (Entry entry : zipFileMap.entrySet()) {
			File zipFile = new File(rootPath, entry.getKey() + ".zip");
			List<File> fileList = (List<File>)entry.getValue();
			try {
				if (zipFile.exists()) {
					zipUtilities.addFilesToExistingZip(zipFile, fileList);
				}
				else {
					zipUtilities.createNewZip(zipFile, fileList);
				}
				deleteFiles(fileList);
			}
			catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
	}
	
	private void deleteFiles(List<File> files) {
		for (File file : files) {
			System.out.println("Purging " + file.getName());
			file.delete();
		}
	}
	
	private void processRacFiles(int daysBack, String rootPath) {
		File[] files;
		Map<String,List<File>> zipFileMap;
		Calendar calThreshold = Calendar.getInstance();
		
		calThreshold.set(Calendar.HOUR_OF_DAY, 0);
		calThreshold.set(Calendar.MINUTE, 0);
		calThreshold.set(Calendar.SECOND, 0);
		calThreshold.set(Calendar.MILLISECOND, 0);
		calThreshold.add(Calendar.DATE, -daysBack);
		files = new File(rootPath).listFiles(new RacFileFilter(calThreshold.getTimeInMillis()));
		zipFileMap = buildZipMap(files);
		zipToMonthZipFiles(rootPath, zipFileMap);
	}
	
	private void purgeExpiredZips(int monthsBack, String rootPath) {
		Calendar calThreshold = Calendar.getInstance();
		List<File> fileList = new ArrayList<File>(); 

		calThreshold.add(Calendar.MONTH, -monthsBack);
		File[] files = new File(rootPath).listFiles(new ZipFileFilter(calThreshold));
		fileList.addAll(Arrays.asList(files));
		deleteFiles(fileList);
	}
		
	private void processRequest(String[] args) {
		int daysBack;
		int monthsBack;
		
		if (!inputValid(args)) {
			showHelp();
		}
		daysBack = getNumberFromArgs("-d", args);
		monthsBack = getNumberFromArgs("-m", args);
		System.out.println("Running with arguments:");
		System.out.println("\tUse file path: " + args[4]);
		System.out.println("\tDays to keep: " + daysBack);
		System.out.println("\tMonths to keep: " + monthsBack);
		System.out.println();
		System.out.println("Readng files to Zip...");
		processRacFiles(daysBack, args[4]);
		System.out.println("Readng Zips to Purge...");
		purgeExpiredZips(monthsBack, args[4]);
	}
	
	public static void main(String[] args) {
		if (lockInstance("archiveRunning")) {
			App app = new App();
			app.processRequest(args);
		}
	}
}
