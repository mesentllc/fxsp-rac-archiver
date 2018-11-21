package com.fedex.smartpost.rac.archiver;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class AppTest {
	private int racCount(File[] files) {
		int count = 0;
		
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".pkgdtl") || filename.endsWith(".mandtl") || filename.endsWith(".taxdtl")) {
				count++;
			}
		}
		return count;
	}
	
	private int zipCount(File[] files) {
		int count = 0;
		
		for (File file : files) {
			String filename = file.getName();
			if (filename.endsWith(".zip")) {
				count++;
			}
		}
		return count;
	}
	
	@Test
	public void testApp() {
		String[] args = {"-d","15","-m","6","/tmp/fxsp-rac-archiver-test"};
		PrepareFiles prep = new PrepareFiles();
		File directory;
		
		prep.setup();
		App.main(args);
		directory = new File(args[4]);
		File[] files = directory.listFiles();
		Assert.assertEquals(52, files.length);
		Assert.assertEquals(45, racCount(files));
		Assert.assertEquals(7, zipCount(files));
		args[1] = "12";
		App.main(args);
		directory = new File(args[4]);
		files = directory.listFiles();
		Assert.assertEquals(43, files.length);
		Assert.assertEquals(36, racCount(files));
		Assert.assertEquals(7, zipCount(files));
	}
}
