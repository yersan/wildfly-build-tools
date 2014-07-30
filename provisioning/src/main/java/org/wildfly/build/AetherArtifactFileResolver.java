package org.wildfly.build;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.util.List;

/**
 * @author Eduardo Martins
 */
public class AetherArtifactFileResolver implements ArtifactFileResolver {

    private final RepositorySystem repoSystem;
    private final RepositorySystemSession repoSession;
    private final List<RemoteRepository> remoteRepos;

    public AetherArtifactFileResolver(RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepos = remoteRepos;
    }

    @Override
    public File getArtifactFile(String artifactCoords) {
        return getArtifactFile(new DefaultArtifact(artifactCoords));
    }

    private File getArtifactFile(Artifact artifact) {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);
        final ArtifactResult result;
        try {
            result = repoSystem.resolveArtifact(repoSession, request);
        } catch ( ArtifactResolutionException e ) {
            throw new RuntimeException("failed to resolve artifact "+artifact, e);
        }
        return result.getArtifact().getFile();
    }

    @Override
    public File getArtifactFile(org.wildfly.build.pack.model.Artifact artifact) {
        final org.wildfly.build.pack.model.Artifact.GACE GACE = artifact.getGACE();
        final String groupId = GACE.getGroupId();
        final String artifactId = GACE.getArtifactId();
        final String extension = GACE.getExtension() != null ? GACE.getExtension() : "jar";
        final String classifier = GACE.getClassifier() != null ? GACE.getClassifier() : "";
        return getArtifactFile(new DefaultArtifact(groupId, artifactId, classifier, extension, artifact.getVersion()));
    }
}
