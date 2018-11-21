package com.fedex.smartpost.rac.archiver;

import java.io.File;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;

public class RacFileFilterTest {
	@Test
	public void testExpired() {
		Calendar cal = Calendar.getInstance();
		PrepareFiles prep = new PrepareFiles();
		
		prep.setup();
		File file = new File("/tmp/fxsp-rac-archiver-test");
		cal.add(Calendar.DATE, -15);
		File[] files = file.listFiles(new RacFileFilter(cal.getTimeInMillis()));
		Assert.assertEquals(75, files.length);
	}
}
