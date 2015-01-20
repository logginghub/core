package com.logginghub.testutils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.logginghub.utils.MutableBoolean;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;

public class CustomRunner extends BlockJUnit4ClassRunner {

    public CustomRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    protected void runChild(FrameworkMethod method, RunNotifier notifier) {

        System.out.println("Running : " + getTestClass().getName() + "." + method.getName());

        int threadsBefore = Thread.activeCount();
        Thread[] threadsBeforeArray = new Thread[threadsBefore];
        Thread.enumerate(threadsBeforeArray);

        final PrintStream err = System.err;
        final PrintStream out = System.out;

        final MutableBoolean outLogged = new MutableBoolean(false);
        final MutableBoolean errLogged = new MutableBoolean(false);

        System.setErr(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {
                errLogged.setValue(true);
                err.write(b);
            }
        }));

        System.setOut(new PrintStream(new OutputStream() {
            @Override public void write(int b) throws IOException {
                outLogged.setValue(true);
                out.write(b);
            }
        }));

        // System.out.println("RUN CHILD START " + method);
        super.runChild(method, notifier);
        // System.out.println("RUN CHILD END " + method);

        int threadsAfter = Thread.activeCount();
        Thread[] threadsAfterArray = new Thread[threadsAfter];
        Thread.enumerate(threadsAfterArray);

        Set<Thread> before = new HashSet<Thread>();
        for (Thread thread : threadsBeforeArray) {
            before.add(thread);
        }

        Set<Thread> after = new HashSet<Thread>();
        for (Thread thread : threadsAfterArray) {
            if (!thread.isDaemon()) {
                after.add(thread);
            }
        }

        if (threadsBefore != threadsAfter) {
            StringUtilsBuilder builder = new StringUtilsBuilder();
            builder.appendLine("Threads before {} threads after {}", threadsBefore, threadsAfter);

            boolean leaked = false;
            Iterator<Thread> iterator = after.iterator();
            while (iterator.hasNext()) {
                Thread thread = (Thread) iterator.next();
                if (!before.contains(thread)) {
                    builder.appendLine(thread.getName());
                    leaked = true;
                }
            }

            System.setErr(err);
            System.setOut(out);

            if (leaked) {
                Description describeChild = describeChild(method);
                Failure failure = new Failure(describeChild, new RuntimeException("Leaked threads : " + builder.toString()));
                notifier.fireTestFailure(failure);
            }
        }
        //
        // if(errLogged.value) {
        // throw new RuntimeException("Wrote output to system.err");
        // }
        //
        // if(outLogged.value) {
        // throw new RuntimeException("Wrote output to system.out");
        // }
    }

}
