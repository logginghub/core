package com.logginghub.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FileSearch {
    public static void main(String[] args) {
        String path = args[0];
        final String namePart = args[1];
        final String word = args[2];

        final ExecutorService pool = Executors.newFixedThreadPool(5);
        final List<Future<?>> futures = new ArrayList<Future<?>>();

        FileUtils.visitChildrenRecursively(new File(path), new FileFilter() {
            public boolean accept(File file) {
                return file.getName().contains(namePart);
            }
        }, new FileVisitor() {
            public void visitFile(final File file) {
                Future<?> future = pool.submit(new Runnable() {
                    public void run() {
                        // System.out.println("Scanning " + file.getAbsolutePath());
                        String read = FileUtils.read(file);
                        if (read.contains(word)) {
                            System.out.println(file.getAbsolutePath());
                        }
                    }
                });

                futures.add(future);
            }
        });

        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();
    }
}
