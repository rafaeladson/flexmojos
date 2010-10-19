package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.sonatype.flexmojos.commons.OSDetector.OS;
import org.sonatype.flexmojos.test.launcher.OSDetectorStub;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class FlexCommandLineBuilderTest {
	
	private static final String SAMPLE_SWF_FILE_NAME = "/Users/usuario/teste.swf";
	
	@Test(dataProvider="buildCommandLineData")
	public void testBuildCommandLine(OS os, String userDefinedFlashPlayerCommand, String expectedCommand) throws FileNotFoundException, IOException {
		
		CommandLineBuilder flexCommandLineBuilder = new FlexCommandLineBuilder(new OSDetectorStub(os));
		String[] commandLineParameters = flexCommandLineBuilder.buildCommandLine(new File(SAMPLE_SWF_FILE_NAME), userDefinedFlashPlayerCommand);
		Assert.assertEquals( commandLineParameters[0], expectedCommand );
		Assert.assertEquals( commandLineParameters[1], SAMPLE_SWF_FILE_NAME);
		
	}
	
	@DataProvider
	public Object[][] buildCommandLineData() {
		Object[][] testData = new Object[5][];
		
		testData[0] = new Object[] { OS.windows, null, "FlashPlayer.exe" };
		testData[1] = new Object[] { OS.linux, null, "flashplayer"};
		testData[2] = new Object[] { OS.mac, null, "Flash Player" };
		testData[3] = new Object[] { OS.mac, "user-defined", "user-defined"};
		testData[4] = new Object[] { OS.windows, "${flashplayer.command}", "FlashPlayer.exe" };
		
		return testData;
	}
	
		
}
