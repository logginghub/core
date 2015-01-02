package com.logginghub.utils.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSearchUtils
{
    public static void search(FileSearchFilter filter, FileSearchListener listener)
    {
        File file = filter.getBaseFolder();
        recursivelySearch(filter, file, listener);
    }
    
    private static void recursivelySearch(FileSearchFilter filter,
                                          File target,
                                          FileSearchListener listener)
    {
        if (target.isDirectory())
        {
            if (target.equals(filter.getBaseFolder()) || filter.passes(target))
            {
                File[] files = target.listFiles();
                if (files != null)
                {
                    for (File file : files)
                    {
                        recursivelySearch(filter, file, listener);
                    }
                }
            }
            else
            {
                listener.onFolderIgnored(target, null);
//                System.out.println("Ignoring path : " +
//                                   target.getAbsolutePath());
            }
        }
        else
        {
            if (filter.passes(target))
            {
                listener.onFileFound(target);
            }
            else
            {
                listener.onFileIgnored(target, null);
            }
        }
    }

    public static List<File> search(FileSearchFilter filter)
    {
        File file = filter.getBaseFolder();
        List<File> files = new ArrayList<File>();
        recursivelySearch(filter, file, files);
        return files;
    }

    private static void recursivelySearch(FileSearchFilter filter,
                                          File target,
                                          List<File> matchingFiles)
    {
        if (target.isDirectory())
        {
            if (target.equals(filter.getBaseFolder()) || filter.passes(target))
            {
                File[] files = target.listFiles();
                if (files != null)
                {
                    for (File file : files)
                    {
                        recursivelySearch(filter, file, matchingFiles);
                    }
                }
            }
            else
            {
//                System.out.println("Ignoring path : " +
//                                   target.getAbsolutePath());
            }
        }
        else
        {
            if (filter.passes(target))
            {
                matchingFiles.add(target);
            }
            else
            {
//                System.out.println("Ignoring : " + target.getAbsolutePath());
            }
        }
    }

}
