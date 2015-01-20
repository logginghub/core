package com.logginghub.logging.repository;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.FileDateFormat;
import com.logginghub.utils.StringUtils;

public class RotatingHelper {
    
    private static DateFormat dateFormat = new FileDateFormat();
    
    public static File[] getSortedFileList(File folder, final String prefix, final String postfix) {
        File[] listFiles = folder.listFiles(new FileFilter() {
            @Override public boolean accept(File file) {
                String fullTimeRegex = "{}\\d{8}\\.\\d{6}(\\.\\d+)?{}";
                boolean matches = StringUtils.matches(file.getName(), fullTimeRegex, prefix, postfix);
                return matches;
            }
        });

        sortArrayByTimestamp(listFiles, prefix, postfix);
        return listFiles;
    }

    public static synchronized void sortArrayByTimestamp(File[] listFiles, final String prefix, final String postfix) {
        Arrays.sort(listFiles, new Comparator<File>() {
            @Override public int compare(File a, File b) {

                String aName = a.getName();
                String bName = b.getName();

                String aTimePart = StringUtils.between(aName, prefix + ".", postfix);
                String bTimePart = StringUtils.between(bName, prefix + ".", postfix);

                // Deal with the .1 bit we have to put on the end if we write
                // too many updates in one time period
                String aJustTimePart = StringUtils.beforeLast(aTimePart, ".");
                String bJustTimePart = StringUtils.beforeLast(bTimePart, ".");

                String aIncrementText = StringUtils.afterLast(aTimePart, ".");
                String bIncrementText = StringUtils.afterLast(bTimePart, ".");

                int aIncrement = 0;
                int bIncrement = 0;

                if (aIncrementText.length() > 0) {
                    aIncrement = Integer.parseInt(aIncrementText);
                }

                if (bIncrementText.length() > 0) {
                    bIncrement = Integer.parseInt(bIncrementText);
                }

                try {
                    Date aDate = dateFormat.parse(aJustTimePart);
                    Date bDate = dateFormat.parse(bJustTimePart);

                    return CompareUtils.add(aDate, bDate).add(aIncrement, bIncrement).compare();
                }
                catch (ParseException e) {
                    throw new RuntimeException(String.format("Failed to parse date part of files %s and %s", a.getAbsolutePath(), b.getAbsoluteFile()),
                                               e);
                }

            }
        });
    }

}
