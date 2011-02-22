package org.sonatype.flexmojos.plugin.utilities;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.sonatype.flexmojos.compiler.IRuntimeSharedLibraryPath;

@SuppressWarnings("deprecation")
public class RslSorter {

	private final DefaultMavenProjectBuilder mavenProjectBuilder;
	private final List remoteRepositories;
	private final ArtifactRepository localRepository;
	private final ArtifactFactory artifactFactory;
	protected ArtifactResolver resolver;
	protected ArtifactMetadataSource artifactMetadataSource;
	private Log log;

	public RslSorter(final DefaultMavenProjectBuilder mavenProjectBuilder, final List remoteRepositories,
			final ArtifactRepository localRepository, final ArtifactFactory artifactFactory,
			final ArtifactResolver resolver, final ArtifactMetadataSource artifactMetadataSource, Log log) {
		super();
		this.mavenProjectBuilder = mavenProjectBuilder;
		this.remoteRepositories = remoteRepositories;
		this.localRepository = localRepository;
		this.artifactFactory = artifactFactory;
		this.resolver = resolver;
		this.artifactMetadataSource = artifactMetadataSource;
		this.log=log;
	}

	public Set<Artifact> rslsSort(Set<Artifact> rslArtifacts) throws MojoExecutionException {

		Calendar antes = Calendar.getInstance();
		Map<Artifact, List<Artifact>> dependencies = getDependencies(rslArtifacts);
		Calendar depois = Calendar.getInstance();
		log.debug("Tempo" + (depois.getTimeInMillis() - antes.getTimeInMillis()));
		Set<Artifact> ordered = new LinkedHashSet<Artifact>();

		for (Artifact a : rslArtifacts) {
			if (dependencies.get(a) == null || dependencies.get(a).isEmpty()) {
				ordered.add(a);
			}
		}
		rslArtifacts.removeAll(ordered);

		while (!rslArtifacts.isEmpty()) {
			int original = rslArtifacts.size();
			for (Artifact a : rslArtifacts) {
				List<Artifact> deps = dependencies.get(a);
				if (ordered.containsAll(deps)) {
					ordered.add(a);
				}
			}
			rslArtifacts.removeAll(ordered);
			if (original == rslArtifacts.size()) {
				throw new MojoExecutionException("Unable to resolve " + rslArtifacts);
			}
		}

		return ordered;
	}

	private Map<Artifact, List<Artifact>> getDependencies(final Set<Artifact> rslArtifacts)
			throws MojoExecutionException {
		Map<Artifact, List<Artifact>> dependencies = new HashMap<Artifact, List<Artifact>>();
		ExecutorService executors = Executors.newFixedThreadPool(rslArtifacts.size());
		Collection<DependenciesCallable> tasks = new ArrayList<DependenciesCallable>();
		for (Artifact art : rslArtifacts) {
			tasks.add(new DependenciesCallable(art, rslArtifacts));
		}
		try {
			List<Future<Entry<Artifact, List<Artifact>>>> deps = executors.invokeAll(tasks);
			for (Future<Entry<Artifact, List<Artifact>>> future : deps) {
				Entry<Artifact, List<Artifact>> entry = future.get();
				dependencies.put(entry.getKey(), entry.getValue());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return dependencies;
	}

	private class DependenciesCallable implements Callable<Entry<Artifact, List<Artifact>>> {

		private final Artifact pomArtifact;
		private final Set<Artifact> rslArtifacts;

		public DependenciesCallable(final Artifact pomArtifact, final Set<Artifact> rslArtifacts) {
			super();
			this.pomArtifact = pomArtifact;
			this.rslArtifacts = rslArtifacts;
		}

		private List<Artifact> removeNonRSLDependencies(final Set<Artifact> rslArtifacts,
				final List<Artifact> artifactDependencies) {
			List<Artifact> cleanArtifacts = new ArrayList<Artifact>();
			artifacts: for (Artifact artifact : artifactDependencies) {
				for (Artifact rslArtifact : rslArtifacts) {
					if (artifact.getGroupId().equals(rslArtifact.getGroupId())
							&& artifact.getArtifactId().equals(rslArtifact.getArtifactId())
							&& artifact.getType().equals(rslArtifact.getType())) {
						cleanArtifacts.add(rslArtifact);
						continue artifacts;
					}
				}
			}
			return cleanArtifacts;
		}

		public Entry<Artifact, List<Artifact>> call() throws Exception {
			try {
				log.debug("thread " + Thread.currentThread().getName() + "Iniciada ");
				Calendar antes = Calendar.getInstance();
				MavenProject pomProject = mavenProjectBuilder.buildFromRepository(pomArtifact, remoteRepositories,
						localRepository);
				Set pomArtifacts = pomProject.createArtifacts(artifactFactory, null, null);
				ArtifactResolutionResult arr = resolver.resolveTransitively(pomArtifacts, pomArtifact,
						remoteRepositories, localRepository, artifactMetadataSource);
				List<Artifact> artifactDependencies = new ArrayList<Artifact>(arr.getArtifacts());
				artifactDependencies = removeNonRSLDependencies(rslArtifacts, artifactDependencies);
				Calendar depois = Calendar.getInstance();
				log.debug("Tempo da Thread " + Thread.currentThread().getName() + " "
						+ (depois.getTimeInMillis() - antes.getTimeInMillis()));
				return new AbstractMap.SimpleEntry(pomArtifact, artifactDependencies);
			} catch (Exception e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}
	}

}
