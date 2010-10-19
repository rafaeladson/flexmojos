package org.sonatype.flexmojos.commons;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public enum ProjectType
{
    FLEX( new FlexExtension[] { FlexExtension.SWF }, false, false), 
    FLEX_LIBRARY( new FlexExtension[] {FlexExtension.SWC, FlexExtension.RB_SWC, FlexExtension.SWZ}, false, false ), 
    ACTIONSCRIPT( new FlexExtension[] { FlexExtension.SWF}, false, true ), 
    AIR( new FlexExtension[] {FlexExtension.AIR, FlexExtension.SWF}, true, false), 
    AIR_LIBRARY( new FlexExtension[] {FlexExtension.SWC}, true, false);
    
    
    private ProjectType(FlexExtension[] extensions, boolean isAir, boolean actionScriptOnly ) {
    	
    	this.allowedExtensions = new HashSet<FlexExtension>(Arrays.asList(extensions));
    	this.isAir = isAir;
		this.actionScriptOnly = actionScriptOnly;
	}
    
    private boolean correspondsTo( FlexExtension packaging, boolean isAir, boolean actionScriptOnly ) {
    	return this.allowedExtensions.contains(packaging) &&
    		this.isAir == isAir &&
    		this.actionScriptOnly == actionScriptOnly;
    		
    }
    
    private final Set<FlexExtension> allowedExtensions;
    private final boolean isAir;
    private final boolean actionScriptOnly;

    public static ProjectType getProjectType( FlexExtension packaging, boolean useApoloConfig, boolean actionScript )
    {
    	for ( ProjectType type : values() ) 
    	{
    		if ( type.correspondsTo(packaging, useApoloConfig, actionScript)) 
    		{
    			return type;
    		}
    	}
    	
    	/**
    	 * maintaining the default behaviour.
    	 */
    	return FLEX_LIBRARY;
    }
}
