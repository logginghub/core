package com.logginghub.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    private static ExceptionHandler exceptionHandler = new SystemErrExceptionHandler();

    public static void setExceptionHandler(ExceptionHandler exceptionHandler) {
        FileUtils.exceptionHandler = exceptionHandler;
    }

    public static class TimeComparator implements Comparator<File> {
        public int compare(File o1, File o2) {
            return CompareUtils.compare(o1.lastModified(), o2.lastModified());
        }
    }

    private static Random s_random = new Random();

    public static FileFilter justFilesFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isFile();
        }
    };

    public static FileFilter justDirectoriesFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    /**
     * Return a list of child files that pass the filter provided
     * 
     * @param root
     * @param filter
     * @return
     */
    public static List<File> getChildenRecursively(File root, FileFilter filter) {
        List<File> list = new ArrayList<File>();
        populateChildenRecursively(root, filter, list);
        return list;
    }

    public static List<File> getChildenRecursively(File root) {
        List<File> list = new ArrayList<File>();
        populateChildenRecursively(root, null, list);
        return list;
    }

    public static void populateChildenRecursively(File root, FileFilter filter, List<File> files) {
        File[] listFiles = root.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {

                if (file.isDirectory()) {
                    populateChildenRecursively(file, filter, files);
                }

                if (filter == null || filter.accept(file)) {
                    files.add(file);
                }
            }
        }
    }

    public static List<File> listFiles(File folder) {
        List<File> files = new ArrayList<File>();
        File[] listFiles = folder.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                files.add(file);
            }
        }
        return files;
    }

    public static List<File> listFiles(File folder, FileFilter filter) {
        List<File> files = new ArrayList<File>();
        File[] listFiles = folder.listFiles(filter);
        if (listFiles != null) {
            for (File file : listFiles) {
                files.add(file);
            }
        }
        return files;
    }

    public static List<File> listFilesRecursive(File path) {
        final List<File> files = new ArrayList<File>();
        visitChildrenRecursively(path, new FileFilter() {
            public boolean accept(File pathname) {
                return true;
            }
        }, new FileVisitor() {
            public void visitFile(File file) {
                files.add(file);
            }
        });
        return files;
    }

    public static List<File> listFilesRecursive(File path, FileFilter filter) {
        return findFilesRecursively(path, filter);
    }

    public static List<File> findFilesRecursively(File path, FileFilter filter) {
        final List<File> files = new ArrayList<File>();
        visitChildrenRecursively(path, filter, new FileVisitor() {
            public void visitFile(File file) {
                files.add(file);
            }
        });
        return files;
    }

    public static void visitChildrenRecursively(File path, FileFilter filter, FileVisitor fileVisitor) {
        File[] listFiles = path.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    visitChildrenRecursively(file, filter, fileVisitor);
                }

                if (filter.accept(file)) {
                    fileVisitor.visitFile(file);
                }
            }
        }
    }

    /**
     * Another variant of recursive directory crawling; this one allows you to specify another file
     * filter to prevent recursion into folders you dont want to crawl
     * 
     * @param path
     * @param fileFilter
     * @param folderFilter
     * @param fileVisitor
     */
    public static void visitChildrenRecursively(File path, FileFilter fileFilter, FileFilter folderFilter, FileVisitor fileVisitor) {
        File[] listFiles = path.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    if (folderFilter.accept(path)) {
                        visitChildrenRecursively(file, fileFilter, folderFilter, fileVisitor);
                    }
                }
                else if (fileFilter.accept(file)) {
                    fileVisitor.visitFile(file);
                }
            }
        }
    }

    /**
     * Strips off the part of the path before the start of the root file. If the root is c:\temp\foo
     * and the file is c:\temp\foo\folder\file.txt this will return folder\
     * 
     * @param file
     * @param root
     * @return
     */
    public static String getRelativePath(File file, File root) {
        int rootLength = root.getAbsolutePath().length();
        String absolutePath = file.getAbsolutePath();
        String relativePath = absolutePath.substring(rootLength + 1);
        return relativePath;
    }

    /**
     * Strips off the part of the path before the start of the root file. If the root is c:\temp\foo
     * and the file is c:\temp\foo\folder\file.txt this will return folder\file.txt
     * 
     * @param file
     * @param root
     * @return
     */
    public static String getRelativeName(File file, File root) {
        int length = root.getAbsolutePath().length();
        String absolutePath = file.getAbsolutePath();
        String relativePath;

        if (absolutePath.length() == length) {
            if (file.getAbsolutePath().equals(root.getAbsolutePath())) {
                // They are the same file
                relativePath = "";
            }
            else {
                throw new RuntimeException(String.format("Something looks wrong with the parameters : file [%s] and root [%s]",
                                                         file.getAbsolutePath(),
                                                         root.getAbsolutePath()));
            }
        }
        else {
            relativePath = absolutePath.substring(length + 1, absolutePath.length());
        }
        return relativePath;
    }

    public final static byte[] getFileAsBytes(final File file) {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            final byte[] bytes = new byte[(int) file.length()];
            bis.read(bytes);
            return bytes;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read file '%s' contents as a byte array", file.getAbsolutePath()), e);
        }
        finally {
            closeQuietly(bis);
        }
    }

    public static String read(String name) {
        File file = new File(name);
        return read(file);
    }

    public static String read(File file) {
        return new String(getFileAsBytes(file));
    }

    public static String read(InputStream openStream) {
        return new String(readFully(openStream));
    }

    public static void write(String xml, File file) {
        ensurePathExists(file);

        FileWriter fw = null;

        try {
            fw = new FileWriter(file);
            fw.write(xml);
        }
        catch (IOException ioe) {
            throw new RuntimeException(String.format("Failed to write to file '%s'", file.getAbsoluteFile()), ioe);
        }
        finally {
            if (fw != null) {
                try {
                    fw.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(String.format("Failed to close writer when writing to file '%s'", file.getAbsoluteFile()), e);
                }
            }
        }
    }

    public static void touch(File file) throws IOException {
        ensurePathExists(file);
        FileOutputStream fos = new FileOutputStream(file);
        closeQuietly(fos);
    }

    public static void close(RandomAccessFile randomAccessFile) {
        if (randomAccessFile != null) {
            try {
                randomAccessFile.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void closeQuietly(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            }
            catch (IOException e) {}
        }
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (IOException e) {}
        }
    }

    public static void closeQuietly(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            }
            catch (IOException e) {}
        }
    }

    public static void closeQuietly(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        }
        catch (IOException e) {}
    }

    private static void closeQuietly(ZipFile file) {
        try {
            if (file != null) {
                file.close();
            }
        }
        catch (IOException e) {}
    }

    public static void closeQuietly(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        }
        catch (IOException e) {}
    }

    public static byte[] generateMD5(File file, int limit) {
        byte[] md5sum = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[8192];

            int read = 0;
            int progress = 0;

            try {

                do {
                    int amountToRead = Math.min(buffer.length, limit - progress);
                    read = is.read(buffer, 0, amountToRead);
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                        progress += read;
                    }
                }
                while (progress < limit && read > 0);
                md5sum = digest.digest();
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to process file for MD5", e);
            }
            finally {
                try {
                    is.close();
                }
                catch (IOException e) {
                    throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
                }
            }
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return md5sum;
    }

    public static byte[] generateMD5(File file) {
        byte[] md5sum = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int read = 0;
            try {
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
                md5sum = digest.digest();
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to process file for MD5", e);
            }
            finally {
                try {
                    is.close();
                }
                catch (IOException e) {
                    throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
                }
            }
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return md5sum;
    }

    public static void append(byte[] data, File destination) throws IOException {
        FileUtils.ensurePathExists(destination);
        FileOutputStream fos = new FileOutputStream(destination, true);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(data);
        bos.close();
    }

    public static void write(byte[] data, File destination) throws IOException {
        FileUtils.ensurePathExists(destination);

        FileOutputStream fos = new FileOutputStream(destination);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(data);
        bos.close();
    }

    public static void write(InputStream openStream, File destination) throws IOException {
        write(openStream, destination, null);
    }

    public static void write(InputStream openStream, File destination, ProgressListener listener) throws IOException {
        FileUtils.ensurePathExists(destination);
        InputStream is = openStream;

        FileOutputStream fos = new FileOutputStream(destination);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        copy(is, bos, listener);
        bos.close();
    }

    public static void copy(InputStream in, File out) {
        try {
            write(in, out, null);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy between streams"), e);
        }
    }

    public static void copy(InputStream in, OutputStream out) {
        try {
            copy(in, out, null);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to copy between streams"), e);
        }
    }

    public static void copy(InputStream intput, OutputStream output, ProgressListener listener) throws IOException {
        byte[] data = new byte[40960];
        int read = 0;
        while (read != -1) {
            read = intput.read(data);
            if (read > 0) {
                output.write(data, 0, read);
            }

            if (listener != null) {
                listener.onProgress(read);
            }
        }
    }

    /**
     * Ensure this files' parent folder exists, so when you try and write to it you wont get any
     * nasty surprises
     * 
     * @param destination
     */
    public static void ensurePathExists(File destination) {
        File parentFile = destination.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
    }

    /**
     * Delete all files (recursively) from this folder, but leave the folder itself alone
     * 
     * @param root
     */
    public static void deleteContents(File folder) {
        if (folder.exists()) {
            // Remove all the files
            deleteFilesRecursively(folder);

            // Now remove all the empty folders
            deleteEmptyFoldersRecursively(folder);
        }
    }

    /**
     * Recursively delete all files, but leave the folder structure intact. This is quite handy if
     * you want to leave an explorer window looking at the files...
     * 
     * @param folder
     */
    public static void deleteContentsButLeaveFolders(File folder) {
        if (folder.exists()) {
            // Remove all the files
            deleteFilesRecursively(folder);
        }
    }

    /**
     * Delete all files (recursively) from this folder, and then delete the folder itself
     * 
     * @param folder
     */
    public static void deleteFolderAndContents(File folder) {
        if (folder.exists()) {
            // Delete the contents
            deleteContents(folder);

            // And finally the folder itself
            deleteLoudly(folder);
        }
    }

    private static void deleteEmptyFoldersRecursively(File folder) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        List<File> childFolders = FileUtils.getChildenRecursively(folder, filter);

        for (File file : childFolders) {
            deleteLoudly(file);
        }
    }

    private static void deleteFilesRecursively(File folder) {
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.isFile();
            }
        };

        List<File> childFiles = FileUtils.getChildenRecursively(folder, filter);

        for (File file : childFiles) {
            deleteLoudly(file);
        }
    }

    public static void deleteLoudly(File file) {
        if (file.isDirectory()) {
            deleteContents(file);
        }

        if (!file.delete()) {
            // TODO : this should be an io exception...
            throw new RuntimeException(String.format("Failed to delete file '%s'", file.getAbsolutePath()));
        }
    }

    public static void zipTo(File file, File target) {

        BufferedInputStream source = null;
        ZipOutputStream out = null;
        try {
            FileOutputStream dest = new FileOutputStream(target);
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            out.setMethod(ZipOutputStream.DEFLATED);
            byte data[] = new byte[4096];

            FileInputStream fi = new FileInputStream(file);
            source = new BufferedInputStream(fi, 4096);
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = source.read(data, 0, 4096)) != -1) {
                out.write(data, 0, count);
            }

        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to compress file '%s' to archive '%s'", file.getAbsolutePath(), target.getAbsolutePath()),
                                       e);
        }
        finally {
            closeQuietly(source);
            closeQuietly(out);
        }
    }

    public static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();

        boolean renameOk = zipFile.renameTo(tempFile);
        if (!renameOk) {
            throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean notInFiles = true;
            for (File f : files) {
                if (f.getName().equals(name)) {
                    notInFiles = false;
                    break;
                }
            }
            if (notInFiles) {
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        // Close the streams
        zin.close();
        // Compress the files
        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i]);
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(files[i].getName()));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
            in.close();
        }
        // Complete the ZIP file
        out.close();
        tempFile.delete();
    }

    public static List<String> readLines(String string) {
        File file = new File(string);
        return readLines(file);
    }

    public static List<String> readLines(File file) {
        List<String> lines = new ArrayList<String>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read lines from file '%s'", file.getAbsolutePath()), e);
        }

        return lines;

    }

    public static byte[] readFully(File file) throws IOException {
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        try {
            return readFully(inputStream);
        }
        finally {
            closeQuietly(inputStream);
        }
    }

    public static byte[] readAsBytes(File file) throws IOException {
        return readFully(file);
    }

    public static byte[] readAsBytes(String filename) throws IOException {
        return readFully(new File(filename));
    }

    public static byte[] readAsBytes(InputStream openStream) {
        return readFully(openStream);
    }

    public static byte[] readFully(InputStream inputStream) {

        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[100 * 1024];
            int read = 0;
            while ((read = inputStream.read(buffer)) > 0) {
                bais.write(buffer, 0, read);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Faield to read input stream"), e);
        }

        return bais.toByteArray();
    }

    public static int readFully(byte[] buffer, InputStream inputStream) {

        try {
            int progress = 0;
            boolean done = false;
            while (!done) {
                int read = inputStream.read(buffer, progress, buffer.length - progress);

                if (read == -1) {
                    done = true;
                    if (progress == 0) {
                        progress = -1;
                    }
                }
                else {
                    progress += read;
                    if (progress == buffer.length) {
                        done = true;
                    }
                }
            }

            return progress;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Faield to read input stream"), e);
        }

    }

    public static void writeRandom(File file, int size) throws IOException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        for (int i = 0; i < size; i++) {
            os.write(s_random.nextInt());
        }
        os.close();
    }

    public static boolean contentsEquals(InputStream a, File b) throws IOException {
        BufferedInputStream bStream = new BufferedInputStream(new FileInputStream(b));

        try {
            return contentsEquals(a, bStream);
        }
        finally {
            FileUtils.closeQuietly(bStream);
        }
    }

    public static boolean contentsEquals(File a, File b) throws IOException {
        BufferedInputStream aStream = new BufferedInputStream(new FileInputStream(a));
        BufferedInputStream bStream = new BufferedInputStream(new FileInputStream(b));

        try {
            return contentsEquals(aStream, bStream);
        }
        catch (IOException e) {
            throw e;
        }
        finally {
            FileUtils.closeQuietly(aStream);
            FileUtils.closeQuietly(bStream);
        }
    }

    public static boolean contentsEquals(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return (ch2 == -1);
    }

    public static File createRandomFolder(String string) {
        File randomFolder = getRandomFolder(string);
        randomFolder.mkdirs();
        return randomFolder;
    }

    public static File getRandomFolder(String string) {
        int index = 0;
        File folder = new File(string + index);
        while (folder.exists()) {
            index++;
            folder = new File(string + index);
        }

        return folder;
    }

    public static File getRandomFile(String foldername, String prefix, String postfix) {
        File folder = new File(foldername);
        folder.mkdirs();
        File file = new File(folder, StringUtils.format("{}{}{}", prefix, StringUtils.randomString(5), postfix));
        while (file.exists()) {
            file = new File(folder, StringUtils.format("{}{}{}", prefix, StringUtils.randomString(5), postfix));
        }

        return file;
    }

    public static File getUniqueFile(File folder, String prefix, String postfix) {
        return getUniqueFile(folder, prefix, postfix, "", 0);
    }

    public static File getUniqueFile(File folder, String prefix, String postfix, String fileNumberPrefix, int startAt) {
        folder.mkdirs();

        File file;
        int counter = startAt;
        do {
            file = new File(folder, StringUtils.format("{}{}{}{}", prefix, fileNumberPrefix, counter, postfix));
            counter++;
        }
        while (file.exists());

        return file;
    }

    public static File createAndFillRandomFile(String string, int length) throws IOException {
        File file = new File(string);
        if (file.getParentFile() != null) file.getParentFile().mkdirs();
        FileUtils.writeRandom(file, length);
        return file;
    }

    public static void write(InputStream input, OutputStream output) throws IOException {
        // TOOD : thread local cache this
        byte[] data = new byte[40960];
        int read = 0;
        while (read != -1) {
            read = input.read(data);
            if (read > 0) {
                output.write(data, 0, read);
            }
        }
    }

    public static boolean assertEquals(String reason, InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }

        int index = 0;

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();

            if (ch != ch2) {
                throw new AssertionError(reason + " : streams differ at index " + index);
            }
            ch = input1.read();
            index++;
        }

        int ch2 = input2.read();
        return (ch2 == -1);
    }

    public static void visitChildrenRecursively(File currentPath, FileVisitor fileVisitor) {
        visitChildrenRecursively(currentPath, new FileFilter() {
            public boolean accept(File pathname) {
                return true;
            }
        }, fileVisitor);
    }

    public static void ensureDoesntExist(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                deleteLoudly(file);
            }
            else {
                deleteFolderAndContents(file);
            }
        }
    }

    public static Object readObject(File indexFile) {
        ObjectInputStream ois = null;
        Object readObject;

        try {
            ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
            readObject = ois.readObject();
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to open file [%s] and deserialise an object from it", indexFile.getAbsolutePath()), e);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Failed to open file [%s] and deserialise an object from it", indexFile.getAbsolutePath()), e);
        }
        finally {
            closeQuietly(ois);
        }

        return readObject;
    }

    public static void writeLong(File file, long value) {
        DataOutputStream oos = null;
        try {
            oos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            oos.writeLong(value);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write long %d to file at [%s]", value, file.getAbsolutePath()), e);
        }
        finally {
            FileUtils.closeQuietly(oos);
        }
    }

    public static long readLong(File file) {
        DataInputStream oos = null;
        try {
            oos = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            long value = oos.readLong();
            return value;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read long from file at [%s]", file.getAbsolutePath()), e);
        }
        finally {
            FileUtils.closeQuietly(oos);
        }
    }

    public static void writeObject(File indexFile, Object object) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
            oos.writeObject(object);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write index file at [%s]", indexFile.getAbsolutePath()), e);
        }
        finally {
            FileUtils.closeQuietly(oos);
        }
    }

    public static void grep(File file, String contents, boolean caseSensitive, SearchResultsListener searchResultsListener) {
        int lineNumber = 0;
        List<String> readLines = readLines(file);
        WildcardMatcher matcher = new WildcardMatcher(contents);
        matcher.setCaseSensitive(caseSensitive);
        for (String string : readLines) {
            lineNumber++;
            if (matcher.matches(string)) {
                searchResultsListener.onNewResult(file.getAbsolutePath(), lineNumber, string);
            }
        }
    }

    /**
     * Opens a stream to this resource, first checking to see if it exists a file before attempting
     * to find it as a classpath resource. Returns null if neither of those works.
     * 
     * @param propertyPath
     * @return
     */
    public static InputStream openStream(String propertyPath) {
        InputStream is;
        File file = new File(propertyPath);
        if (file.exists()) {
            try {
                is = new FileInputStream(file);
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException("I think file " +
                                           file.getAbsolutePath() +
                                           " has just beed deleted, it was there when we checked but not when we tried to open a stream");
            }
        }
        else {
            is = FileUtils.class.getResourceAsStream(propertyPath);
        }

        return is;
    }

    public static InputStream openStream(File file) {
        InputStream is = null;
        if (file.exists()) {
            try {
                is = new BufferedInputStream(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException("I think file " +
                                           file.getAbsolutePath() +
                                           " has just beed deleted, it was there when we checked but not when we tried to open a stream");
            }
        }

        return is;
    }

    public static void recursiveCopy(File src, File dest, FileFilter fileFilter) {
        recursiveCopyInternal(src, src, dest, fileFilter);
    }

    private static void recursiveCopyInternal(File sourceTarget, File src, File dest, FileFilter fileFilter) {
        File[] listFiles = sourceTarget.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    recursiveCopyInternal(file, src, dest, fileFilter);
                }
                else {
                    if (fileFilter.accept(file)) {
                        File destinationFile = new File(dest, getRelativeName(file, src));
                        copy(file, destinationFile);
                    }
                }
            }
        }
    }

    public static void append(File sourceFile, File destFile) {
        copy(sourceFile, destFile, true);
    }

    public static void copy(File sourceFile, File destFile) {
        copy(sourceFile, destFile, false);
    }

    public static void copy(File sourceFile, File destFile, boolean append) {
        try {
            if (!destFile.exists()) {
                if (destFile.getParentFile() != null) {
                    destFile.getParentFile().mkdirs();
                }
                destFile.createNewFile();
            }
            else {
                if (destFile.isDirectory()) {
                    destFile = new File(destFile, sourceFile.getName());
                }
            }

            FileChannel source = null;
            FileChannel destination = null;
            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile, append).getChannel();
                destination.transferFrom(source, 0, source.size());
            }
            finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File writeTemp(String pre, String csvFile) throws IOException {
        File createTempFile = File.createTempFile(pre, ".tmp");
        write(csvFile, createTempFile);
        return createTempFile;
    }

    public static String readUrl(String string) {
        try {
            return read(new URL(string).openStream());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Failed to read url %s", string), e);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read url %s", string), e);
        }
    }

    public static void moveToDirectory(File file, File newFolder) {
        File newFile = new File(newFolder, file.getName());
        boolean success = file.renameTo(newFile);
        if (!success) {
            copy(file, newFile);
            boolean delete = file.delete();
            if (!delete) {
                throw new RuntimeException(String.format("Failed to delete file after copy, you might have two copies now. Original path is %s, destination file is %s",
                                                         file.getAbsolutePath(),
                                                         newFile.getAbsolutePath()));
            }
        }
    }

    public static String getNameWithoutExtension(File file) {

        String filename = file.getName();

        int extensionIndex = filename.lastIndexOf(".");
        String name;
        if (extensionIndex == -1) {
            name = filename;
        }
        else {
            name = filename.substring(0, extensionIndex);
        }

        return name;
    }

    public static String getFileExtension(File file) {

        String filename = file.getName();
        int extensionIndex = filename.lastIndexOf(".");
        String extension;
        if (extensionIndex == -1) {
            extension = "";
        }
        else {
            extension = filename.substring(extensionIndex + 1, filename.length());
        }

        return extension;
    }

    public static void closeQuietly(List<Closeable> closables) {
        for (Closeable closeable : closables) {
            if (closeable != null) {
                try {
                    closeable.close();
                }
                catch (IOException e) {

                }
            }
        }
    }

    public static void closeQuietly(Closeable... closables) {
        for (Closeable closeable : closables) {
            if (closeable != null) {
                try {
                    closeable.close();
                }
                catch (IOException e) {

                }
            }
        }
    }

    public static String readFromZipAsString(File zipFile, String filename) {
        return new String(readFromZip(zipFile, filename));

    }

    public static byte[] readFromZip(File zipFile, String filename) {
        ZipFile file = null;
        InputStream inputStream = null;
        try {
            file = new ZipFile(zipFile);
            ZipEntry entry = file.getEntry(filename);
            if (entry == null) {
                throw new RuntimeException(String.format("Entry '%s' was not found in this zip file '%s'", filename, zipFile.getAbsolutePath()));
            }
            inputStream = file.getInputStream(entry);
            byte[] readFully = readFully(inputStream);
            return readFully;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read entry '%s' from zip file '%s'", filename, zipFile.getAbsolutePath()), e);
        }
        finally {
            closeQuietly(inputStream);
            closeQuietly(file);
        }

    }

    public static String readAsString(File file) {
        return read(file);
    }

    public static String readAsString(InputStream is) {
        return read(is);
    }

    public static String[] readZipAsStringArray(File file, String internalFilename) {
        String readFromZipAsString = FileUtils.readFromZipAsString(file, internalFilename);
        return StringUtils.splitIntoLines(readFromZipAsString);
    }

    public static String[] readAsStringArray(String filename) {

        InputStream openStream = ResourceUtils.openStream(filename);
        try {
            return readAsStringArray(openStream);
        }
        finally {
            closeQuietly(openStream);
        }
    }

    public static String[] readAsStringArray(File file) {
        return StringUtils.splitIntoLines(readAsString(file));
    }

    public static String[] readAsStringArray(InputStream is) {
        return StringUtils.splitIntoLines(readAsString(is));
    }

    public static String[] readAsStringArray(File file, int linesToRead) {
        try {
            return readAsStringArray(new FileInputStream(file), linesToRead);
        }
        catch (FileNotFoundException e) {
            throw new FormattedRuntimeException(e, "Failed to read lines from file '{}'", file.getAbsolutePath());
        }
    }

    public static String[] readAsStringArray(InputStream is, int linesToRead) {

        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        int count = 0;
        try {
            while ((line = reader.readLine()) != null && count < linesToRead) {
                lines.add(line);
                count++;
            }
        }
        catch (IOException e) {
            throw new FormattedRuntimeException(e, "Failed to read lines from input stream");
        }

        return StringUtils.toArray(lines);
    }

    public static void writeAsStringList(File file, List<String> history) {
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter writer = new FileWriter(file);
            bufferedWriter = new BufferedWriter(writer);

            for (String string : history) {
                bufferedWriter.write(string);
                bufferedWriter.newLine();
            }
        }
        catch (IOException e) {

        }
        finally {
            FileUtils.closeQuietly(bufferedWriter);
        }
    }

    public static List<String> readAsStringList(File file) {
        return StringUtils.splitIntoLineList(readAsString(file));
    }

    public static File downloadToFolder(String url, File folder) {
        return downloadToFolder(url, folder, null);
    }

    public static File downloadToFolder(String url, File folder, ProgressListener listener) {
        String name = StringUtils.afterLast(url, "/");
        File destinationFile = new File(folder, name);

        InputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new URL(url).openStream();
            output = createBufferedOutputStream(destinationFile);
            copy(input, output, listener);
            return destinationFile;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to download '%s' to file '%s'", url, destinationFile.getAbsolutePath()), e);
        }
        finally {
            closeQuietly(input);
            closeQuietly(output);
        }
    }

    public static String get(String url) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream input = null;
        try {
            input = new URL(url).openStream();
            copy(input, output, null);
            return new String(output.toByteArray());
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to download '%s'", url), e);
        }
        finally {
            closeQuietly(input);
            closeQuietly(output);
        }
    }

    public static BufferedOutputStream createBufferedOutputStream(File destinationFile) {
        try {
            return new BufferedOutputStream(new FileOutputStream(destinationFile));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("Failed to created output stream for file '%s' - this is impossible...",
                                                     destinationFile.getAbsolutePath()), e);
        }
    }

    static public void unzip(File file) {

        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            String newPath = FileUtils.getNameWithoutExtension(file);

            File basedir = new File(file.getParentFile(), newPath);
            basedir.mkdirs();
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

                File destFile = new File(basedir, entry.getName());
                destFile.getParentFile().mkdirs();

                if (!entry.isDirectory()) {
                    write(zip.getInputStream(entry), destFile);
                }
                else {
                    destFile.mkdir();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to unzip file '%s'", file.getAbsolutePath()), e);
        }
        finally {
            FileUtils.closeQuietly(zip);
        }
    }

    static public void unzipInSitu(File file) {

        ZipFile zip = null;
        try {
            zip = new ZipFile(file);

            File basedir = file.getParentFile();
            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

                File destFile = new File(basedir, entry.getName());
                destFile.getParentFile().mkdirs();

                if (!entry.isDirectory()) {
                    write(zip.getInputStream(entry), destFile);
                }
                else {
                    destFile.mkdir();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to unzip file '%s'", file.getAbsolutePath()), e);
        }
        finally {
            FileUtils.closeQuietly(zip);
        }
    }

    public static void searchAndReplace(File file, String searchString, String replaceString) {
        String contents = readAsString(file);
        String replaced = contents.replace(searchString, replaceString);
        write(replaced, file);
    }

    public static String detectEOLCharacters(File file) {

        StringBuilder eolCharacters = new StringBuilder();
        String readAsString = readAsString(file);
        boolean isEOL = false;
        for (int i = 0; i < readAsString.length(); i++) {
            char c = readAsString.charAt(i);

            if (c == '\r' || c == '\n') {
                isEOL = true;
                eolCharacters.append(c);
            }
            else {
                if (isEOL) {
                    break;
                }
            }
        }

        return eolCharacters.toString();
    }

    public static void move(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            copy(from, to);
            if (!from.delete()) {
                if (!to.delete()) {
                    throw new IOException("Unable to delete " + to);
                }
                throw new IOException("Unable to delete " + from);
            }
        }
    }

    public static void moveRuntime(File from, File to) {
        if (!from.renameTo(to)) {
            copy(from, to);
            if (!from.delete()) {
                if (!to.delete()) {
                    throw new RuntimeException("Unable to delete " + to);
                }
                throw new RuntimeException("Unable to delete " + from);
            }
        }
    }

    public static void extractFirstXLines(int lines, File in, File out) throws IOException {
        ensurePathExists(out);
        BufferedReader reader = new BufferedReader(new FileReader(in));
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));

        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null && lineNumber < lines) {
            writer.write(line);
            writer.newLine();
            lineNumber++;
        }

        reader.close();
        writer.close();
    }

    public static File getMostRecentFile(File... filesOrFolders) {
        final Pointer<File> mostRecent = new Pointer<File>(null);

        for (File fileOrFolder : filesOrFolders) {
            if (fileOrFolder.isDirectory()) {
                visitChildrenRecursively(fileOrFolder, new FileVisitor() {
                    public void visitFile(File file) {
                        if (mostRecent.value == null || file.lastModified() > mostRecent.value.lastModified()) {
                            mostRecent.value = file;
                        }
                    }
                });
            }
            else if (fileOrFolder.isFile()) {
                if (mostRecent.value == null || fileOrFolder.lastModified() > mostRecent.value.lastModified()) {
                    mostRecent.value = fileOrFolder;
                }
            }
        }

        return mostRecent.value;
    }

    public static File getMostRecentFile(File folder) {
        final Pointer<File> mostRecent = new Pointer<File>(null);
        visitChildrenRecursively(folder, new FileVisitor() {
            public void visitFile(File file) {
                if (mostRecent.value == null || file.lastModified() > mostRecent.value.lastModified()) {
                    mostRecent.value = file;
                }
            }
        });

        return mostRecent.value;
    }

    public static void write(byte[] data, OutputStream outputStream) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        write(byteArrayInputStream, outputStream);
    }

    public static Iterator<byte[]> getChunkIterator(File file, final int chunkSize) throws IOException {

        final FileInputStream fis = new FileInputStream(file);

        Iterator<byte[]> iterator = new Iterator<byte[]>() {

            private int read;
            private byte[] nextChunk = null;
            private boolean lastChunk = false;
            private boolean done = false;

            public void remove() {}

            public byte[] next() {
                if (nextChunk == null) {
                    readNext(chunkSize, fis);
                }

                byte[] chunk = nextChunk;
                nextChunk = null;

                if (lastChunk) {
                    done = true;
                }
                return chunk;
            }

            private void readNext(final int chunkSize, final FileInputStream fis) {
                nextChunk = new byte[chunkSize];
                read = FileUtils.readFully(nextChunk, fis);
                if (read > 0 && read < chunkSize) {
                    byte[] smallChunk = new byte[read];
                    System.arraycopy(nextChunk, 0, smallChunk, 0, read);
                    nextChunk = smallChunk;
                    lastChunk = true;
                }
                if (read == -1) {
                    done = true;
                    nextChunk = null;
                }
            }

            public boolean hasNext() {
                if (nextChunk == null) {
                    readNext(chunkSize, fis);
                }
                return !done;
            }
        };
        return iterator;
    }

    public static void sync(File source, File destination, FileFilter filter) {
        sync(source, destination, filter, FileSyncListener.noop);
    }

    public static boolean hasDifferences(File source, File destination, FilenameContainsIgnoreFileFilter filter, FileSyncListener listener) {
        boolean hasDifferences = false;

        List<File> sourceFiles = listFilesRecursive(source, filter);
        Map<String, File> relativeSources = convertFilesToMap(source, sourceFiles);

        List<File> destinationFiles = listFilesRecursive(destination, filter);
        Map<String, File> relativeDestinations = convertFilesToMap(destination, destinationFiles);

        // First loop against the source set
        Set<Entry<String, File>> entrySet = relativeSources.entrySet();
        for (Entry<String, File> entry : entrySet) {

            String path = entry.getKey();
            File sourceFile = entry.getValue();
            File destinationFile = relativeDestinations.remove(path);

            File destFile = new File(destination, path);
            if (destinationFile == null || !destinationFile.exists()) {
                if (sourceFile.isDirectory()) {
                    hasDifferences = true;
                }
                else {
                    hasDifferences = true;
                    listener.onFileCreated(sourceFile, destFile);
                }
            }
            else if (destinationFile.lastModified() < sourceFile.lastModified()) {
                if (destinationFile.isFile()) {
                    hasDifferences = true;

                    listener.onFileUpdated(sourceFile, destFile);
                }
            }
        }

        // Second loop on the remaining files in the destination - these need to
        // be deleted
        entrySet = relativeDestinations.entrySet();
        for (Entry<String, File> entry : entrySet) {
            File file = entry.getValue();
            hasDifferences = true;
            listener.onFileDeleted(file);
        }

        return hasDifferences;
    }

    public static void sync(File source, File destination, FileFilter filter, FileSyncListener listener) {

        List<File> sourceFiles = listFilesRecursive(source, filter);
        Map<String, File> relativeSources = convertFilesToMap(source, sourceFiles);

        List<File> destinationFiles = listFilesRecursive(destination, filter);
        Map<String, File> relativeDestinations = convertFilesToMap(destination, destinationFiles);

        // First loop against the source set
        Set<Entry<String, File>> entrySet = relativeSources.entrySet();
        for (Entry<String, File> entry : entrySet) {

            String path = entry.getKey();
            File sourceFile = entry.getValue();
            File destinationFile = relativeDestinations.remove(path);

            File destFile = new File(destination, path);
            if (destinationFile == null || !destinationFile.exists()) {
                if (sourceFile.isDirectory()) {
                    destFile.mkdirs();
                }
                else {
                    copy(sourceFile, destFile);
                    listener.onFileCreated(sourceFile, destFile);
                }
            }
            else if (destinationFile.lastModified() < sourceFile.lastModified()) {
                if (destinationFile.isFile()) {
                    copy(sourceFile, destFile);
                    listener.onFileUpdated(sourceFile, destFile);
                }
            }
        }

        // Second loop on the remaining files in the destination - these need to
        // be deleted
        entrySet = relativeDestinations.entrySet();
        for (Entry<String, File> entry : entrySet) {
            File file = entry.getValue();
            deleteLoudly(file);
            listener.onFileDeleted(file);
        }
    }

    public static Map<String, File> convertFilesToMap(File source, List<File> sourceFiles) {
        Map<String, File> relativeSources = new HashMap<String, File>();
        for (File file : sourceFiles) {
            String relative = getRelativeName(file, source);
            relativeSources.put(relative, file);
        }
        return relativeSources;
    }

    public static File createRandomTestFolderForClass(Class<?> class1) {
        return createRandomFolder("target/test/" + class1.getSimpleName());
    }

    public static File createRandomTestFileForClass(Class<?> class1) {
        File folder = createRandomFolder("target/test/" + class1.getSimpleName());
        File file = getUniqueFile(folder, "random.", ".file");
        return file;
    }

    public static Map<String, File> listFilesAsMap(File folder) {
        List<File> listFiles = listFiles(folder);
        Map<String, File> map = new HashMap<String, File>();
        for (File file : listFiles) {
            map.put(getRelativeName(file, folder), file);
        }
        return map;
    }

    public static String get(String format, Object... params) {
        return get(StringUtils.format(format, params));
    }

    public static File getHomeDirectory() {
        return new File(System.getProperty("user.home"));
    }

    public static long getFolderSize(File dataFolder) {
        return getFolderSize(dataFolder, null);
    }

    public static long getFolderSize(File dataFolder, final FileFilter filter) {
        final MutableLong value = new MutableLong();
        visitChildrenRecursively(dataFolder, new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        }, new FileVisitor() {
            public void visitFile(File file) {
                if (filter == null || filter.accept(file)) {
                    value.value += file.length();
                }
            }
        });

        return value.value;

    }

    public interface FileWatcherListener {
        void onFileChanged(File file);
    }

    public static Timer watchFile(final File file, final FileWatcherListener listener) {

        Timer timer = TimerUtils.everySecond("FileWatcher-" + file.getAbsolutePath(), new Runnable() {
            long time = -1;
            long size;

            public void run() {

                long newTime = file.lastModified();
                long newSize = file.length();

                if (time != -1) {
                    if (newTime != time || newSize != size) {
                        try {
                            listener.onFileChanged(file);
                        }
                        catch (Exception e) {
                            exceptionHandler.handleException("FileWatcherListener.onFileChanged", e);
                        }
                    }
                }

                time = file.lastModified();
                size = file.length();
            }
        });

        return timer;

    }

    public static FileUtilsWriter createWriter(File properties) {
        return new FileUtilsWriter(properties);
    }

    public static void closeQuietly(Statement closable) {
        if (closable != null) {
            try {
                closable.close();
            }
            catch (SQLException e) {}
        }
    }

    public static void closeQuietly(Connection closable) {
        if (closable != null) {
            try {
                closable.close();
            }
            catch (SQLException e) {}
        }
    }

    public static void appendLine(String key, File progressFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(progressFile, true));
            writer.append(key);
            writer.newLine();
            writer.close();
        }
        catch (IOException e) {
            throw new FormattedRuntimeException(e, "Failed to append line '{}' to file '{}'", key, progressFile.getAbsolutePath());
        }
    }

    public static List<File> resolveWildcards(String inputPathWithWildcards) {

        String base = StringUtils.before(inputPathWithWildcards, "*");
        String query = StringUtils.after(inputPathWithWildcards, base);

        // Convert the bits that will trip up the regex
        query = query.replace(".", "\\.");

        // Turn the wildcards into their regex equivalents
        query = query.replace("*", ".*");

        File baseFolder = new File(base);

        RegexFileFilter filter = new RegexFileFilter(query);

        List<File> listFiles = FileUtils.listFiles(baseFolder, filter);
        return listFiles;

    }

}
