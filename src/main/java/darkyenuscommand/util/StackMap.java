package darkyenuscommand.util;

import com.esotericsoftware.jsonbeans.ObjectMap;

import java.util.ArrayList;

/**
 *
 */
public final class StackMap <K, V> extends ObjectMap<K, ArrayList<V>> {

    private final int limit;

    public StackMap(int limit) {
        this.limit = limit;
    }

    @Override
    public ArrayList<V> get(K key) {
        final ArrayList<V> result = super.get(key);
        if (result != null) {
            return result;
        } else {
            final ArrayList<V> newResult = new ArrayList<>();
            put(key, newResult);
            return newResult;
        }
    }

    public void push(K key, V value) {
        final ArrayList<V> list = get(key);
        list.add(value);
        while (list.size() > limit) {
            list.remove(0);
        }
    }

    public V popOrNull(K key) {
        return pop(key, null);
    }

    public V pop(K key, V defaultValue) {
        final ArrayList<V> list = get(key, null);
        if (list == null || list.isEmpty()) {
            return defaultValue;
        } else {
            return list.remove(list.size() - 1);
        }
    }
}
