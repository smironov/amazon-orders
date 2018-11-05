package ca.mironov.amazon;

import java.util.*;

public class ListUtils {

    public static <E> Collection<E> filterDuplicates(Collection<E> collection) {
        Collection<E> set = new LinkedHashSet<>(collection);
        collection.addAll(set);
        return set;
    }

    public static <E> E requireSingle(Collection<E> collection) {
        Optional<E> single = getSingle(collection);
        return single.orElseThrow(() -> new IllegalArgumentException("items not found"));
    }

    public static <E> Optional<E> getSingle(Collection<E> collection) {
        Iterator<E> iterator = collection.iterator();
        if (iterator.hasNext()) {
            E value = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalArgumentException("too many items: " + collection.size() + ", " + collection);
            }
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

}
