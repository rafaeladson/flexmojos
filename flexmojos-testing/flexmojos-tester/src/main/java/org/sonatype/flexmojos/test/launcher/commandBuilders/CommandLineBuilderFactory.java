package org.sonatype.flexmojos.test.launcher.commandBuilders;

import org.sonatype.flexmojos.commons.OSDetector;
import org.sonatype.flexmojos.commons.ProjectType;

public class CommandLineBuilderFactory {
	
	
	public CommandLineBuilder getCommandLineBuilder(ProjectType projectType, OSDetector osDetector) {
		
		switch( projectType ) {
			case FLEX:
			case FLEX_LIBRARY:
				return new FlexCommandLineBuilder(osDetector);
			case AIR:
			case AIR_LIBRARY:
				return new AirCommandLineBuilder(osDetector);
			default:
				throw new IllegalArgumentException( "Unsupported project type: " + projectType );
		}
		
	}

}
