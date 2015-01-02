package com.logginghub.utils.sof;

import com.logginghub.utils.Destination;

import java.io.*;

public class SofStreamSerialiser {

    public static byte[] write(SerialisableObject serialisableObject, SofConfiguration configuration) throws
                                                                                                      SofException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos, serialisableObject, configuration);
        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked") public static <T> T read(byte[] data, SofConfiguration configuration) throws
                                                                                                         SofException,
                                                                                                         EOFException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        StreamReaderAbstraction reader = new StreamReaderAbstraction(bais, data.length);
        return (T) SofSerialiser.read(reader, configuration);
    }

    public static void write(OutputStream stream,
                             SerialisableObject serialisableObject,
                             SofConfiguration configuration) throws SofException {
        WriterAbstraction writer = createWriterAbstraction(stream);
        SofSerialiser.write(writer, serialisableObject, configuration);
    }

    public static WriterAbstraction createWriterAbstraction(OutputStream stream) {
        StreamWriterAbstraction streamWriterAbstraction = new StreamWriterAbstraction(stream);
        return streamWriterAbstraction;
    }

    public static void visit(InputStream bis, long length, SofConfiguration sofConfig, Destination<SerialisableObject> destination) throws
                                                                                                                                   EOFException,
                                                                                                                                   SofException {
        StreamReaderAbstraction reader = new StreamReaderAbstraction(bis, length);
        SofSerialiser.readAll(reader, sofConfig, destination);
    }
}
