package com.logginghub.logging.repository;

import java.io.Closeable;

public abstract class WritableSofBlock implements Closeable {

    private long startTime;
    private long endTime;
    private long encodedLength;
    private long unencodedLength;

   

}
