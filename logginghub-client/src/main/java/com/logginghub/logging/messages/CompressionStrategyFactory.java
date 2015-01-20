package com.logginghub.logging.messages;

import com.logginghub.utils.FormattedRuntimeException;


public class CompressionStrategyFactory {

    public final static byte compression_none = 0;   
    public final static byte compression_snappy = 1;
    public final static byte compression_lz4 = 2;
    public final static byte compression_zlib = 3;

    public static CompressionStrategy createStrategy(byte strategy) {
        switch (strategy) {
            case compression_none: return new NoopCompressionStrategy();
            case compression_snappy : return new SnappyCompressionStrategy();
            case compression_lz4 : return new LZ4CompressionStrategy();
            case compression_zlib : return new FlatorCompressionStrategy();
            default:
                throw new FormattedRuntimeException("We dont recognise CompressionStrategy '{}'", strategy);
        }
    }
    
}
