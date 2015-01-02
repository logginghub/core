package com.logginghub.utils.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.VersionNumber;
import com.logginghub.utils.Xml;
import com.logginghub.utils.Xml.XmlEntry;
import com.logginghub.utils.logging.Logger;

public class PomModel {

    private static final Logger logger = Logger.getLoggerFor(PomModel.class);
    private VersionedMavenKey key;

    private VersionedMavenKey parentKey;

    private List<MavenDependency> dependencies = new ArrayList<MavenDependency>();

    private String content;
    private Xml xml;

    public static PomModel fromFile(File pomFile, PomModelProvider modelProvider) {
        PomModel pomModel = new PomModel();

        logger.trace("Loading {}", pomFile.getAbsolutePath());
        pomModel.content = FileUtils.read(pomFile);

        if (pomModel.content.length() == 0) {
            return null;
        }

        logger.trace("Attempting to extract maven key from : {}", pomModel.content);

        pomModel.xml = new Xml(pomModel.content);

        extractKey(pomModel);
        extractParent(pomModel);
        extractDependencies(pomModel, modelProvider);

        return pomModel;
    }

    @Override public String toString() {
        return getKey().toString();
    }

    public List<MavenDependency> getRuntimeDependencies() {
        List<MavenDependency> runtime = new ArrayList<MavenDependency>();
        for (MavenDependency mavenDependency : dependencies) {
            if (mavenDependency.isRuntime()) {
                runtime.add(mavenDependency);
            }
        }
        return runtime;
    }

    public List<MavenDependency> getDependencies() {
        return dependencies;
    }

    private static void extractDependencies(PomModel pomModel, PomModelProvider modelProvider) {

        XmlEntry root = pomModel.xml.getRoot();
        XmlEntry dependencies = root.getNode("dependencies");
        if (dependencies != null) {

            List<XmlEntry> findAllRecursive = dependencies.findAllRecursive("dependency");
            for (XmlEntry xmlEntry : findAllRecursive) {

                MavenDependency dependency = new MavenDependency();

                dependency.setGroup(xmlEntry.path("groupId"));
                dependency.setArtifact(xmlEntry.path("artifactId"));
                String scope = xmlEntry.pathRelaxed("scope");
                if (scope == null) {
                    scope = "compile";
                }
                dependency.setScope(scope);
                
                String classifier = xmlEntry.pathRelaxed("classifier");
                dependency.setClassifier(classifier);

                String version = xmlEntry.pathRelaxed("version");
                if (version != null) {
                    if (version.startsWith("${")) {
                        version = resolveVariable(version, pomModel, modelProvider);
                    }
                }
                else {
                    // if the version is null, it must be set via dependency management
                    version = resolveDependencyManagement(dependency, pomModel, modelProvider);
                }

                if (version == null) {
                    throw new RuntimeException(String.format("Failed to resolve version for %s", pomModel.getKey()));
                }

                dependency.setVersionNumber(VersionNumber.parse(version));

                String optional = xmlEntry.pathRelaxed("optional");
                if (optional != null) {
                    dependency.setOptional(Boolean.parseBoolean(optional));
                }

                pomModel.dependencies.add(dependency);
            }
        }

    }

    private static String resolveDependencyManagement(MavenDependency dependency, PomModel pomModel, PomModelProvider modelProvider) {

        String resolvedVersion = null;

        // Look in this pom for the dependency management section
        XmlEntry node = pomModel.getXml().getRoot().getNode("dependencyManagement");
        if (node != null) {
            List<XmlEntry> findAllRecursive = node.findAllRecursive("dependency");
            for (XmlEntry xmlEntry : findAllRecursive) {
                String group = xmlEntry.pathRelaxed("groupId");
                String artifact = xmlEntry.pathRelaxed("artifactId");

                if (dependency.getArtifact().equals(artifact) && dependency.getGroup().equals(group)) {
                    String version = xmlEntry.pathRelaxed("versionId");
                    resolvedVersion = resolveVariable(version, pomModel, modelProvider);
                    break;
                }
            }
        }

        // TODO : refactor
        if (resolvedVersion == null) {
            PomModel parentModel = modelProvider.getModel(pomModel.getParentKey());
            node = parentModel.getXml().getRoot().getNode("dependencyManagement");
            if (node != null) {
                List<XmlEntry> findAllRecursive = node.findAllRecursive("dependency");
                for (XmlEntry xmlEntry : findAllRecursive) {
                    String group = xmlEntry.pathRelaxed("groupId");
                    String artifact = xmlEntry.pathRelaxed("artifactId");

                    if (dependency.getArtifact().equals(artifact) && dependency.getGroup().equals(group)) {
                        String version = xmlEntry.pathRelaxed("version");
                        resolvedVersion = resolveVariable(version, parentModel, modelProvider);
                        break;
                    }
                }
            }
        }

        return resolvedVersion;

    }

    private static String resolveVariable(String property, PomModel pomModel, PomModelProvider modelProvider) {
        if (property.startsWith("${")) {
            if (property.equals("${project.parent.version}")) {
                return pomModel.getParentKey().getVersionNumber().toString();
            }
            
            if (property.equals("${parent.version}")) {
                return pomModel.getParentKey().getVersionNumber().toString();
            }

            if (property.equals("${project.version}")) {
                return pomModel.getKey().getVersionNumber().toString();
            }

            // TODO: load properties in once
            String between = StringUtils.between(property, "${", "}");

            String pathRelaxed = pomModel.xml.pathRelaxed("project", "properties", between);
            if (pathRelaxed == null) {
                // Maybe its in the parent?
                PomModel parentModel = modelProvider.getModel(pomModel.getParentKey());
                pathRelaxed = resolveVariable(property, parentModel, modelProvider);
            }

            return pathRelaxed;
        }
        else {
            return property;
        }

    }

    private static void extractParent(PomModel pomModel) {
        if (pomModel.xml.pathExists("project.parent.artifactId")) {

            String artifact = pomModel.xml.path("project.parent.artifactId");
            String group = pomModel.xml.path("project.parent.groupId");
            String version = pomModel.xml.path("project.parent.version");

            pomModel.parentKey = new VersionedMavenKey();
            pomModel.parentKey.setArtifact(artifact);
            pomModel.parentKey.setGroup(group);
            pomModel.parentKey.setVersionNumber(VersionNumber.parse(version));

        }
        else {
            pomModel.parentKey = null;
        }
    }

    public String getContent() {
        return content;
    }

    public VersionedMavenKey getKey() {
        return key;
    }

    public VersionedMavenKey getParentKey() {
        return parentKey;
    }

    public Xml getXml() {
        return xml;
    }

    private static void extractKey(PomModel pomModel) {
        String artifact = pomModel.xml.path("project.artifactId");

        // Group and version can come from this or its parent
        String group;
        String version;

        if (pomModel.xml.pathExists("project.groupId")) {
            group = pomModel.xml.path("project.groupId");
        }
        else {
            group = pomModel.xml.path("project.parent.groupId");
        }

        if (pomModel.xml.pathExists("project.version")) {
            version = pomModel.xml.path("project.version");
        }
        else {
            version = pomModel.xml.path("project.parent.version");
        }

        pomModel.key = new VersionedMavenKey();
        pomModel.key.setArtifact(artifact);
        pomModel.key.setGroup(group);
        pomModel.key.setVersionNumber(VersionNumber.parse(version));
    }

}
