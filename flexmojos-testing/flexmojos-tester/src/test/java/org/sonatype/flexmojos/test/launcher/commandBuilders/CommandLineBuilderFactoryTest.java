package org.sonatype.flexmojos.test.launcher.commandBuilders;

import org.sonatype.flexmojos.commons.OSDetector.OS;
import org.sonatype.flexmojos.commons.ProjectType;
import org.sonatype.flexmojos.test.launcher.OSDetectorStub;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CommandLineBuilderFactoryTest {
	
	private final CommandLineBuilderFactory factory = new CommandLineBuilderFactory();
	
	@Test(dataProvider="testGetCommandLineData")
	public void testGetCommandLineBuilder(ProjectType projectType, Class<? extends CommandLineBuilder> expectedClass) {
		Assert.assertTrue( factory.getCommandLineBuilder(projectType, new OSDetectorStub(OS.windows)).getClass().equals(expectedClass));
	}
	
	@DataProvider
	public Object[][] testGetCommandLineData() {
		Object[][] testData = new Object[4][];
		
		testData[0] = new Object[] { ProjectType.FLEX, FlexCommandLineBuilder.class};
		testData[1] = new Object[] { ProjectType.FLEX_LIBRARY, FlexCommandLineBuilder.class};
		testData[2] = new Object[] { ProjectType.AIR, AirCommandLineBuilder.class };
		testData[3] = new Object[] { ProjectType.AIR_LIBRARY, AirCommandLineBuilder.class };
		
		
		return testData;
	}
}
