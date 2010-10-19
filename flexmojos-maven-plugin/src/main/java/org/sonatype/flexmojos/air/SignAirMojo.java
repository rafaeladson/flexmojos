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
package org.sonatype.flexmojos.air;

import static org.sonatype.flexmojos.commons.FlexExtension.AIR;
import static org.sonatype.flexmojos.commons.FlexExtension.SWC;
import static org.sonatype.flexmojos.commons.FlexExtension.SWF;
import static org.sonatype.flexmojos.test.util.PathUtil.getCanonicalPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.transform.SnapshotTransformation;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.MavenMojo;
import org.sonatype.flexmojos.commons.FlexExtension;
import org.sonatype.flexmojos.utilities.FileInterpolationUtil;

import com.adobe.air.AIRPackager;
import com.adobe.air.Listener;
import com.adobe.air.Message;

/**
 * @goal sign-air
 * @phase package
 * @requiresDependencyResolution compile
 * @author Marvin Froeder
 */
public class SignAirMojo
    extends AbstractMojo
    implements MavenMojo
{

    private static String TIMESTAMP_NONE = "none";

    /**
     * @parameter default-value="${project.build.directory}/air"
     */
    private File airOutput;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     * 
     * @parameter expression="${flexmojos.classifier}"
     */
    private String classifier;

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     * 
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;

    /**
     * @parameter default-value="${basedir}/src/main/resources/descriptor.xml"
     */
    private File descriptorTemplate;

    /**
     * Ideally Adobe would have used some parseable token, not a huge pass-phrase on the descriptor output. They did
     * prefer to reinvent wheel, so more work to all of us.<BR>
     * I wonder why people has to be so creative, what is wrong with using something similar to what the rest of the
     * world uses?! =(
     * 
     * @parameter expression="${flexmojos.flexbuilderCompatibility}"
     */
    private boolean flexBuilderCompatibility;

    /**
     * Include specified files in AIR package.
     * 
     * @parameter
     */
    private List<String> includeFiles;

    /**
     * Include specified files or directories in AIR package.
     * 
     * @parameter
     */
    private FileSet[] includeFileSets;

    /**
     * @parameter default-value="${basedir}/src/main/resources/sign.p12"
     */
    private File keystore;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${project.build.resources}"
     */
    private List<Resource> resources;

    /**
     * @parameter
     * @required
     */
    private String storepass;

    /**
     * The type of keystore, determined by the keystore implementation.
     * 
     * @parameter default-value="pkcs12"
     */
    private String storetype;

    /**
     * Strip artifact version during copy of dependencies.
     * 
     * @parameter default-value="false"
     */
    private boolean stripVersion;

    /**
     * The URL for the timestamp server. If 'none', no timestamp will be used.
     * 
     * @parameter
     */
    private String timestampURL;

    // /**
    // * @component role="org.apache.maven.artifact.transform.ArtifactTransformation" hint="snapshot"
    // */
    // private SnapshotTransformation snapshotTransformation;

    private void addSourceWithPath( AIRPackager airPackager, String directory, String includePath )
        throws MojoFailureException
    {
        if ( includePath == null )
        {
            throw new MojoFailureException( "Cannot include a null file" );
        }

        // get file from output directory to allow filtered resources
        File includeFile = new File( directory, includePath );
        if ( !includeFile.isFile() )
        {
            throw new MojoFailureException( "Include files only accept files as parameters: " + includePath );
        }

        // don't include the app descriptor or the cert
        if ( getCanonicalPath( includeFile ).equals( getCanonicalPath( this.descriptorTemplate ) )
            || getCanonicalPath( includeFile ).equals( getCanonicalPath( this.keystore ) ) )
        {
            return;
        }

        getLog().debug( "  adding source " + includeFile + " with path " + includePath );
        airPackager.addSourceWithPath( includeFile, includePath );
    }

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        AIRPackager airPackager = new AIRPackager();
        try
        {
            String c = this.classifier == null ? "" : "-" + this.classifier;
            File output =
                new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + c + "." + AIR );
            airPackager.setOutput( output );
            airPackager.setDescriptor( getAirDescriptor() );

            KeyStore keyStore = KeyStore.getInstance( storetype );
            keyStore.load( new FileInputStream( keystore.getAbsolutePath() ), storepass.toCharArray() );
            String alias = keyStore.aliases().nextElement();
            airPackager.setPrivateKey( (PrivateKey) keyStore.getKey( alias, storepass.toCharArray() ) );
            airPackager.setSignerCertificate( keyStore.getCertificate( alias ) );
            airPackager.setCertificateChain( keyStore.getCertificateChain( alias ) );
            if ( this.timestampURL != null )
            {
                airPackager.setTimestampURL( TIMESTAMP_NONE.equals( this.timestampURL ) ? null : this.timestampURL );
            }

            String packaging = project.getPackaging();
            if ( AIR.equals( packaging ) )
            {
                Set<Artifact> deps = project.getDependencyArtifacts();
                for ( Artifact artifact : deps )
                {
                    if ( SWF.equals( artifact.getType() ) )
                    {
                        File source = artifact.getFile();
                        String path = source.getName();
                        if ( stripVersion && path.contains( artifact.getVersion() ) )
                        {
                            path = path.replace( "-" + artifact.getVersion(), "" );
                        }
                        getLog().debug( "  adding source " + source + " with path " + path );
                        airPackager.addSourceWithPath( source, path );
                    }
                }
            }
            else if ( SWF.equals( packaging ) )
            {
                File source = project.getArtifact().getFile();
                String path = source.getName();
                getLog().debug( "  adding source " + source + " with path " + path );
                airPackager.addSourceWithPath( source, path );
            }
            else
            {
                throw new MojoFailureException( "Unexpected project packaging " + packaging );
            }

            if ( includeFiles == null && includeFileSets == null )
            {
                includeFileSets = resources.toArray( new FileSet[0] );
            }

            if ( includeFiles != null )
            {
                for ( final String includePath : includeFiles )
                {
                    String directory = project.getBuild().getOutputDirectory();
                    addSourceWithPath( airPackager, directory, includePath );
                }
            }

            if ( includeFileSets != null )
            {
                for ( FileSet set : includeFileSets )
                {
                    DirectoryScanner scanner = new DirectoryScanner();
                    scanner.setBasedir( set.getDirectory() );
                    scanner.setIncludes( (String[]) set.getIncludes().toArray( new String[0] ) );
                    scanner.setExcludes( (String[]) set.getExcludes().toArray( new String[0] ) );
                    scanner.addDefaultExcludes();
                    scanner.scan();

                    String[] files = scanner.getIncludedFiles();
                    for ( String path : files )
                    {
                        addSourceWithPath( airPackager, set.getDirectory(), path );
                    }
                }
            }

            if ( classifier != null )
            {
                projectHelper.attachArtifact( project, project.getArtifact().getType(), classifier, output );
            }
            else if ( SWF.equals( packaging ) )
            {
                projectHelper.attachArtifact( project, FlexExtension.AIR.toString(), output );
            }
            else
            {
                project.getArtifact().setFile( output );
            }

            final List<Message> messages = new ArrayList<Message>();

            airPackager.setListener( new Listener()
            {
                public void message( final Message message )
                {
                    messages.add( message );
                }

                public void progress( final int soFar, final int total )
                {
                    getLog().info( "  completed " + soFar + " of " + total );
                }
            } );

            airPackager.createAIR();

            if ( messages.size() > 0 )
            {
                for ( final Message message : messages )
                {
                    getLog().error( "  " + message.errorDescription );
                }

                throw new MojoExecutionException( "Error creating AIR application" );
            }
            else
            {
                getLog().info( "  AIR package created: " + output.getAbsolutePath() );
            }
        }
        catch ( MojoExecutionException e )
        {
            // do not handle
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Error invoking AIR api", e );
        }
        finally
        {
            airPackager.close();
        }
    }

    @SuppressWarnings( "unchecked" )
    private File getAirDescriptor()
        throws MojoExecutionException
    {
        File output = null;
        if ( project.getPackaging().equals( AIR ) )
        {
            Set<Artifact> deps = project.getDependencyArtifacts();
            for ( Artifact artifact : deps )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    output = artifact.getFile();
                    break;
                }
            }
        }
        else
        {
            output = project.getArtifact().getFile();
        }

        String version;
        if ( project.getArtifact().isSnapshot() )
        {
            String timestamp = SnapshotTransformation.getUtcDateFormatter().format( new Date() );
            version = project.getVersion().replace( "SNAPSHOT", timestamp );
        }
        else
        {
            version = project.getVersion();
        }

        File dest = new File( airOutput, project.getBuild().getFinalName() + "-descriptor.xml" );
        try
        {
            Map<String, String> props = new HashMap<String, String>();
            props.put( "output", output.getName() );
            props.put( "version", version );

            FileInterpolationUtil.copyFile( descriptorTemplate, dest, props );

            if ( flexBuilderCompatibility )
            {
                // Workaround Flexbuilder/Flashbuilder weridness
                String str = FileUtils.readFileToString( dest );
                str =
                    str.replace( "[This value will be overwritten by Flex Builder in the output app.xml]",
                                 output.getName() );
                FileUtils.writeStringToFile( dest, str );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy air template", e );
        }

        return dest;
    }

    public MavenSession getSession()
    {
        return context;
    }

}
