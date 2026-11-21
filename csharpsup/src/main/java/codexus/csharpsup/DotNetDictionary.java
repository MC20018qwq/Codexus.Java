package codexus.csharpsup;

import java.util.*;

/** Dictionary enumeration uses entry slots; a new key reuses the last removed slot. */
public final class DotNetDictionary<K, V> extends AbstractMap<K, V> {
    private final Map<K, Integer> indices = new HashMap<>();
    private final List<Map.Entry<K, V>> slots = new ArrayList<>();
    private final Deque<Integer> free = new ArrayDeque<>();

    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key);
        Integer index = indices.get(key);
        if (index != null) {
            return slots.get(index).setValue(value);
        }
        int next;
        if (free.isEmpty()) {
            next = slots.size();
            slots.add(new SimpleEntry<>(key, value));
        } else {
            next = free.pop();
            slots.set(next, new SimpleEntry<>(key, value));
        }
        indices.put(key, next);
        return null;
    }

    @Override
    public V get(Object key) {
        Integer index = indices.get(Objects.requireNonNull(key));
        return index == null ? null : slots.get(index).getValue();
    }

    @Override
    public boolean containsKey(Object key) {
        return indices.containsKey(Objects.requireNonNull(key));
    }

    @Override
    public V remove(Object key) {
        Integer index = indices.remove(Objects.requireNonNull(key));
        if (index == null) return null;
        V value = slots.get(index).getValue();
        slots.set(index, null);
        free.push(index);
        return value;
    }

    @Override
    public int size() {
        return indices.size();
    }

    @Override
    public void clear() {
        indices.clear();
        slots.clear();
        free.clear();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<>() {
            @Override public int size() { return indices.size(); }
            @Override public Iterator<Map.Entry<K, V>> iterator() {
                return slots.stream().filter(Objects::nonNull).iterator();
            }
        };
    }
}
