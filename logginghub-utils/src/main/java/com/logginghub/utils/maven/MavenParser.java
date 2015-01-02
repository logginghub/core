package com.logginghub.utils.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.utils.Tree;
import com.logginghub.utils.Tree.Node;
import com.logginghub.utils.logging.Logger;

public class MavenParser implements PomModelProvider {

    private PomArtifactResolver artifactResolver = new DefaultPomArtifactResolver();
    private PomLocationResolver locationResolver = new DefaultPomLocationResolver();
    private Map<File, PomModel> parsedFiles = new HashMap<File, PomModel>();

    private Map<VersionedMavenKey, PomModel> parsedModels = new HashMap<VersionedMavenKey, PomModel>();

    public static class DefaultPomArtifactResolver implements PomArtifactResolver {
        public File resolveArtifact(VersionedMavenKey key, MavenParser parser) {
            // TODO : speed this up
            File jarArtifactForKey = MavenUtils.getJarArtifactForKey(key);
            return jarArtifactForKey;
        }
    }

    public static class DefaultPomLocationResolver implements PomLocationResolver {
        public File resolveLocation(VersionedMavenKey key, MavenParser parser) {
            // TODO : speed this up
            logger.debug("Trying to find pom for key '{}'", key);
            File pomArtifactForKey = MavenUtils.getPomArtifactForKey(key);
            return pomArtifactForKey;
        }
    }

    public interface PomArtifactResolver {
        File resolveArtifact(VersionedMavenKey key, MavenParser parser);
    }

    public interface PomLocationResolver {
        File resolveLocation(VersionedMavenKey key, MavenParser parser);
    }

    public interface PomScopeFilter {
        boolean passes(String scope);
    }

    public static class WorkspaceFirstPomArtifactResolver extends DefaultPomArtifactResolver {

        private File[] workspaceRoots;

        public WorkspaceFirstPomArtifactResolver(File... workspaceRoots) {
            this.workspaceRoots = workspaceRoots;
        }

        public File resolveArtifact(VersionedMavenKey key, MavenParser parser) {
            logger.debug("Resolving artifact '{}'", key);
            File found = null;
            for (File workspaceRoot : workspaceRoots) {
                File potentialProjectFolder = new File(workspaceRoot, key.getArtifact());
                if (potentialProjectFolder.exists()) {
                    File potentialPomFile = new File(potentialProjectFolder, "pom.xml");
                    if (potentialPomFile.exists()) {
                        PomModel model = parser.getModel(potentialPomFile);
                        VersionedMavenKey key2 = model.getKey();
                        if (key2.equals((VersionedMavenKey) key)) {
                            found = new File(potentialProjectFolder, "target/classes");
                            logger.trace("Found workspace match for key {} here {}", key, found.getAbsolutePath());
                            break;
                        }
                    }
                }
            }

            if (found == null) {
                found = super.resolveArtifact(key, parser);
            }

            return found;
        }
    }

    public static class WorkspaceFirstPomLocationResolver extends DefaultPomLocationResolver {

        private File[] workspaceRoots;

        public WorkspaceFirstPomLocationResolver(File... workspaceRoots) {
            this.workspaceRoots = workspaceRoots;
        }

        public File resolveLocation(VersionedMavenKey key, MavenParser parser) {
            File found = null;
            for (File workspaceRoot : workspaceRoots) {
                File potentialProjectFolder = new File(workspaceRoot, key.getArtifact());
                if (potentialProjectFolder.exists()) {
                    File potentialPomFile = new File(potentialProjectFolder, "pom.xml");
                    if (potentialPomFile.exists()) {
                        PomModel model = parser.getModel(potentialPomFile);
                        VersionedMavenKey key2 = model.getKey();
                        if (key2.equals((VersionedMavenKey) key)) {
                            found = potentialPomFile;
                            logger.trace("Found workspace match for key {} here {}", key, found.getAbsolutePath());
                            break;
                        }
                    }
                }
            }

            if (found == null) {
                found = super.resolveLocation(key, parser);
            }

            return found;
        }
    }

    private static final Logger logger = Logger.getLoggerFor(MavenParser.class);

    public PomArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    public PomLocationResolver getLocationResolver() {
        return locationResolver;
    }

    public Tree<VersionedMavenKey> buildTree(File pom) {

        Map<MavenKey, String> versionRegistry = new HashMap<MavenKey, String>();

        Tree<VersionedMavenKey> tree = new Tree<VersionedMavenKey>();

        PomModel model = getModel(pom);
        Node<VersionedMavenKey> topLevel = tree.getRoot().add(model.getKey());

        List<MavenDependency> dependencies = model.getRuntimeDependencies();
        for (MavenDependency mavenDependency : dependencies) {
            if (versionRegistry.containsKey(mavenDependency)) {
                // Already got a different version of this one... ignore it
            }
            else {
                Node<VersionedMavenKey> node = topLevel.add(mavenDependency);
                versionRegistry.put(mavenDependency.extractKey(), mavenDependency.getVersionNumber().toString());
            }
        }

        List<Node<VersionedMavenKey>> children = topLevel.getChildren();
        for (Node<VersionedMavenKey> node : topLevel.getChildren()) {
            buildTreeRecursive(node, versionRegistry);
        }

        return tree;

    }

    private void buildTreeRecursive(Node<VersionedMavenKey> node, Map<MavenKey, String> versionRegistry) {
        VersionedMavenKey key = node.get();

        PomModel model = getModel(key);
        if (model != null) {
            List<MavenDependency> dependencies = model.getRuntimeDependencies();
            for (MavenDependency mavenDependency : dependencies) {
                if (versionRegistry.containsKey(mavenDependency.extractKey())) {
                    // Already got a different version of this one... ignore it
                }
                else {
                    Node<VersionedMavenKey> child = node.add(mavenDependency);
                    versionRegistry.put(mavenDependency.extractKey(), mavenDependency.getVersionNumber().toString());
                }
            }

            List<Node<VersionedMavenKey>> children = node.getChildren();
            for (Node<VersionedMavenKey> childNode : children) {
                buildTreeRecursive(childNode, versionRegistry);
            }
        }
    }

    private void buildTreeRecursive(Node<MavenDependency> parent, VersionedMavenKey key) {
        PomModel model = getModel(key);
        if (model != null) {
            List<MavenDependency> dependencies = model.getDependencies();
            for (MavenDependency mavenDependency : dependencies) {
                if (mavenDependency.isRuntime()) {
                    Node<MavenDependency> node = parent.add(mavenDependency);
                    buildTreeRecursive(node, mavenDependency);
                }
            }
        }
        else {
            boolean okToIgnore = false;

            if (key instanceof MavenDependency) {
                MavenDependency mavenDependency = (MavenDependency) key;
                if (mavenDependency.isOptional()) {
                    // Ignore this
                    okToIgnore = true;
                }
            }

            if (!okToIgnore) {
                logger.warning("No model could be loaded for '{}'", key);
            }
        }
    }

    public List<File> getRuntimeArtifacts(File pom) {

        final List<File> files = new ArrayList<File>();
        Tree<VersionedMavenKey> buildTree = buildTree(pom);
        buildTree.visitBreadthFirst(new Tree.TreeVisitor<VersionedMavenKey>() {
            public void visit(VersionedMavenKey key) {
                File resolveArtifact = artifactResolver.resolveArtifact(key, MavenParser.this);
                files.add(resolveArtifact);
            }
        });

        return files;
    }

    private void recurse(PomModel model, List<File> artifacts) {
        artifacts.add(resolveArtifact(model));

        List<MavenDependency> dependencies = model.getDependencies();

        for (MavenDependency mavenDependency : dependencies) {
            PomModel childModel = getModel(mavenDependency);
            recurse(childModel, artifacts);
        }
    }

    public PomModel getModel(VersionedMavenKey mavenDependency) {
        return getModel(getPom(mavenDependency));
    }

    public void setArtifactResolver(PomArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    public void setLocationResolver(PomLocationResolver locationResolver) {
        this.locationResolver = locationResolver;
    }

    private PomModel getModel(File pom) {
        PomModel pomModel = parsedFiles.get(pom);
        if (pomModel == null) {
            if (pom.exists() && pom.length() != 0) {
                pomModel = PomModel.fromFile(pom, this);
                parsedFiles.put(pom, pomModel);
                parsedModels.put(pomModel.getKey(), pomModel);
            }
            else {
                logger.warning("Pom doesn't exist '{}'", pom.getAbsolutePath());
            }
        }
        return pomModel;
    }

    private File getPom(VersionedMavenKey VersionedMavenKey) {
        File location = locationResolver.resolveLocation(VersionedMavenKey, this);
        return location;
    }

    private List<MavenDependency> getRuntimeArtifactsRecusive(MavenDependency mavenDependency, Set<VersionedMavenKey> processed) {

        logger.trace("Getting runtime artifacts for {}", mavenDependency);

        File pom = getPom(mavenDependency);
        PomModel model = getModel(pom);
        List<MavenDependency> dependencies = model.getDependencies();

        List<MavenDependency> ordered = new ArrayList<MavenDependency>();
        for (MavenDependency mavenDependency2 : dependencies) {
            ordered.add(mavenDependency2);
            List<MavenDependency> childDeps = getRuntimeArtifactsRecusive(mavenDependency2, processed);
            for (MavenDependency mavenDependency3 : childDeps) {
                ordered.add(mavenDependency3);
            }
        }

        return ordered;

    }

    private File resolveArtifact(PomModel model) {
        return artifactResolver.resolveArtifact(model.getKey(), this);
    }

}
