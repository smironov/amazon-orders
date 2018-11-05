package ca.mironov.amazon;

import java.util.*;

public class ListUtils {

    public static <E> E getSingle(Collection<E> collection) {
        Iterator<E> iterator = collection.iterator();
        if (iterator.hasNext()) {
            E value = iterator.next();
            if (iterator.hasNext()) {
                throw new IllegalArgumentException("too many items: " + collection.size() + ", " + collection);
            }
            return value;
        } else {
            throw new IllegalArgumentException("items not found");
        }
    }

}
