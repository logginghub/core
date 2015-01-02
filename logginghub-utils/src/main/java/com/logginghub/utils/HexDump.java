package com.logginghub.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.logginghub.utils.StringUtils.StringUtilsBuilder;

/**
 * Ripped off from somewhere on the internet - this is a bit shit and isn't at all thread safe, but
 * it gives a nice output
 * 
 * @author admin
 * 
 */
public class HexDump {
    private static final int BYTES_PER_LINE = 16;
    private static final int PADDING = 4;
    private static final int READ_BUFFER_SIZE = 1024;
    private static final int MAX_UNWRITTEN_LINE_COUNT = 100;

    // private static int runningCount = 0;
    // private static StringBuffer outBuffer = new StringBuffer();
    // private static int unwrittenLineCount = 0;

    // private static void reset()
    // {
    // runningCount = 0;
    // outBuffer = new StringBuffer();
    // unwrittenLineCount = 0;
    // }

    /**
     * returns a char corresponding to the hex digit of the supplied integer.
     */
    private static char charFromHexDigit(int digit) {
        char c;

        if ((digit >= 0) && (digit <= 9)) {
            c = (char) (digit + '0');
        }
        else {
            c = (char) (digit - 10 + 'a');
        }

        return c;
    }

    /**
     * @param inLine
     *            - a byte array containing the data to dump (only a line's worth beginning at
     *            offset is dumped)
     * @param offset
     *            - location in inLine at which to start
     * @param linelength
     *            - the actual number of characters in this line. This will equal BYTES_PER_LINE for
     *            all lines bar the last one, which may be shorter.
     */
    private static void printLine(byte[] inLine, int runningCount, int offset, int lineLength, StringBuffer outBuffer) {
        // print running location count
        for (int d = 0; d < 8; d++) {
            int digitValue = (int) (runningCount >> (4 * (7 - d))) % 16;
            outBuffer.append(charFromHexDigit((digitValue >= 0) ? digitValue : (16 + digitValue)));
        }
        outBuffer.append("  ");

        // print hexbytes
        for (int x = 0; x < lineLength; x++) {
            byte by = inLine[x + offset];
            int i = (by >= 0) ? by : (256 + by);

            outBuffer.append(charFromHexDigit(i / 16));
            outBuffer.append(charFromHexDigit(i % 16));
            outBuffer.append(' ');
        }

        // print padding between hexbytes and ascii
        for (int x = 0; x < PADDING + ((BYTES_PER_LINE - lineLength) * 3); x++) {
            outBuffer.append(' ');
        }

        // print ascii
        for (int x = 0; x < lineLength; x++) {
            char v = (char) inLine[x + offset];
            if ((v >= ' ') && (v < (char) 0x7f)) {
                outBuffer.append(v);
            }
            else {
                outBuffer.append('.');
            }
        }

        outBuffer.append('\n');
    }

    public static synchronized String format(InputStream in, int maxBytes) {
        try {
            int count;
            byte[] inBuf = new byte[READ_BUFFER_SIZE];

            StringBuffer outBuffer = new StringBuffer();

            int runningCount = 0;
            while ((count = in.read(inBuf)) > 0) {
                int bytesToWrite = count;
                int writePos = 0;

                while (bytesToWrite >= BYTES_PER_LINE) {
                    printLine(inBuf, runningCount, writePos, BYTES_PER_LINE, outBuffer);
                    writePos += BYTES_PER_LINE;
                    bytesToWrite -= BYTES_PER_LINE;
                    runningCount += BYTES_PER_LINE;
                }

                if (bytesToWrite > 0) {
                    printLine(inBuf, runningCount, writePos, bytesToWrite, outBuffer);
                }

                if (runningCount > maxBytes) {
                    break;
                }
            }

            return outBuffer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dump(File file, String toFile) throws IOException {
        StringUtilsBuilder sb = new StringUtilsBuilder();
        sb.appendLine("{} : {} bytes", file.getAbsolutePath(), file.length());
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        sb.append(format(bis, (int) file.length()));
        bis.close();
        FileUtils.write(sb.toString(), new File(toFile));
    }

    public static void dump(File file, long position, int length) throws IOException {
        Out.out("{} : {} bytes : position {} to {}", file.getAbsolutePath(), file.length(), position, position + length);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.skip(position);
        System.out.println(format(bis, length));
        bis.close();
    }

    public static void dump(File file) throws IOException {
        Out.out("{} : {} bytes", file.getAbsolutePath(), file.length());
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        System.out.println(format(bis, (int) file.length()));
        bis.close();
    }

    public static synchronized void dump(InputStream in, int maxBytes) {
        System.out.println(format(in, maxBytes));
    }
    
    public static synchronized void dump(StringUtilsBuilder builder, InputStream in, int maxBytes) {
        builder.appendLine(format(in, maxBytes));
    }

    public static synchronized final void main(String[] args) {
        if (args.length == 1) {
            try {
                FileInputStream inStream = new FileInputStream(args[0]);
                dump(inStream, Integer.MAX_VALUE);
                inStream.close();
            }
            catch (FileNotFoundException e) {
                System.out.println("File \"" + args[0] + "\" not found");
            }
            catch (IOException e) {
                System.out.println("error closing file");
            }
        }
        else {
            System.out.println("usage:\n  java Hexdump <filename>\n");
        }
    }

    public static synchronized void dump(byte[] bytes) {
        dump(new ByteArrayInputStream(bytes), Integer.MAX_VALUE);
    }
    
    public static synchronized void dump(StringUtilsBuilder builder, byte[] bytes) {
        dump(builder, new ByteArrayInputStream(bytes), Integer.MAX_VALUE);
    }

    public static synchronized void dump(byte[] bytes, int maxBytes) {
        dump(new ByteArrayInputStream(bytes), maxBytes);
    }

    public static synchronized void dump(ByteBuffer buffer) {
        dump(buffer, Integer.MAX_VALUE);
    }

    public static void dump(StringUtilsBuilder builder, ByteBuffer buffer, int start, int end) {

        int length = end - start;
        byte[] data = new byte[length];
      
        int position = buffer.position();
        buffer.position(start);
        buffer.get(data);
        buffer.position(position);
        
        dump(builder, data);
    }

    

    public static synchronized void dump(ByteBuffer buffer, int maxBytes) {
        int remaining = buffer.remaining();
        byte[] data = new byte[remaining];
        buffer.mark();
        buffer.get(data);
        buffer.reset();
        dump(data, remaining);
    }

    public static synchronized String format(byte[] bytes) {
        return format(bytes, Integer.MAX_VALUE);
    }

    public static synchronized String format(byte[] bytes, int maxBytes) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        String formatted = format(in, maxBytes);
        try {
            in.close();
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to close byte array input stream?!"), e);
        }
        return formatted;
    }

    public static synchronized String format(ByteBuffer buffer, int maxBytes) {
        int remaining = buffer.remaining();
        byte[] data = new byte[remaining];
        buffer.mark();
        buffer.get(data);
        buffer.reset();
        return format(data, remaining);
    }

    public static synchronized String format(ByteBuffer buffer) {
        return format(buffer, Integer.MAX_VALUE);
    }

    public static String dump(MappedByteBuffer buffer, int start, int end) {
        System.out.println(buffer);
        int length = end - start;
        length = Math.min(buffer.remaining(), length);
        byte[] data = new byte[length];
        int position = buffer.position();
        buffer.position(start);
        buffer.get(data);
        buffer.position(position);
        String formatted = format(data);
        System.out.println(formatted);
        return formatted;
    }

}