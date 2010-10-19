package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;
import java.io.IOException;

public interface CommandLineBuilder {
	
	public String[] buildCommandLine(final File targetSwf, final String userDefinedFlashPlayerCommand ) throws IOException;

}
