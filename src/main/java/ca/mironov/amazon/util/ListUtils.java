package ca.mironov.amazon.util;

import com.google.common.collect.Iterables;

import java.util.*;

@SuppressWarnings("UtilityClass")
public final class ListUtils {

    private ListUtils() {
    }

    public static <E> Optional<E> findOnlyElement(Iterable<E> collection) {
        return Optional.ofNullable(Iterables.getOnlyElement(collection, null));
    }

}
