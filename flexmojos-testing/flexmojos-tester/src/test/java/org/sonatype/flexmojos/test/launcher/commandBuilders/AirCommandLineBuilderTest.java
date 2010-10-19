package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;
import java.io.IOException;

import org.easymock.EasyMock;
import org.sonatype.flexmojos.commons.OSDetector.OS;
import org.sonatype.flexmojos.test.launcher.OSDetectorStub;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AirCommandLineBuilderTest {
	
	
	@Test(dataProvider="testBuildCommandLineData")
	public void testBuildCommandLine( String userDefinedCommand, OS os, String expectedCommand ) throws IOException {
		
		IAppXmlGetter mock = EasyMock.createMock(IAppXmlGetter.class);
		EasyMock.expect(mock.getAppXml("/tmp/teste-app.xml", "teste.swf")).andReturn(new File( "/tmp/teste-app.xml" ));
		EasyMock.replay(mock);
		
		AirCommandLineBuilder commandLineBuilder = new AirCommandLineBuilder(new OSDetectorStub(os));
		commandLineBuilder.setAppXmlGetter(mock);
		
		String[] commandLine = commandLineBuilder.buildCommandLine(new File( "/tmp/teste.swf"), userDefinedCommand );
		Assert.assertEquals( commandLine[0], expectedCommand);
		Assert.assertEquals( commandLine[1], "/tmp/teste-app.xml");
		
		EasyMock.verify( mock );
		
		
	}
	
	
	@DataProvider
	public Object[][] testBuildCommandLineData() {
		Object[][] testData = new Object[4][];
		
		testData[0] = new Object[] { null, OS.windows, "adl.exe" };
		testData[1] = new Object[] { null, OS.linux, "adl"};
		testData[2] = new Object[] { null, OS.mac, "adl" };
		testData[3] = new Object[] { "adl-user-defined", OS.mac, "adl-user-defined" };
		
		
		return testData;
	}
	
	
	@Test(dataProvider="testGetAppXmlNameFromSwfData")
	public void testGetAppXmlNameFromSwf(String swfName, String expectedAppXmlName) {
		AirCommandLineBuilder commandLineBuilder = new AirCommandLineBuilder(new OSDetectorStub(OS.linux));
		Assert.assertEquals( commandLineBuilder.getAppXmlNameFromSwf(swfName), expectedAppXmlName );
		
	}
	
	@DataProvider
	public Object[][] testGetAppXmlNameFromSwfData() {
		Object[][] testData = new Object[4][];
		
		testData[0] = new Object[] { "teste.swf", "teste-app.xml" };
		testData[1] = new Object[] { "/Users/usuario/teste.swf", "/Users/usuario/teste-app.xml" };
		testData[2] = new Object[] { "C:\\Users\\usuario\\teste.swf", "C:\\Users\\usuario\\teste-app.xml" };
		testData[3] = new Object[] { "a.swf", "a-app.xml" };
		
		return testData;
	}
	
	@Test(expectedExceptions=IllegalArgumentException.class)
	public void testGetAppXmlFromNonSwf() {
		AirCommandLineBuilder commandLineBuilder = new AirCommandLineBuilder(new OSDetectorStub(OS.linux));
		commandLineBuilder.getAppXmlNameFromSwf("blah.txt");
	}
	
	@Test(expectedExceptions=NullPointerException.class)
	public void testGetAppXmlFromNull() {
		AirCommandLineBuilder commandLineBuilder = new AirCommandLineBuilder(new OSDetectorStub(OS.linux));
		commandLineBuilder.getAppXmlNameFromSwf(null);
	}

}
