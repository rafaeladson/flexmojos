package org.sonatype.flexmojos.test.launcher;

import org.sonatype.flexmojos.commons.OSDetector;

public class OSDetectorStub extends OSDetector {
	
	private final OS osToReturn;

	public OSDetectorStub(OS osToReturn) {
		super();
		this.osToReturn = osToReturn;
	}
	
	@Override
	public OS getOSType() {
		return osToReturn;
	}
	
	@Override
	public boolean isHeadless() {
		return true;
	}

}
