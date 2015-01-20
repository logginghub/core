package com.logginghub.logging.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StacktraceUtils;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

public class SofBlockStreamRotatingReader {

    private File folder;
    private String prefix;
    private String postfix;

    private SofConfiguration configuration;

    private static final Logger logger = Logger.getLoggerFor(SofBlockStreamRotatingReader.class);

    public SofBlockStreamRotatingReader(File folder, String prefix, String postfix, SofConfiguration configuration) {
        this.folder = folder;
        this.prefix = prefix;
        this.postfix = postfix;
        this.configuration = configuration;
    }

    public void visit(final long start, final long end, final Destination<SerialisableObject> destination, boolean mostRecentFirst)
                    throws SofException {

        // TODO : implement most recent first

        Stopwatch fileListStopwatch = Stopwatch.start("Getting file list");
        File[] listFiles = RotatingHelper.getSortedFileList(folder, prefix, postfix);
        logger.fine(fileListStopwatch.stopAndFormat());

        for (File file : listFiles) {
            logger.fine("Visiting file '{}' ({} MB)", file.getName(), ByteUtils.formatMB((double) file.length()));
            // Stopwatch sw = Stopwatch.start("");

            SofBlockStreamReader reader = new SofBlockStreamReader(configuration);

            try {
                // Stopwatch sw2 = Stopwatch.start("Loading pointers : {}", file.getName());
                List<SofBlockPointer> loadPointers = reader.loadPointers(file);
                // logger.info(sw2);
                List<SofBlockPointer> interestingPointers = new ArrayList<SofBlockPointer>();

                for (SofBlockPointer sofBlockPointer : loadPointers) {
                    if(logger.willLog(Logger.finest)) {
                        logger.finest("Checking pointer {} against start {} and end {}", sofBlockPointer, start, end);
                    }
                    if (sofBlockPointer.overlaps(start, end)) {
                        interestingPointers.add(sofBlockPointer);
                    }
                }

                logger.fine("Found {} interesting pointers out of {} total pointers", interestingPointers.size(), loadPointers.size());
                if (interestingPointers.size() > 0) {
                    TimeFilterDestination filterDestination = new TimeFilterDestination(destination, start, end);
                    reader.visit(file, interestingPointers, filterDestination);
                    logger.fine("Decoded {} : {} passed the filter",
                                filterDestination.getPassed() + filterDestination.getFailed(),
                                filterDestination.getPassed());

                }
            }
            catch (Exception e) {
                logger.fine("Failed to visit file '{}' :  {}", file.getAbsolutePath(), e.getMessage());
//                logger.warn(e, "Failed to visit file '{}'", file.getAbsolutePath());
            }

            // logger.info("File '{}' visited in {}", file.getName(), sw.stopAndFormat());
        }

    }

    public void dumpIndex() throws IOException, SofException {

        File[] sortedFileList = RotatingHelper.getSortedFileList(folder, prefix, postfix);
        for (File file : sortedFileList) {
            System.out.println(file.getAbsolutePath());
            SofBlockStreamReader reader = new SofBlockStreamReader(configuration);
            List<SofBlockPointer> loadPointers = reader.loadPointers(file);
            for (SofBlockPointer sofBlockPointer : loadPointers) {
                System.out.println(sofBlockPointer);
            }
        }

    }

    public void healthCheck(Destination<String> destination) {
        
        StringUtilsBuilder sb = new StringUtils.StringUtilsBuilder();
        Stopwatch fileListStopwatch = Stopwatch.start("Getting file list");
        File[] listFiles = RotatingHelper.getSortedFileList(folder, prefix, postfix);
        sb.appendLine(fileListStopwatch.stopAndFormat());
        for (File file : listFiles) {
            sb.appendLine("{} | {}", file.getAbsolutePath(), file.length());
        }
        
        destination.send(sb.toString());        
        sb.clear();
        
        for (File file : listFiles) {
            
            sb.appendLine("Data file '{}'", file.getAbsolutePath());
            
            SofBlockStreamReader reader = new SofBlockStreamReader(configuration);
            List<SofBlockPointer> loadPointers;
            try {
                loadPointers = reader.loadPointers(file);
                for (SofBlockPointer sofBlockPointer : loadPointers) {
                    sb.appendLine(sofBlockPointer);
                }
            }
            catch (IOException e) {                
                sb.appendLine(StacktraceUtils.toString(e));                
            }
            catch (SofException e) {
                sb.appendLine(StacktraceUtils.toString(e));
            }
            
            destination.send(sb.toString());        
            sb.clear();
        }
        
    }

}
