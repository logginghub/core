package com.logginghub.utils.sof;

import java.io.EOFException;
import java.nio.ByteBuffer;

import com.logginghub.utils.ExpandingByteBuffer;

public class SofExpandingBufferSerialiser {

    public static byte[] write(SerialisableObject serialisableObject, SofConfiguration resolver) throws SofException {
        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        WriterAbstraction writer = new ExpandingByteBufferWriterAbstraction(buffer);
        SofSerialiser.write(writer, serialisableObject, resolver);
        
        buffer.flip();
        byte[] result = new byte[buffer.remaining()];
        buffer.getBuffer().get(result);
        return result;
    }
    
    public static void write(final ExpandingByteBuffer buffer, SerialisableObject serialisableObject, SofConfiguration resolver) throws SofException {
        WriterAbstraction writer = new ExpandingByteBufferWriterAbstraction(buffer);
        SofSerialiser.write(writer, serialisableObject, resolver);
    }

    @SuppressWarnings("unchecked") public static <T extends SerialisableObject> T read(final ByteBuffer buffer, SofConfiguration resolver)
                    throws SofException, EOFException {
        ReaderAbstraction reader = new ByteBufferReaderAbstraction(buffer);
        return SofSerialiser.read(reader, resolver);
    }

    

}
