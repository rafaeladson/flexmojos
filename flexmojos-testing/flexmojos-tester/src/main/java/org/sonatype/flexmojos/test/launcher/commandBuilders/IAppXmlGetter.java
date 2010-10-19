package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;
import java.io.IOException;

interface IAppXmlGetter {
	
	File getAppXml( String appXmlFileName, String swfFileName) throws IOException;

}
