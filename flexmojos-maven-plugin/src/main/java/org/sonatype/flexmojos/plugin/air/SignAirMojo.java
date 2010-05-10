package org.sonatype.flexmojos.plugin.air;

import static org.sonatype.flexmojos.util.PathUtil.*;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.AIR;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWC;
import static org.sonatype.flexmojos.plugin.common.FlexExtension.SWF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
import org.sonatype.flexmojos.plugin.utilities.FileInterpolationUtil;

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
{

    /**
     * The type of keystore, determined by the keystore implementation.
     * 
     * @parameter default-value="pkcs12"
     */
    private String storetype;

    /**
     * @parameter default-value="${basedir}/src/main/resources/sign.p12"
     */
    private File keystore;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.build.resources}"
     */
    private List<Resource> resources;

    /**
     * @parameter default-value="${basedir}/src/main/resources/descriptor.xml"
     */
    private File descriptorTemplate;

    /**
     * @parameter
     * @required
     */
    private String storepass;

    /**
     * @parameter default-value="${project.build.directory}/air"
     */
    private File airOutput;

    /**
     * Include specified files in AIR package.
     * 
     * @parameter
     */
    private List<String> includeFiles;

    /**
     * Classifier to add to the artifact generated. If given, the artifact will be an attachment instead.
     * 
     * @parameter expression="${flexmojos.classifier}"
     */
    private String classifier;

    /**
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

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

            String packaging = project.getPackaging();
            if ( AIR.equals( packaging ) )
            {
                appendArtifacts( airPackager, project.getDependencyArtifacts() );
                appendArtifacts( airPackager, project.getAttachedArtifacts() );
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

            if ( includeFiles == null )
            {
                includeFiles = listAllResources();
            }

            for ( final String includePath : includeFiles )
            {
                if ( includePath == null )
                {
                    throw new MojoFailureException( "Cannot include a null file" );
                }

                // get file from output directory to allow filtered resources
                File includeFile = new File( project.getBuild().getOutputDirectory(), includePath );
                if ( !includeFile.exists() )
                {
                    throw new MojoFailureException( "Unable to find resource: " + includePath );
                }

                // don't include the app descriptor or the cert
                if ( getCanonicalPath( includeFile ).equals( getCanonicalPath( this.descriptorTemplate ) )
                    || getCanonicalPath( includeFile ).equals( getCanonicalPath( this.keystore ) ) )
                {
                    continue;
                }

                getLog().debug( "  adding source " + includeFile + " with path " + includePath );
                airPackager.addSourceWithPath( includeFile, includePath );
            }

            if ( classifier != null )
            {
                projectHelper.attachArtifact( project, project.getArtifact().getType(), classifier, output );
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

    private void appendArtifacts( AIRPackager airPackager, Collection<Artifact> deps )
    {
        for ( Artifact artifact : deps )
        {
            if ( SWF.equals( artifact.getType() ) )
            {
                File source = artifact.getFile();
                String path = source.getName();
                getLog().debug( "  adding source " + source + " with path " + path );
                airPackager.addSourceWithPath( source, path );
            }
        }
    }

    private File getAirDescriptor()
        throws MojoExecutionException
    {
        File output = getOutput();

        File dest = new File( airOutput, project.getBuild().getFinalName() + "-descriptor.xml" );
        try
        {
            FileInterpolationUtil.copyFile( descriptorTemplate, dest, Collections.singletonMap( "output",
                                                                                                output.getName() ) );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to copy air template", e );
        }
        return dest;
    }

    private File getOutput()
    {
        File output = null;
        if ( project.getPackaging().equals( AIR ) )
        {
            List<Artifact> attach = project.getAttachedArtifacts();
            for ( Artifact artifact : attach )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    return artifact.getFile();
                }
            }
            Set<Artifact> deps = project.getDependencyArtifacts();
            for ( Artifact artifact : deps )
            {
                if ( SWF.equals( artifact.getType() ) || SWC.equals( artifact.getType() ) )
                {
                    return artifact.getFile();
                }
            }
        }
        else
        {
            output = project.getArtifact().getFile();
        }
        return output;
    }

    /**
     * @see org.sonatype.flexmojos.compiler.LibraryMojo.listAllResources()
     */
    private List<String> listAllResources()
    {
        List<String> inclusions = new ArrayList<String>();
        for ( Resource resource : resources )
        {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir( resource.getDirectory() );
            scanner.setIncludes( (String[]) resource.getIncludes().toArray( new String[0] ) );
            scanner.setExcludes( (String[]) resource.getExcludes().toArray( new String[0] ) );
            scanner.addDefaultExcludes();
            scanner.scan();

            inclusions.addAll( Arrays.asList( scanner.getIncludedFiles() ) );
        }

        return inclusions;
    }

}