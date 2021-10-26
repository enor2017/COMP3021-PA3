package hk.ust.cse.comp3021.pa3.util;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.TestAbortedException;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public static int repeatTest(ThrowingSupplier<Boolean> runner, int repeat, int timeout) throws TestAbortedException {
        return repeatTest(runner, repeat, timeout, false);
    }

    public static int repeatTest(ThrowingSupplier<Boolean> runner, int repeat, int timeout, boolean parallel) throws TestAbortedException {
        var pool = Executors.newFixedThreadPool(parallel ? repeat : 1);
        var abortException = new AtomicReference<TestAbortedException>(null);
        var trueCount = (int) Arrays.stream(new Boolean[repeat])
                .map(it -> pool.submit(() -> {
                    try {
                        return runner.get();
                    } catch (Throwable e) {
                        if (e instanceof TestAbortedException abortedException) {
                            abortException.set(abortedException);
                        }
                        return false;
                    }
                }))
                .map(fu -> {
                    try {
                        return fu.get(timeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException | TimeoutException | ExecutionException ignored) {
                        return false;
                    }
                })
                .filter(r -> r)
                .count();
        if (abortException.get() != null) {
            throw abortException.get();
        }
        return trueCount;
    }

    public static void assertMostly(ThrowingSupplier<Boolean> runner, int repeat, int timeout) {
        assertMostly(runner, repeat, timeout, false);
    }

    public static void assertMostly(ThrowingSupplier<Boolean> runner, int repeat, int timeout, boolean parallel) {
        var c = repeatTest(runner, repeat, timeout, parallel);
        assertThat(c, Matchers.greaterThan(repeat / 2));
    }

    public static void assertAlways(ThrowingRunnable runner, int repeat, int timeout) {
        assertAlways(runner, repeat, timeout, false);
    }

    public static void assertAlways(ThrowingRunnable runner, int repeat, int timeout, boolean parallel) {
        var c = repeatTest(() -> {
            try {
                runner.run();
                return true;
            } catch (Throwable e) {
                if (e instanceof TestAbortedException) {
                    throw e;
                }
                return false;
            }
        }, repeat, timeout);
        assertEquals(repeat, c);
    }

    public static <T> T assumeDoesNotThrow(ThrowingSupplier<T> runner) {
        try {
            return runner.get();
        } catch (Throwable throwable) {
            throw new TestAbortedException(throwable.toString());
        }
    }
}
