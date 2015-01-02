package com.logginghub.utils.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.WildcardMatcher;

public class FileSearchFilter
{
    private File baseFolder;

    private List<String> fileIncludePatterns = new ArrayList<String>();
    private List<String> fileExcludePatterns = new ArrayList<String>();

    private List<String> folderIncludePatterns = new ArrayList<String>();
    private List<String> folderExcludePatterns = new ArrayList<String>();

    public File getBaseFolder()
    {
        return baseFolder;
    }

    public void setBaseFolder(File baseFolder)
    {
        this.baseFolder = baseFolder;
    }

    public boolean passes(File file)
    {
        boolean passes;
        if (file.isDirectory())
        {
            passes = checkAgainst(file,
                                  this.folderIncludePatterns,
                                  this.folderExcludePatterns);
        }
        else
        {
            passes = checkAgainst(file,
                                  this.fileIncludePatterns,
                                  this.fileExcludePatterns);
        }
        return passes;
    }

    private static boolean checkAgainst(File file,
                                        List<String> includePatterns,
                                        List<String> exludePatterns)
    {
        String path = file.getAbsolutePath();

        boolean passes = true;

        if (includePatterns.size() > 0)
        {
            passes = false;
            for (String string : includePatterns)
            {
                WildcardMatcher matcher = new WildcardMatcher(string);
                passes = matcher.matches(path);
                if (passes)
                {
                    break;
                }
            }
        }
        else
        {
            // No include filtering, so include everything
            passes = true;
        }

        if (exludePatterns.size() > 0 && passes)
        {
            for (String string : exludePatterns)
            {
                WildcardMatcher matcher = new WildcardMatcher(string);
                passes = !matcher.matches(path);
                if (!passes)
                {
                    break;
                }
            }
        }
        else
        {
            // No exclude filtering means we just leave the result from the
            // includes be
        }

        return passes;
    }

    public void addFileIncludeWildcard(String includeWildcard)
    {
        fileIncludePatterns.add(includeWildcard);
    }

    public void addFileExcludeWildcard(String excludes)
    {
        fileExcludePatterns.add(excludes);
    }

    public void addFolderIncludeWildcard(String includeWildcare)
    {
        folderIncludePatterns.add(includeWildcare);
    }

    public void addFolderExcludeWildcard(String excludeWildcare)
    {
        folderExcludePatterns.add(excludeWildcare);
    }

    public List<String> getFileExcludePatterns()
    {
        return fileExcludePatterns;
    }

    public List<String> getFileIncludePatterns()
    {
        return fileIncludePatterns;
    }

    public List<String> getFolderExcludePatterns()
    {
        return folderExcludePatterns;
    }

    public List<String> getFolderIncludePatterns()
    {
        return folderIncludePatterns;
    }

    public void clearFilters()
    {
        fileIncludePatterns.clear();
        fileExcludePatterns.clear();
        folderIncludePatterns.clear();
        folderExcludePatterns.clear();
    }
}
