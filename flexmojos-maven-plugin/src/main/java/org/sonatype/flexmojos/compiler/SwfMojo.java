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
package org.sonatype.flexmojos.compiler;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static java.util.Arrays.asList;
import static org.sonatype.flexmojos.commons.FlexExtension.SWF;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jvnet.animal_sniffer.IgnoreJRERequirement;
import org.sonatype.flexmojos.commons.FlexExtension;
import org.sonatype.flexmojos.compatibilitykit.FlexCompatibility;
import org.sonatype.flexmojos.test.util.PathUtil;
import org.sonatype.flexmojos.truster.FlashPlayerTruster;
import org.sonatype.flexmojos.truster.TrustException;
import org.sonatype.flexmojos.utilities.SourceFileResolver;

import flex2.tools.oem.Application;
import flex2.tools.oem.Configuration;
import flex2.tools.oem.internal.OEMConfiguration;

/**
 * <p>
 * Goal which compiles the Flex sources into an application for either Flex or AIR depending on the package type.
 * </p>
 * <p>
 * The Flex Compiler plugin compiles all ActionScript sources. It can compile the source into 'swf' files. The plugin
 * supports 'swf' packaging.
 * </p>
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 1.0
 * @goal compile-swf
 * @requiresDependencyResolution compile
 * @phase compile
 */
public class SwfMojo
    extends AbstractCompilerMojo<Application>
{

    /**
     * Turn on generation of debuggable SWFs. False by default for mxmlc, but true by default for compc.
     * 
     * @parameter default-value="false"
     */
    private boolean debug;

    /**
     * Default locale for libraries. This is useful to non localized applications, just to define swc.rb locale
     * 
     * @parameter default-value="en_US"
     */
    private String defaultLocale;

    /**
     * When true, tells flexmojos to use link reports/load externs on modules compilation
     * 
     * @parameter default-value="true" expression="${loadExternsOnModules}"
     */
    private boolean loadExternsOnModules;

    /**
     * The list of modules files to be compiled. The path must be relative with source folder.<BR>
     * Usage:
     * 
     * <pre>
     * &lt;moduleFiles&gt;
     *   &lt;module&gt;com/acme/AModule.mxml&lt;/module&gt;
     * &lt;/moduleFiles&gt;
     * </pre>
     * 
     * @parameter
     */
    private String[] moduleFiles;

    private List<File> modules;

    /**
     * The file to be compiled
     */
    protected File source;

    /**
     * The file to be compiled. The path must be relative with source folder
     * 
     * @parameter
     */
    protected String sourceFile;

    /**
     * When true will strip artifact and version information from the built MXML module artifact.
     * 
     * @parameter default-value="false"
     */
    private boolean stripModuleArtifactInfo;

    /**
     * @component
     */
    private FlashPlayerTruster truster;

    /**
     * When true, flexmojos will register register every compiled SWF files as trusted. These SWF files are assigned to
     * the local-trusted sandbox. They can interact with any other SWF files, and they can load data from anywhere,
     * remote or local. On false nothing is done, so if the file is already trusted it will still as it is.
     * 
     * @parameter default-value="true" expression="${updateSecuritySandbox}"
     */
    private boolean updateSecuritySandbox;

    protected void compileModules()
        throws MojoFailureException, MojoExecutionException
    {
        if ( loadExternsOnModules )
        {
            configuration.addExterns( new File[] { linkReportFile } );
        }

        // restore source paths
        configuration.setSourcePath( sourcePaths );

        for ( File module : modules )
        {
            getLog().info( "Compiling module " + module );
            String moduleName = module.getName();
            moduleName = moduleName.substring( 0, moduleName.lastIndexOf( '.' ) );

            Application moduleBuilder;
            try
            {
                moduleBuilder = new Application( module );
            }
            catch ( FileNotFoundException e )
            {
                throw new MojoFailureException( "Unable to find " + module, e );
            }

            setMavenPathResolver( moduleBuilder );
            moduleBuilder.setConfiguration( configuration );
            moduleBuilder.setLogger( new MavenLogger( getLog() ) );
            String moduleArtifactPrefix = stripModuleArtifactInfo ? "" : build.getFinalName() + "-";
            File outputModule =
                new File( build.getDirectory(), moduleArtifactPrefix + moduleName + "." + project.getPackaging() );
            updateSecuritySandbox( outputModule );

            moduleBuilder.setOutput( outputModule );

            callCompiler( moduleBuilder, false );

            projectHelper.attachArtifact( project, FlexExtension.SWF.toString(), moduleName, outputModule );

        }
    }

    protected void configureResourceBundles( OEMConfiguration oemConfig )
    {
        if ( includeResourceBundles != null )
        {
            oemConfig.setIncludeResourceBundles( includeResourceBundles );
        }
    }

    @Override
    protected void fixConfigReport( FlexConfigBuilder configBuilder )
    {
        super.fixConfigReport( configBuilder );

        configBuilder.addList( new String[] { source.getAbsolutePath() }, "file-specs", "path-element" );
    }

    @FlexCompatibility( maxVersion = "2" )
    @IgnoreJRERequirement
    private void forkMxmlc( Set<String> args )
        throws MojoExecutionException
    {
        throw new MojoExecutionException( "Not implemented yet" );
    }

    @Override
    protected boolean getIsApplication()
    {
        return true;
    }

    @Override
    protected boolean getIsDebug()
    {
        return this.debug;
    }

    protected Configuration getResourceBundleConfiguration( String[] bundlesNames, String locale, File localePath )
        throws MojoExecutionException
    {
        if ( configuration instanceof OEMConfiguration )
        {
            OEMConfiguration oemConfiguration = (OEMConfiguration) configuration;
            oemConfiguration.setIncludeResourceBundles( bundlesNames );
        }

        setLocales( locale );
        if ( localePath != null )
        {
            configuration.setSourcePath( new File[] { localePath } );
        }

        configuration.includeLibraries( null );
        configuration.addExternalLibraryPath( getDependenciesPath( INTERNAL ) );

        configuration.addLibraryPath( getResourcesBundles( locale ) );

        configuration.setLibraryPath( getResourcesBundles( locale ) );
        configuration.addLibraryPath( getDependenciesPath( MERGED ) );

        configuration.setServiceConfiguration( null );

        return configuration;
    }

    private File getSource()
    {
        if ( source == null )
        {
            if ( sourceFile == null )
            {
                getLog().warn( "Source file was not defined, flexmojos will guess one." );
            }
            source =
                SourceFileResolver.resolveSourceFile( project.getCompileSourceRoots(), sourceFile,
                                                      project.getGroupId(), project.getArtifactId() );
        }

        return source;
    }

    @Override
    protected String getToolLocale()
    {
        if ( "css".equalsIgnoreCase( FilenameUtils.getExtension( getSource().getName() ) ) )
        {
            return null;
        }
        return this.defaultLocale;
    }

    @FlexCompatibility( minVersion = "3", maxVersion = "3.1" )
    @IgnoreJRERequirement
    private void runMxmlc( Set<String> args )
    {
        // Just a work around
        // TODO https://bugs.adobe.com/jira/browse/SDK-15139
        flex2.tools.Compiler.mxmlc( args.toArray( new String[args.size()] ) );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void setUp()
        throws MojoExecutionException, MojoFailureException
    {
        File sourceDirectory = new File( build.getSourceDirectory() );
        if ( !sourceDirectory.exists() )
        {
            throw new MojoExecutionException( "Unable to find sourceDirectory: " + sourceDirectory );
        }

        getSource();

        if ( source == null )
        {
            throw new MojoExecutionException( "Source file not expecified and no default found!" );
        }
        if ( !source.exists() )
        {
            throw new MojoFailureException( "Unable to find " + sourceFile );
        }

        // need to initialize builder before go super
        try
        {
            builder = new Application( source );
        }
        catch ( FileNotFoundException e )
        {
            throw new MojoFailureException( "Unable to find " + source );
        }

        if ( moduleFiles != null )
        {
            modules = new ArrayList<File>();
            for ( String modulePath : moduleFiles )
            {
                File module = new File( sourceDirectory, modulePath );
                if ( !module.exists() )
                {
                    throw new MojoExecutionException( "Module " + module + " not found." );
                }
                modules.add( module );
            }

            if ( loadExternsOnModules )
            {
                super.linkReport = true;
            }
        }

        super.setUp();

        builder.setOutput( getOutput() );
    }

    @Override
    protected void tearDown()
        throws MojoExecutionException, MojoFailureException
    {
        super.tearDown();

        if ( modules != null )
        {
            compileModules();
        }

        updateSecuritySandbox( getOutput() );
    }

    private String toString( List<File> libs )
    {
        StringBuilder sb = new StringBuilder();
        for ( File lib : libs )
        {
            if ( sb.length() != 0 )
            {
                sb.append( ',' );
            }

            sb.append( lib.getAbsolutePath() );
        }
        return sb.toString();
    }

    protected void updateSecuritySandbox( File output )
        throws MojoExecutionException
    {
        if ( updateSecuritySandbox )
        {
            try
            {
                truster.updateSecuritySandbox( getOutput() );
            }
            catch ( TrustException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }

    }

    @Override
    protected void writeResourceBundle( String[] bundlesNames, String locale, File localePath )
        throws MojoExecutionException
    {
        writeResourceBundleFlex30( bundlesNames, locale, localePath );
        writeResourceBundleFlex32( bundlesNames, locale, localePath );
    }

    @FlexCompatibility( minVersion = "3", maxVersion = "3.1" )
    @IgnoreJRERequirement
    protected void writeResourceBundleFlex30( String[] bundles, String locale, File localePath )
        throws MojoExecutionException
    {
        // Dont break this method in parts, is a work around

        File output = getRuntimeLocaleOutputFile( locale, FlexExtension.SWF.toString() );

        /*
         * mxmlc -locale=en_US -source-path=locale/{locale} -include-resource-bundles
         * =FlightReservation2,SharedResources,collections ,containers,controls,core,effects,formatters,skins,styles
         * -output=src/Resources_en_US.swf
         */

        String bundlesString = Arrays.toString( bundles ) //
        .replace( "[", "" ) // remove start [
        .replace( "]", "" ) // remove end ]
        .replace( ", ", "," ); // remove spaces

        ArrayList<File> external = new ArrayList<File>();
        ArrayList<File> internal = new ArrayList<File>();
        ArrayList<File> merged = new ArrayList<File>();

        external.addAll( asList( getGlobalDependency() ) );
        external.addAll( asList( getDependenciesPath( "external" ) ) );
        external.addAll( asList( getDependenciesPath( "rsl" ) ) );

        internal.addAll( asList( getDependenciesPath( "internal" ) ) );

        merged.addAll( asList( getDependenciesPath( "compile" ) ) );
        merged.addAll( asList( getDependenciesPath( "merged" ) ) );
        merged.addAll( asList( getResourcesBundles( locale ) ) );

        Set<String> args = new HashSet<String>();
        // args.addAll(Arrays.asList(configs));
        args.add( "-locale=" + locale );
        if ( localePath != null )
        {
            args.add( "-source-path=" + localePath.getAbsolutePath() );
        }
        args.add( "-include-resource-bundles=" + bundlesString );
        args.add( "-output=" + output.getAbsolutePath() );
        args.add( "-compiler.fonts.local-fonts-snapshot=" + getFontsSnapshot().getAbsolutePath() );
        if ( configFile != null )
        {
            args.add( "-load-config=" + PathUtil.getCanonicalPath( configFile ) );
        }
        else if ( configFiles != null )
        {
            String separator = "=";
            for ( File cfg : configFiles )
            {
                args.add( " -load-config" + separator + PathUtil.getCanonicalPath( cfg ) );
                separator = "+=";
            }
        }
        else
        {
            args.add( "-load-config=" );
        }

        args.add( "-external-library-path=" + toString( external ) );
        args.add( "-include-libraries=" + toString( internal ) );
        args.add( "-library-path=" + toString( merged ) );

        getLog().debug( "writeResourceBundle calling mxmlc with args: " + args.toString() );
        forkMxmlc( args );
        runMxmlc( args );

        projectHelper.attachArtifact( project, FlexExtension.SWF.toString(), locale, output );
    }

    @FlexCompatibility( minVersion = "3.2" )
    @IgnoreJRERequirement
    protected void writeResourceBundleFlex32( String[] bundlesNames, String locale, File localePath )
        throws MojoExecutionException
    {
        Application rbBuilder = new Application();
        File output = getRuntimeLocaleOutputFile( locale, FlexExtension.SWF.toString());

        rbBuilder.setLogger( new MavenLogger( getLog() ) );
        rbBuilder.setOutput( output );
        rbBuilder.setConfiguration( configuration );

        if ( configuration instanceof OEMConfiguration )
        {
            OEMConfiguration oemConfiguration = (OEMConfiguration) configuration;
            oemConfiguration.setIncludeResourceBundles( bundlesNames );
        }
        setLocales( locale );
        if ( localePath != null )
        {
            configuration.setSourcePath( new File[] { localePath } );
        }
        configuration.includeLibraries( null );
        configuration.addExternalLibraryPath( getDependenciesPath( INTERNAL ) );

        configuration.addLibraryPath( getResourcesBundles( locale ) );

        callCompiler( rbBuilder, true );

        if ( configurationReport )
        {
            try
            {
                FlexConfigBuilder configBuilder = new FlexConfigBuilder( rbBuilder );
                configBuilder.addOutput( output );
                configBuilder.write( new File( output.getPath().replace( "." + SWF, "-config-report.xml" ) ) );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "An error has ocurried while recording config-report", e );
            }
        }

        projectHelper.attachArtifact( project, FlexExtension.SWF.toString(), locale, output );
    }

}
