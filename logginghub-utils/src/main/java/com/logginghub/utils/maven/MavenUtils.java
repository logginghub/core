package com.logginghub.utils.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.VersionNumber;
import com.logginghub.utils.Xml;
import com.logginghub.utils.Xml.XmlEntry;
import com.logginghub.utils.logging.Logger;

public class MavenUtils {

    private static final Logger logger = Logger.getLoggerFor(MavenUtils.class);

    public interface ScopeFilter {
        boolean passes(String scope);
    }

    public static VersionedMavenKey getParentKey(File basedir) {
        String read = FileUtils.read(getPomFile(basedir));
        Xml xml = new Xml(read);

        VersionedMavenKey parentKey;

        if (xml.pathExists("project.parent.artifactId")) {

            String artifact = xml.path("project.parent.artifactId");
            String group = xml.path("project.parent.groupId");
            String version = xml.path("project.parent.version");

            parentKey = new VersionedMavenKey();
            parentKey.artifact = artifact;
            parentKey.group = group;
            parentKey.versionNumber = VersionNumber.parse(version);

        }
        else {
            parentKey = null;
        }
        return parentKey;
    }

    public static String getPackaging(File basedir) {
        Xml xml = new Xml(FileUtils.read(getPomFile(basedir)));
        String packaging = xml.pathRelaxed("project.packaging");
        return packaging;
    }

    public static VersionedMavenKey getKey(File basedir) {

        File pomFile = getPomFile(basedir);
        logger.trace("Loading {}", pomFile.getAbsolutePath());
        String contents = FileUtils.read(pomFile);

        if (contents.length() == 0) {
            return null;
        }

        logger.trace("Attempting to extract maven key from : {}", contents);

        Xml xml = new Xml(contents);

        String artifact = xml.path("project.artifactId");

        // Group and version can come from this or its parent
        String group;
        String version;

        if (xml.pathExists("project.groupId")) {
            group = xml.path("project.groupId");
        }
        else {
            group = xml.path("project.parent.groupId");
        }

        if (xml.pathExists("project.version")) {
            version = xml.path("project.version");
        }
        else {
            version = xml.path("project.parent.version");
        }

        VersionedMavenKey key = new VersionedMavenKey();
        key.artifact = artifact;
        key.group = group;
        key.versionNumber = VersionNumber.parse(version);
        return key;
    }

    public static List<VersionedMavenKey> getDeclaredDependencies(File basedir, ScopeFilter scopeFilter) {
        List<VersionedMavenKey> dependenciesList = new ArrayList<VersionedMavenKey>();
        File pomFile = getPomFile(basedir);
        logger.debug("Parsing XML from {}", pomFile.getAbsolutePath());
        String contents = FileUtils.read(pomFile);
        if (contents.length() > 0) {
            Xml xml = new Xml(contents);
            XmlEntry root = xml.getRoot();
            if (root != null) {

                String parentVersion = xml.pathRelaxed("project.parent.version");
                logger.trace("Parent version is '{}'", parentVersion);

                XmlEntry dependencies = root.getNode("dependencies");
                if (dependencies != null) {

                    List<XmlEntry> findAllRecursive = dependencies.findAllRecursive("dependency");
                    for (XmlEntry xmlEntry : findAllRecursive) {

                        String group = xmlEntry.path("groupId");
                        String artifact = xmlEntry.path("artifactId");
                        String scope = xmlEntry.pathRelaxed("scope");
                        if (scope == null) {
                            scope = "compile";
                        }

                        if (scopeFilter.passes(scope)) {
                            VersionedMavenKey key;
                            String version = xmlEntry.pathRelaxed("version");

                            logger.trace("Depdendency : {}::{}::{} ({})", group, artifact, version, scope);

                            if (version == null) {
                                // It must come from a dependencyManagement block then...
                                XmlEntry node = root.getNode("dependencyManagement");
                                if (node != null) {
                                    List<XmlEntry> deps = node.findAllRecursive("dependency");
                                    for (XmlEntry xmlEntry2 : deps) {
                                        String group2 = xmlEntry2.path("groupId");
                                        String artifact2 = xmlEntry2.path("artifactId");

                                        if (group2.equals(group) && artifact2.equals(artifact)) {
                                            version = xmlEntry2.path("version");
                                            break;
                                        }
                                    }
                                }
                                else {
                                    logger.warning("Couldn't find the dependency management node");
                                }
                            }

                            if (version == null) {
                                // Gah. Maybe its from the parent chain dependency management?!
                                version = resolveVersionFromParent(group, artifact, xml);
                            }

                            if (version != null) {
                                try {
                                    if (version.startsWith("${project")) {
                                        version = parentVersion;
                                    }

                                    logger.trace("Depdendency (version resolved) : {}::{}::{}", group, artifact, version);
                                    key = buildVersionedMavenKey(xmlEntry, version);

                                    if (!dependenciesList.contains(key)) {
                                        logger.debug("Adding dependency {}", key);
                                        dependenciesList.add(key);
                                    }
                                }
                                catch (NumberFormatException nfe) {
                                    logger.warning("Couldn't find the version for dependency {}:{}:{}", group, artifact, nfe.getMessage());
                                }
                            }
                            else {
                                logger.warning("Couldn't find the version for dependency {}:{}", group, artifact);
                            }
                        }
                    }
                }
                else {
                    logger.trace("Pom has no dependencies!");
                }
            }
        }

        return dependenciesList;
    }

    private static String resolveVersionFromParent(String group, String artifact, Xml xml) {

        String version = null;

        VersionedMavenKey parent = getParentKey(xml);
        if (parent != null) {
            File parentPom = getPomArtifactForKey(parent);
            if (parentPom.exists()) {
                Xml parentXml = new Xml(FileUtils.readAsString(parentPom));

                XmlEntry root = parentXml.getRoot();
                XmlEntry dependencyManagementNode = root.getNode("dependencyManagement");
                if (dependencyManagementNode != null) {

                    List<XmlEntry> children = dependencyManagementNode.findAllRecursive("dependency");
                    for (XmlEntry xmlEntry : children) {
                        if (group.equals(xmlEntry.path("groupId")) && artifact.equals(xmlEntry.path("artifactId"))) {
                            version = xmlEntry.path("version");
                            break;
                        }
                    }

                    if (version != null && version.startsWith("${")) {
                        version = resolveProperty(version, parentXml);
                    }

                }

            }
            else {
                logger.warning("Parent pom {} not found", parentPom.getAbsolutePath());
                version = null;
            }

        }
        else {
            version = null;
        }

        return version;
    }

    private static String resolveProperty(String property, Xml xml) {

        String between = StringUtils.between(property, "${", "}");
        String pathRelaxed = xml.pathRelaxed("project.properties." + between);
        if (pathRelaxed == null) {
            pathRelaxed = property;
        }

        return pathRelaxed;

    }

    private static VersionedMavenKey getParentKey(Xml xml) {

        VersionedMavenKey parent;
        XmlEntry parentNode = xml.getRoot().getNode("parent");
        if (parentNode != null) {
            String group = parentNode.path("groupId");
            String artifact = parentNode.path("artifactId");
            String version = parentNode.pathRelaxed("version");
            parent = new VersionedMavenKey(group, artifact, VersionNumber.parse(version));
        }
        else {
            parent = null;
        }

        return parent;
    }

    private static File getPomFile(File file) {
        if (file.isFile()) {
            return file;
        }
        else {
            return new File(file, "pom.xml");
        }
    }

    private static VersionedMavenKey buildVersionedMavenKey(XmlEntry xmlEntry) {
        String artifact = xmlEntry.path("artifactId");
        String group = xmlEntry.path("groupId");
        String version = xmlEntry.path("version");

        VersionedMavenKey key = new VersionedMavenKey();
        key.artifact = artifact;
        key.group = group;
        key.versionNumber = VersionNumber.parse(version);
        return key;
    }

    private static VersionedMavenKey buildVersionedMavenKey(XmlEntry xmlEntry, String parentVersion) {
        String artifact = xmlEntry.path("artifactId");
        String group = xmlEntry.path("groupId");

        VersionedMavenKey key = new VersionedMavenKey();
        key.artifact = artifact;
        key.group = group;
        key.versionNumber = VersionNumber.parse(parentVersion);
        return key;
    }

    public static File getRepositoryPathFromMavenSettings() {
        String homeDirectoryPath = System.getProperty("user.home");
        File repositoryPath = new File(homeDirectoryPath, ".m2/repository");

        File homeDirectory = new File(homeDirectoryPath);
        File configuration = new File(homeDirectory, ".m2/settings.xml");
        if (configuration.exists()) {
            Xml xml = Xml.parse(configuration);
            if (xml.pathExists("settings.localRepository")) {
                repositoryPath = new File(xml.path("settings.localRepository"));
            }
        }
        return repositoryPath;
    }

    public static File getJarArtifactName(VersionedMavenKey key) {
        File jar = new File(StringUtils.format("{}-{}.jar", key.artifact, key.versionNumber.toString().replace("-", ".")));
        return jar;
    }

    public static File getJarArtifactForKey(VersionedMavenKey key) {
        File repositoryPath = MavenUtils.getRepositoryPathFromMavenSettings();
        File artifactFolder = getArtifactFolder(key, repositoryPath);
        File jar;
        if (StringUtils.isNotNullOrEmpty(key.getClassifier())) {
            jar = new File(artifactFolder, StringUtils.format("{}-{}-{}.jar",
                                                              key.artifact,
                                                              key.versionNumber.toString().replace("-", "."),
                                                              key.getClassifier()));
        }
        else {
            jar = new File(artifactFolder, StringUtils.format("{}-{}.jar", key.artifact, key.versionNumber.toString().replace("-", ".")));
        }
        return jar;
    }

    public static File getPomArtifactForKey(VersionedMavenKey key) {
        File repositoryPath = MavenUtils.getRepositoryPathFromMavenSettings();
        File artifactFolder = getArtifactFolder(key, repositoryPath);
        File jar = new File(artifactFolder, StringUtils.format("{}-{}.pom", key.artifact, key.versionNumber.toString()));
        if (!jar.exists() && key.versionNumber.toString().contains("-")) {
            jar = new File(artifactFolder, StringUtils.format("{}-{}.pom", key.artifact, key.versionNumber.toString().replace("-", ".")));
        }
        return jar;
    }

    private static File getArtifactFolder(VersionedMavenKey key, File repositoryPath) {
        File artifactFolder = new File(repositoryPath, StringUtils.format("{}/{}/{}",
                                                                          key.group.replace('.', File.separatorChar),
                                                                          key.artifact,
                                                                          key.versionNumber.toString()));
        if (!artifactFolder.exists()) {
            artifactFolder = new File(repositoryPath, StringUtils.format("{}/{}/{}",
                                                                         key.group.replace('.', File.separatorChar),
                                                                         key.artifact,
                                                                         key.versionNumber.toString().replace("-", ".")));
        }
        return artifactFolder;
    }

    public static List<VersionedMavenKey> getFullDependencies(File pom, ScopeFilter scopeFilter) {

        VersionedMavenKey key = getKey(pom);

        logger.debug("*** Getting full dependencies for {}", key);
        logger.moreIndent();
        List<VersionedMavenKey> all = new ArrayList<VersionedMavenKey>();

        logger.trace("Checking direct declared dependencies of {} ...", key);
        logger.moreIndent();
        List<VersionedMavenKey> direct = getDeclaredDependencies(pom, scopeFilter);
        logger.debug("Declared dependencies of {}", key);
        for (VersionedMavenKey VersionedMavenKey : direct) {
            logger.debug("   * {}::{}::{}", VersionedMavenKey.getGroup(), VersionedMavenKey.getArtifact(), VersionedMavenKey.getVersionNumber());
        }
        logger.lessOutdent();

        logger.trace("Checking parent dependencies of {} ...", key);
        logger.moreIndent();
        List<VersionedMavenKey> parent = getParentDependencies(pom, scopeFilter);
        for (VersionedMavenKey VersionedMavenKey : parent) {
            logger.trace("Parent dependency found : {}", VersionedMavenKey);
        }
        logger.lessOutdent();
        logger.trace("Done with parents");

        // Add the main dependencies first, so direct references will always trump an inherited
        // entry
        all.addAll(direct);
        all.addAll(parent);

        // Add the dependencies in order
        List<VersionedMavenKey> reordered = new ArrayList<VersionedMavenKey>();

        logger.trace("Checking child transitive dependencies...");
        logger.moreIndent();
        for (VersionedMavenKey VersionedMavenKey : direct) {
            logger.trace("Processing dependency {} to check for its dependencies...", VersionedMavenKey);
            reordered.add(VersionedMavenKey);

            if (VersionedMavenKey.getArtifact().equals("jetty-server")) {
                System.out.println("FUCK!");
            }
            File pomArtifactForKey = getPomArtifactForKey(VersionedMavenKey);
            if (pomArtifactForKey.exists()) {
                List<VersionedMavenKey> upstreamDependenciesRecursive = getFullDependencies(pomArtifactForKey, scopeFilter);

                for (VersionedMavenKey VersionedMavenKey2 : upstreamDependenciesRecursive) {
                    if (!all.contains(VersionedMavenKey2)) {
                        logger.trace("Adding dependency {}", VersionedMavenKey2);
                        reordered.add(VersionedMavenKey2);
                    }
                }
            }
            else {
                logger.warning("  !! Couldn't find pom for key {} at {}", VersionedMavenKey, pomArtifactForKey);
            }
        }
        logger.lessOutdent();
        logger.trace("Done checking child transitive dependencies");
        for (VersionedMavenKey VersionedMavenKey : reordered) {
            logger.trace("    {}::{}::{}", VersionedMavenKey.getGroup(), VersionedMavenKey.getArtifact(), VersionedMavenKey.getVersionNumber());
        }
        logger.lessOutdent();
        return reordered;

    }

    private static List<VersionedMavenKey> getParentDependencies(File pom, ScopeFilter scopeFilter) {
        List<VersionedMavenKey> parentDependencies;

        VersionedMavenKey parentKey = MavenUtils.getParentKey(pom);
        if (parentKey != null) {
            parentDependencies = getFullDependencies(getPomArtifactForKey(parentKey), scopeFilter);
        }
        else {
            parentDependencies = Collections.emptyList();
        }

        return parentDependencies;
    }

    private static VersionedMavenKey getParentKey(VersionedMavenKey VersionedMavenKey) {
        File pomArtifactForKey = getPomArtifactForKey(VersionedMavenKey);
        VersionedMavenKey parentKey = getKey(pomArtifactForKey);
        return parentKey;
    }

    public static List<VersionedMavenKey> getFullDependencies(VersionedMavenKey key, ScopeFilter scopeFilter) {
        File pomArtifactForKey = getPomArtifactForKey(key);
        return getFullDependencies(pomArtifactForKey, scopeFilter);
    }

    public static List<VersionedMavenKey> getUpstreamDependencies(File basedir) {
        List<VersionedMavenKey> dependenciesList = new ArrayList<VersionedMavenKey>();
        Xml xml = new Xml(FileUtils.read(new File(basedir, "pom.xml")));

        XmlEntry dependencies = xml.getRoot().getNode("dependencies");
        if (dependencies != null) {

            List<XmlEntry> findAllRecursive = dependencies.findAllRecursive("dependency");
            for (XmlEntry xmlEntry : findAllRecursive) {
                VersionedMavenKey key = buildVersionedMavenKey(xmlEntry);
                dependenciesList.add(key);
            }
        }

        return dependenciesList;
    }

    public static List<VersionedMavenKey> getRuntimeDependencies(File pom) {
        return getFullDependencies(pom, new ScopeFilter() {
            public boolean passes(String scope) {
                return scope.equals("compile") || scope.equals("runtime");
            }
        });
    }

}
