package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.commons.OSDetector;

final class AirCommandLineBuilder implements CommandLineBuilder {
	
	private final OSDetector osDetector;
	private IAppXmlGetter appXmlGetter;
	
	
	public AirCommandLineBuilder(OSDetector osDetector) {
		super();
		this.osDetector = osDetector;
		this.appXmlGetter = new AppXmlGetter();
	}

	public String[] buildCommandLine(File targetSwf, String userDefinedCommand) throws IOException {
		String airCommand = (userDefinedCommand == null || "${flashplayer.command}".equals( userDefinedCommand ) ) ? 
				FlashRuntime.getForOS(osDetector.getOSType()).getAirCommand() : userDefinedCommand;
		File appXmlFile = appXmlGetter.getAppXml(getAppXmlNameFromSwf(targetSwf.getAbsolutePath()), FileUtils.basename(targetSwf.getAbsolutePath()) + "swf");
		return new String[] { airCommand, appXmlFile.getAbsolutePath() };
	}
	
	public String getAppXmlNameFromSwf( final String swfName ) {
		
		
		int extensionIndex = swfName.indexOf(".swf");
		if ( extensionIndex > 0 ) {
			String appXmlFileName = swfName.substring(0, extensionIndex) + "-app.xml";
			return appXmlFileName;
			
		}
		else {
			throw new IllegalArgumentException("targetSwf file should end with .swf");
		}
	}

	void setAppXmlGetter(IAppXmlGetter appXmlGetter) {
		this.appXmlGetter = appXmlGetter;
	}
	
	
	
	

}
