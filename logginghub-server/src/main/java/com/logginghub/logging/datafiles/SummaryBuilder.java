package com.logginghub.logging.datafiles;

import com.logginghub.logging.datafiles.aggregation.Aggregation;
import com.logginghub.logging.datafiles.aggregation.PatternAggregation;
import com.logginghub.logging.datafiles.aggregation.TimeAggregation;
import com.logginghub.utils.Destination;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofExpandingBufferSerialiser;
import com.logginghub.utils.sof.SofPartialDecodeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by james on 18/09/15.
 */
public class SummaryBuilder {

    private long intervalLength = 1000;

    private FactoryMap<Long, SummaryTimeElement> summaryTimeElements = new FactoryMap<Long, SummaryTimeElement>() {
        @Override
        protected SummaryTimeElement createEmptyValue(Long key) {
            return new SummaryTimeElement(key, intervalLength);
        }
    };

    public void build(Aggregation aggregation) {

        Collection<PatternAggregation> patternData = aggregation.getPatternData();
        for (PatternAggregation patternAggregation : patternData) {

            int patternId = patternAggregation.getPatternId();

            List<TimeAggregation> sortedTimeAggregations = patternAggregation.getSortedTimeAggregations();
            for (TimeAggregation sortedTimeAggregation : sortedTimeAggregations) {
                long time = sortedTimeAggregation.getTime();
                long count = sortedTimeAggregation.getCount();

                summaryTimeElements.get(time).update(patternId, count);
            }

        }

    }

    public void save(File file) throws IOException, SofException {

        List<SummaryTimeElement> sortedByTime = new ArrayList<SummaryTimeElement>(summaryTimeElements.values());
        Collections.sort(sortedByTime, new Comparator<SummaryTimeElement>() {
            @Override
            public int compare(SummaryTimeElement o1, SummaryTimeElement o2) {
                return Long.compare(o1.getTime(), o2.getTime());
            }
        });

        FileOutputStream fos = new FileOutputStream(file);
        FileChannel channel = fos.getChannel();

        SofConfiguration sofConfiguration = getSofConfiguration();

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();

        for (SummaryTimeElement summaryTimeElement : sortedByTime) {
            SofExpandingBufferSerialiser.write(buffer, summaryTimeElement, sofConfiguration);
            buffer.flip();
            channel.write(buffer.getBuffer());
            buffer.clear();
        }

        channel.close();

    }

    private static SofConfiguration getSofConfiguration() {
        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(SummaryTimeElement.class, 0);
        return sofConfiguration;
    }

    public static void replay(File file, Destination<SummaryTimeElement> destination) {

        SofConfiguration sofConfiguration = getSofConfiguration();

        FileChannel channel = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteBuffer buffer = ByteBuffer.allocateDirect(1 * 1024 * 1024);

            int read;
            channel = fis.getChannel();
            while ((read = channel.read(buffer)) != -1) {

                buffer.flip();
                try {
                    while (buffer.hasRemaining()) {
                        buffer.mark();
                        SummaryTimeElement decoded = SofExpandingBufferSerialiser.read(buffer, sofConfiguration);
                        destination.send(decoded);
                    }
                } catch (SofPartialDecodeException e) {
                    buffer.reset();
                } catch (BufferUnderflowException e) {
                    buffer.reset();
                } catch (SofException e) {
                    e.printStackTrace();
                }

                buffer.compact();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.closeQuietly(channel);
        }
    }


}
