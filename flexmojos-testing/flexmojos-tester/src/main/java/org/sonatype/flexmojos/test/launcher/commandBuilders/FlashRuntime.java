package org.sonatype.flexmojos.test.launcher.commandBuilders;

import org.sonatype.flexmojos.commons.OSDetector.OS;

enum FlashRuntime {
	WINDOWS_FLASH_RUNTIME("FlashPlayer.exe", "adl.exe"),
	MAC_FLASH_RUNTIME("Flash Player", "adl"),
	LINUX_FLASH_RUNTIME("flashplayer", "adl");
	
	private FlashRuntime(String flexCommand, String airCommand) {
		this.flexCommand = flexCommand;
		this.airCommand = airCommand;
	}

	private final String flexCommand;
	private final String airCommand;
	

	public String getFlexCommand() {
		return flexCommand;
	}
	
	String getAirCommand() {
		return airCommand;
	}



	public static FlashRuntime getForOS( OS os ) {
		switch( os ) {
		case windows:
			return WINDOWS_FLASH_RUNTIME;
		case mac:
			return MAC_FLASH_RUNTIME;
		case linux:
		case solaris:
		case unix:
		case other:
			return LINUX_FLASH_RUNTIME;
		default:
			throw new IllegalArgumentException( "Unsupported operating system: " + os.toString() );
		}
		
	}
	
	
}