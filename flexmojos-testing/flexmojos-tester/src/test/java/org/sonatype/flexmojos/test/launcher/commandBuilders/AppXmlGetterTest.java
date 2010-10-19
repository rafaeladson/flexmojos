package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AppXmlGetterTest {
	
	private AppXmlGetter appXmlGetter = new AppXmlGetter();
	
	
	@Test(groups={"file", "integration"})
	public void testCreateDocument() throws Exception {
		
		File appXml = new File( "new-app.xml");
		if ( appXml.exists() ) {
			appXml.delete();
		}
		
		File generatedAppXml = appXmlGetter.getAppXml(appXml.getAbsolutePath(), "something.swf");
		Assert.assertTrue( generatedAppXml.exists() );
		
		String appXmlContents = FileUtils.fileRead(generatedAppXml);
		Assert.assertTrue(appXmlContents.contains("<content>something.swf</content>") );
		
	}
	
	@Test(groups={"file", "integration"})
	public void testRunWithFileThatExists() throws Exception {
		
		File appXml = new File( "existing-app.xml" );
		if ( !appXml.exists() ) {
			appXml.createNewFile();
		}
		Assert.assertEquals( FileUtils.fileRead(appXml), "");
		
		File xmlThatShouldBeEmpty = appXmlGetter.getAppXml(appXml.getAbsolutePath(), "something.swf");
		Assert.assertEquals( FileUtils.fileRead(xmlThatShouldBeEmpty), "");
	}
	
	
	
	

}
