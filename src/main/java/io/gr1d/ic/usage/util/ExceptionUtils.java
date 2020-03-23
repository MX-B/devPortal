package io.gr1d.ic.usage.util;

public class ExceptionUtils {

    public static String toString(final Throwable throwable) {
        final StringBuilder str = new StringBuilder();
        Throwable current = throwable;

        while (current != null) {
            str.append(current.getClass().getName());
            str.append(": ");
            str.append(current.getLocalizedMessage());

            if (current.getCause() != null) {
                str.append(" -[caused by]-> ");
            }

            current = current.getCause();
        }

        return str.toString();
    }

}
