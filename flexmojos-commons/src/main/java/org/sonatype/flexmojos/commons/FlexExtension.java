/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.commons;

public enum FlexExtension
{
	AIR( "air"), SWF("swf"), SWC("swc"), SWZ("swz"), RB_SWC("rb.swc"), ZIP("zip"), POM("pom");
	
	private FlexExtension(final String extensionString) {
		this.extensionString = extensionString;
	}
	
	private final String extensionString;

	public boolean equals( String extension ) {
		return this.extensionString.equalsIgnoreCase(extension);
	}
	
	public static FlexExtension get( String extension ) {
		for ( FlexExtension flexExtension : values() ) {
			if ( flexExtension.equals(extension)) {
				return flexExtension;
			}
				
		}
		return null;
	}
	
	public String toString() {
		return extensionString;
	}

}
