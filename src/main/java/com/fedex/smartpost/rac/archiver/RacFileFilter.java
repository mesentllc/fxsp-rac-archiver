package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.io.FilenameFilter;

public class RacFileFilter implements FilenameFilter {
    private long fileAge = 0;
    
    public RacFileFilter(long fileAge) {
        this.fileAge = fileAge;
    }
    
    public boolean accept(File dir, String name) {
        File file = new File(dir, name);
        long fileModified = file.lastModified();
        return (file.isFile() && (fileModified < fileAge) && 
				(file.getName().endsWith(".pkgdtl") || file.getName().endsWith(".mandtl") ||
				 file.getName().endsWith(".taxdtl")));
    }
}
