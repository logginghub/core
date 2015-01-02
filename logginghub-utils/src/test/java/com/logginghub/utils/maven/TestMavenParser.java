package com.logginghub.utils.maven;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.utils.Tree;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.maven.MavenParser;
import com.logginghub.utils.maven.VersionedMavenKey;

public class TestMavenParser {

    @Ignore// This is defintely not a good unit test!!
    @Test public void testGetRuntimeArtifacts() {

        Logger.setRootLevel(Logger.debug);

        MavenParser mavenParser = new MavenParser();
        mavenParser.setLocationResolver(new MavenParser.WorkspaceFirstPomLocationResolver(new File("D:\\Development\\July2012\\vlcoregit")));
        mavenParser.setArtifactResolver(new MavenParser.WorkspaceFirstPomArtifactResolver(new File("D:\\Development\\July2012\\vlcoregit")));

        File pom = new File("D:\\Development\\July2012\\vlcoregit\\vl-logging-repo\\pom.xml");

        Tree<VersionedMavenKey> buildTree = mavenParser.buildTree(pom);
        System.out.println(buildTree);

        List<File> runtimeArtifacts = mavenParser.getRuntimeArtifacts(pom);

        assertThat(runtimeArtifacts.get(0).getAbsolutePath(), is("D:\\Development\\July2012\\vlcoregit\\vl-logging-repo\\target\\classes"));
        assertThat(runtimeArtifacts.get(1).getAbsolutePath(), is("D:\\Development\\July2012\\vlcoregit\\vl-messaging3\\target\\classes"));
        assertThat(runtimeArtifacts.get(2).getAbsolutePath(), is("D:\\Development\\July2012\\vlcoregit\\vl-logging-client\\target\\classes"));
        assertThat(runtimeArtifacts.get(3).getAbsolutePath(), is("D:\\Development\\mavenRepository\\org\\fusesource\\sigar\\1.6.4\\sigar-1.6.4.jar"));
        assertThat(runtimeArtifacts.get(4).getAbsolutePath(), is("D:\\Development\\mavenRepository\\org\\fusesource\\sigar-native\\1.6.4\\sigar-native-1.6.4.jar"));
        assertThat(runtimeArtifacts.get(5).getAbsolutePath(), is("D:\\Development\\mavenRepository\\net\\sf\\opencsv\\opencsv\\2.0\\opencsv-2.0.jar"));
        assertThat(runtimeArtifacts.get(6).getAbsolutePath(), is("D:\\Development\\mavenRepository\\backport-util-concurrent\\backport-util-concurrent\\3.1\\backport-util-concurrent-3.1.jar"));

        assertThat(runtimeArtifacts.get(7).getAbsolutePath(), is("D:\\Development\\July2012\\vlcoregit\\vl-logging\\target\\classes"));
        assertThat(runtimeArtifacts.get(8).getAbsolutePath(), is("D:\\Development\\July2012\\vlcoregit\\vl-utils\\target\\classes"));
        assertThat(runtimeArtifacts.get(9).getAbsolutePath(), is("D:\\Development\\mavenRepository\\com\\miglayout\\miglayout\\3.7.2\\miglayout-3.7.2.jar"));
        assertThat(runtimeArtifacts.get(10).getAbsolutePath(), is("D:\\Development\\mavenRepository\\commons-lang\\commons-lang\\2.5\\commons-lang-2.5.jar"));
    }

}
