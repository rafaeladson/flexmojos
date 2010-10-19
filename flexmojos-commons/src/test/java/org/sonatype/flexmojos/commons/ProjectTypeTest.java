package org.sonatype.flexmojos.commons;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ProjectTypeTest {
	
	
	@Test(dataProvider="testGetProjectTypeData")
	public void testGetProjectType(FlexExtension extension, boolean isAir, boolean actionScriptOnly, ProjectType expectedProjectType )  {
		Assert.assertEquals( ProjectType.getProjectType(extension, isAir, actionScriptOnly), expectedProjectType);
	}
	
	@SuppressWarnings("unused")
	@DataProvider
	private Object[][] testGetProjectTypeData() {
		Object[][] testData = new Object[8][];
		
		testData[0] = new Object[] {FlexExtension.SWF, true, false, ProjectType.AIR};
		testData[1] = new Object[] {FlexExtension.AIR, true, false, ProjectType.AIR};
		testData[2] = new Object[] {FlexExtension.SWF, false, false, ProjectType.FLEX };
		testData[3] = new Object[] {FlexExtension.SWF, false, true, ProjectType.ACTIONSCRIPT };
		testData[4] = new Object[] {FlexExtension.SWC, true, false, ProjectType.AIR_LIBRARY };
		testData[5] = new Object[] {FlexExtension.SWC, false, false, ProjectType.FLEX_LIBRARY };
		
		//default cases 
		testData[6] = new Object[] {FlexExtension.ZIP, false, false, ProjectType.FLEX_LIBRARY };
		testData[7] = new Object[] {FlexExtension.SWF, true, true, ProjectType.FLEX_LIBRARY };
		
		return testData;
	}
	

}
