package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;

import org.sonatype.flexmojos.commons.OSDetector;

final class FlexCommandLineBuilder implements CommandLineBuilder {
	
	private final OSDetector osDetector;

	public FlexCommandLineBuilder(OSDetector osDetector) {
		super();
		this.osDetector = osDetector;
		
	}

	public String[] buildCommandLine(final File targetSwf, final String userDefinedFlashPlayerCommand) {
		String flashPlayerCommand = ( userDefinedFlashPlayerCommand == null || "${flashplayer.command}".equals( userDefinedFlashPlayerCommand )) ? FlashRuntime.getForOS(osDetector.getOSType()).getFlexCommand() : userDefinedFlashPlayerCommand;  
		
		String[] commandLine = new String[] { flashPlayerCommand, targetSwf.getAbsolutePath() };
		return commandLine;
		
	}
	
	
	
	
	
	

}
