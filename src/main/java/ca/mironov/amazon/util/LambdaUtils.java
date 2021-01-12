package ca.mironov.amazon.util;

import java.util.concurrent.Callable;

@SuppressWarnings("UtilityClass")
public final class LambdaUtils {

    private LambdaUtils() {
    }

    public static <V> V rethrow(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("PublicInnerClass")
    public interface Runnable {

        @SuppressWarnings("ProhibitedExceptionDeclared")
        void run() throws Exception;

    }

    public static void rethrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(e);
        }
    }

}
