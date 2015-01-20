package com.logginghub.logging.modules.history;

import com.logginghub.logging.messages.LZ4CompressionStrategy;
import com.logginghub.logging.messages.SofSerialisationStrategy;
import com.logginghub.logging.modules.history.CompressedBlockEventBuffer;
import com.logginghub.logging.modules.history.EventBuffer;
import com.logginghub.utils.ByteUtils;

public class TestCompressedBlockEventBuffer extends AbstractEventBufferTest {

    @Override protected EventBuffer createBuffer() {
        return new CompressedBlockEventBuffer(new LZ4CompressionStrategy(),
                                              new SofSerialisationStrategy(false, true),
                                              (int) ByteUtils.kilobytes(256),
                                              (int) ByteUtils.megabytes(1));

    }
}
