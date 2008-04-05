/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.rvin.mojo.flexmojo.compiler;

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

import static info.rvin.flexmojos.utilities.MavenUtils.resolveArtifact;
import flex2.tools.oem.Library;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal compile-swc
 * @requiresDependencyResolution
 * @phase compile
 */
public class LibraryMojo extends AbstractFlexCompilerMojo<Library> {

	/**
	 * Enable or disable the computation of a digest for the created swf
	 * library. This is equivalent to using the
	 * <code>compiler.computDigest</code> in the compc compiler.
	 * 
	 * @parameter default-value="true"
	 */
	private boolean computeDigest;

	/**
	 * This is the equilvalent of the <code>include-classes</code> option of
	 * the compc compiler.
	 * 
	 * @parameter
	 */
	private String[] includeClasses;

	/**
	 * This is equilvalent to the <code>include-file</code> option of the
	 * compc compiler.
	 * 
	 * @parameter
	 */
	private File[] includeFiles;

	/**
	 * This is equilvalent to the <code>include-namespaces</code> option of
	 * the compc compiler.
	 * 
	 * @parameter
	 */
	private String[] includeNamespaces;

	/**
	 * This is equilvalent to the <code>include-resource-bundles</code> option
	 * of the compc compiler.
	 * 
	 * @parameter
	 */
	private String[] includeResourceBundles;

	/**
	 * @parameter
	 */
	private MavenArtifact[] includeResourceBundlesArtifact;

	/**
	 * This is the equilvalent of the <code>include-sources</code> option of
	 * the compc compiler.
	 * 
	 * @parameter
	 */
	private File[] includeSources;

	@Override
	public void setUp() throws MojoExecutionException, MojoFailureException {
		// need to initialize builder before go super
		builder = new Library();
		super.setUp();

		builder.setOutput(outputFile);
		builder.setDirectory(new File(build.getDirectory()));

		if (checkNullOrEmpty(includeClasses) && checkNullOrEmpty(includeFiles)
				&& checkNullOrEmpty(includeNamespaces)
				&& checkNullOrEmpty(includeResourceBundles)
				&& checkNullOrEmpty(includeResourceBundlesArtifact)
				&& checkNullOrEmpty(includeSources)) {
			throw new MojoExecutionException("Nothing to be included.");
		}

		if (!checkNullOrEmpty(includeClasses)) {
			for (String asClass : includeClasses) {
				builder.addComponent(asClass);
			}
		}

		if (!checkNullOrEmpty(includeFiles)) {
			for (File file : includeFiles) {
				if (file == null) {
					throw new MojoFailureException("Cannot include a null file");
				}
				if (!file.exists()) {
					throw new MojoFailureException("File " + file.getName()
							+ " not found");
				}
				builder.addArchiveFile(file.getName(), file);
			}
		}

		if (!checkNullOrEmpty(includeNamespaces)) {
			for (String uri : includeNamespaces) {
				try {
					builder.addComponent(new URI(uri));
				} catch (URISyntaxException e) {
					throw new MojoExecutionException("Invalid URI " + uri, e);
				}
			}
		}

		if (!checkNullOrEmpty(includeResourceBundles)) {
			for (String rb : includeResourceBundles) {
				builder.addResourceBundle(rb);
			}
		}

		if (!checkNullOrEmpty(includeResourceBundlesArtifact)) {
			for (MavenArtifact mvnArtifact : includeResourceBundlesArtifact) {
				Artifact artifact = artifactFactory
						.createArtifactWithClassifier(mvnArtifact.getGroupId(),
								mvnArtifact.getArtifactId(), mvnArtifact
										.getVersion(), "properties",
								"resource-bundle");
				resolveArtifact(artifact, resolver, localRepository,
						remoteRepositories);
				String bundleFile;
				try {
					bundleFile = FileUtils.readFileToString(artifact.getFile());
				} catch (IOException e) {
					throw new MojoExecutionException(
							"Ocorreu um erro ao ler o artefato " + artifact, e);
				}
				String[] bundles = bundleFile.split(" ");
				for (String bundle : bundles) {
					builder.addResourceBundle(bundle);
				}
			}
		}

		if (!checkNullOrEmpty(includeSources)) {
			for (File file : includeSources) {
				if (file == null) {
					throw new MojoFailureException("Cannot include a null file");
				}
				if (!file.exists()) {
					throw new MojoFailureException("File " + file.getName()
							+ " not found");
				}
				builder.addComponent(file);
			}
		}

		configuration.enableDigestComputation(computeDigest);
	}

	private boolean checkNullOrEmpty(Object[] includeClasses) {
		if (includeClasses == null) {
			return true;
		}

		if (includeClasses.length == 0) {
			return false;
		}

		return false;
	}

	@Override
	protected void writeResourceBundle(String[] bundles, String locale,
			File localePath) throws MojoExecutionException {
		getLog().info("Generating resource-bundle for " + locale);

		Library localized = new Library();
		localized.setConfiguration(configuration);

		localized.setLogger(new CompileLogger(getLog()));

		configuration.addLibraryPath(new File[] { outputFile });
		configuration.setLocale(new String[] { locale });
		configuration.setSourcePath(new File[] { localePath });
		for (String bundle : bundles) {
			localized.addResourceBundle(bundle);
		}

		File output = new File(build.getDirectory(), project.getArtifactId()
				+ "-" + project.getVersion() + "-" + locale + ".swc");

		localized.setOutput(output);
		long bytes;
		try {
			bytes = localized.build(false);
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Unable to compile resource bundle", e);
		}
		if (bytes == 0) {
			throw new MojoExecutionException(
					"Unable to compile resource bundle");
		}

		projectHelper
				.attachArtifact(project, "resource-bundle", locale, output);
	}

}
