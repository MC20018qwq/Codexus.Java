package codexus.base12111.plugin.utils.patch;

import java.util.*;
public final class IdBiMap<T> {
    private final List<T> values = new ArrayList<>();
    private final Map<T,Integer> ids = new IdentityHashMap<>();
    public int add(T value) { Objects.requireNonNull(value); Integer old = ids.get(value); if(old != null) return old; int id = values.size(); values.add(value); ids.put(value,id); return id; }
    public T getObject(int id) { return values.get(id); }
    public int getId(T value) { Integer id = ids.get(value); if(id == null) throw new NoSuchElementException("Unregistered serializer"); return id; }
}
