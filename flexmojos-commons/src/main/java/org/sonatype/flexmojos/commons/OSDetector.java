package org.sonatype.flexmojos.commons;

import java.awt.GraphicsEnvironment;

public class OSDetector
{

    private static final String WINDOWS_CMD = "FlashPlayer.exe";

    private static final String MAC_CMD = "Flash Player";

    private static final String UNIX_CMD = "flashplayer";

    public enum OS
    {
        windows, linux, solaris, mac, unix, other;
    }

    public OS getOSType()
    {
        String osName = System.getProperty( "os.name" ).toLowerCase();
        for ( OS os : OS.values() )
        {
            if ( osName.contains( os.toString() ) )
            {
                return os;
            }
        }
        return OS.other;
    }

    public String getPlatformDefaultCommand()
    {
        switch ( getOSType() )
        {
            case windows:
                return WINDOWS_CMD;
            case mac:
                return MAC_CMD;
            default:
                return UNIX_CMD;
        }
    }

    public boolean isLinux()
    {
        switch ( getOSType() )
        {
            case windows:
            case mac:
                return false;
            default:
                return true;
        }
    }
    
    public boolean isHeadless() 
    {
    	return GraphicsEnvironment.isHeadless();
    }

}