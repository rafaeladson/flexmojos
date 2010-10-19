package org.sonatype.flexmojos.test.launcher;

import java.io.File;

import org.codehaus.plexus.logging.AbstractLogger;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.flexmojos.commons.OSDetector.OS;
import org.sonatype.flexmojos.commons.ProjectType;
import org.sonatype.flexmojos.test.TestRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AsVmLauncherUnitTest {
	
	
	private AsVmLauncherMock launcher;
	
	@BeforeMethod
	public void setUp() {
		launcher = new AsVmLauncherMock(); 
		
	}
	
	
	
	@Test(dataProvider="testStartData")
	public void testStart(TestRequest request, OS os, boolean headlessRunWasCalled, boolean normalRunWasCalled) throws Exception {
		
		launcher.setOsDetector(new OSDetectorStub(os));
		launcher.start(request);
		Assert.assertEquals( launcher.isHeadlessRun(), headlessRunWasCalled);
		Assert.assertEquals( launcher.isNormalRun(), normalRunWasCalled );
		
	}
	
	@DataProvider
	public Object[][] testStartData() {
		Object[][] testData = new Object[4][];
		
		TestRequest headlessRequest = createTestRequest(true);
		testData[0] = new Object[] { headlessRequest, OS.linux, true, false };
		
		TestRequest normalRequest = createTestRequest(false);
		testData[1] = new Object[] { normalRequest, OS.linux, false, true};
		
		testData[2] = new Object[] { headlessRequest, OS.mac, false, true};
		testData[3] = new Object[] { headlessRequest, OS.windows, false, true };
		
		return testData;
	}



	protected TestRequest createTestRequest(boolean headlessAllowed) {
		TestRequest normalRequest = new TestRequest();
		normalRequest.setAllowHeadlessMode(headlessAllowed);
		normalRequest.setSwf(new File("test.swf"));
		normalRequest.setProjectType(ProjectType.FLEX);
		return normalRequest;
	}


}


class AsVmLauncherMock extends AsVmLauncher {
	
	private boolean headlessRun = false;
	private boolean normalRun = false;
	
	@Override
	protected void runCommand(String[] command)
			throws LaunchFlashPlayerException {
		this.normalRun = true;
	}
	
	@Override
	protected void runCommandHeadless(String[] commandLineArguments)
			throws LaunchFlashPlayerException {
		this.headlessRun = true;
	}

	public boolean isHeadlessRun() {
		return headlessRun;
	}

	public boolean isNormalRun() {
		return normalRun;
	}
	
	@Override
	protected void checkTargetSwfExists(File targetSwf) {
		//do nothing -- avoiding this test.
	}
	
	@Override
	protected Logger getLogger() {
		return new LoggerStub();
	}
	
	
	
}

class LoggerStub extends AbstractLogger {

	public LoggerStub() {
		super(0, "aaa");
	}

	public void debug(String message, Throwable throwable) {
	}

	public void info(String message, Throwable throwable) {
	}

	public void warn(String message, Throwable throwable) {
	}

	public void error(String message, Throwable throwable) {
	}

	public void fatalError(String message, Throwable throwable) {
	}

	public Logger getChildLogger(String name) {
		return null;
	}

		
	
	
}
