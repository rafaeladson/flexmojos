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
package org.sonatype.flexmojos.flexbuilder;

import static org.sonatype.flexmojos.commons.FlexExtension.SWC;
import static org.sonatype.flexmojos.commons.FlexExtension.SWF;

import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.eclipse.EclipseConfigFile;

/**
 * Generates AXDT configuration files for SWC and SWF projects.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.4
 * @extendsPlugin eclipse
 * @extendsGoal eclipse
 * @goal axdt
 * @requiresDependencyResolution compile
 */
public class AxdtMojo
    extends AbstractIdeMojo

{
    protected static final String AXDT_NATURE = "org.axdt.as3.imp.nature";

    protected static final String AXDT_BUILD_COMMAND = "org.axdt.as3.imp.builder";

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultNatures( String packaging )
    {
        super.fillDefaultNatures( packaging );

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) )
        {
            getProjectnatures().add( AXDT_NATURE );
        }
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected void fillDefaultBuilders( String packaging )
    {
        super.fillDefaultBuilders( packaging );

        if ( SWF.equals( packaging ) || SWC.equals( packaging ) )
        {
            getBuildcommands().add( AXDT_BUILD_COMMAND );
        }
    }

    @Override
    protected List<EclipseConfigFile> getExtraConfigs()
    {
        EclipseConfigFile axdtConfig = new EclipseConfigFile();
        axdtConfig.setName( ".settings/org.axdt.as3.prefs" );
        axdtConfig.setContent( getAxdtContent() );

        List<EclipseConfigFile> extraConfigs = super.getExtraConfigs();
        extraConfigs.add( axdtConfig );
        return extraConfigs;
    }

    private String getAxdtContent()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( '#' ).append( new Date().toString() ).append( '\n' );
        sb.append( "CONFIG_PATH=config" ).append( '\n' );
        sb.append( "DEPLOY_PATH=bin" ).append( '\n' );
        sb.append( "LIBRARY_PATHS=libs" ).append( '\n' );
        sb.append( "SOURCE_PATHS=" + plain( getRelativeSources() ) ).append( '\n' );
        return sb.toString();
    }

}
