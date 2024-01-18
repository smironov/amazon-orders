package ca.mironov.amazon.util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Multimap<K, V> {

    private final Map<K, List<V>> map;

    public Multimap() {
        map = new LinkedHashMap<>();
    }

    public List<V> get(K key) {
        return Optional.ofNullable(map.get(key)).orElse(List.of());
    }

    public void put(K key, V value) {
        map.computeIfAbsent(key, newKey -> new LinkedList<>())
                .add(value);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public V getOnlyElement(K key) {
        List<V> values = get(key);
        if (values.size() == 1)
            return values.getFirst();
        else
            throw new IllegalArgumentException("Expected 1 element, got " + values.size() + ": " + values);
    }

    public Optional<V> findOnlyElement(K key) {
        List<V> values = get(key);
        return (values.size() == 1) ? Optional.of(values.getFirst()) : Optional.empty();
    }

}
