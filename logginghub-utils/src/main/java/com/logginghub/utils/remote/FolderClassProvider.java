package com.logginghub.utils.remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileVisitor;
import com.logginghub.utils.StringUtils;

public class FolderClassProvider implements ClassDataProvider {
    private final File root;

    public FolderClassProvider(File root) {
        this.root = root;
    }

    public byte[] getClassBytes(String classname) {
        String path = StringUtils.classnameToFilename(classname);
        return getResourceBytes(path);
    }

    public String toString() {
        return StringUtils.reflectionToString(this);
    }

    public byte[] getResourceBytes(String resourcePath) {
        File file = new File(root, resourcePath);
        byte[] b = null;
        if (file.exists()) {
            b = FileUtils.getFileAsBytes(file);
        }

        return b;
    }

    public Collection<String> enumerateClasses() {

        final List<String> classnames = new ArrayList<String>();
        FileUtils.visitChildrenRecursively(root, new FileVisitor() {
            public void visitFile(File file) {
                if (file.getName().endsWith(".class")) {
                    classnames.add(toClassname(file));
                }
            }
        });

        return classnames;
    }

    protected String toClassname(File file) {
        String relativeName = FileUtils.getRelativeName(file, root);
        String classname = relativeName.substring(0, relativeName.length() - 6).replace(File.separatorChar, '.');
        return classname;
    }
}
