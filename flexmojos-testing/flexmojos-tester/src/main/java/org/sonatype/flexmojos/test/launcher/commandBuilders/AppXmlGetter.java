package org.sonatype.flexmojos.test.launcher.commandBuilders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

public class AppXmlGetter implements IAppXmlGetter {

	public File getAppXml(String appXmlFileName, String swfFileName) throws IOException {
		File appXmlFile = new File( appXmlFileName );
		if ( !appXmlFile.exists() ) {
			createAppXml( appXmlFile, swfFileName);
			
		}
		return appXmlFile;
	}

	private void createAppXml(File appXmlFile, String swfFileName) throws IOException{
		
		try {
			
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.ENCODING_DEFAULT, "UTF-8");

			Properties props = new Properties();
			props.put("resource.loader","class");
			props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			ve.init( props );
			

			final String templatePath = "templates/main-appxml.vm";
			
			InputStream input = this.getClass().getClassLoader().getResourceAsStream(templatePath);
			if (input == null) {
				throw new IOException("Template file doesn't exist");
			}

			VelocityContext context = new VelocityContext();
			context.put("targetSwf", swfFileName);
			
			Writer writer = new PrintWriter(appXmlFile, "UTF-8");
			InputStreamReader reader = new InputStreamReader(input, "UTF-8");

			if (!ve.evaluate(context, writer, templatePath, reader)) {
				throw new IOException("Failed to transform velocity template into XML");
			}
			writer.flush();
			writer.close();
		} catch( Exception e ) {
			throw new IOException( e );
		}

		
		
	}

}
