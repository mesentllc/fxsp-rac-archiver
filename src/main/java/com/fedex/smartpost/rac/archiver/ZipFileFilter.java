package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ZipFileFilter implements FilenameFilter {
    private Calendar expireDate = null;
    
	private boolean expired(String filename) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMM");
		String truncName = filename.substring(0, filename.length() - 4);
		
		try {
			Date date = sdf.parse(truncName);
			cal.setTimeInMillis(date.getTime());
			return expireDate.after(cal);
		}
		catch (ParseException pe) {
			return false;
		}
	}
	
    public ZipFileFilter(Calendar expireAfter) {
		expireAfter.set(Calendar.HOUR_OF_DAY, 0);
		expireAfter.set(Calendar.MINUTE, 0);
		expireAfter.set(Calendar.SECOND, 0);
		expireAfter.set(Calendar.MILLISECOND, 0);
		expireAfter.set(Calendar.DATE, 1);
		this.expireDate = expireAfter;
    }
    
    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
		String filename = file.getName();
        return (file.isFile() && filename.endsWith(".zip") && expired(filename));
    }
}
