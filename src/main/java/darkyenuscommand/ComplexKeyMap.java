package darkyenuscommand;

import com.esotericsoftware.jsonbeans.*;
import org.bukkit.Bukkit;

import java.util.logging.Level;

/**
 *
 */
public class ComplexKeyMap<K,V> extends ObjectMap<K, V> {

    private final Class<K> keyClass, valueClass;

    public ComplexKeyMap(Class<K> keyClass, Class<K> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public static final JsonSerializer<ComplexKeyMap<?,?>> SERIALIZER = new JsonSerializer<ComplexKeyMap<?, ?>>() {
        @Override
        public void write(Json json, ComplexKeyMap<?, ?> object, Class knownType) {
            json.writeObjectStart();
            json.writeValue("key", object.keyClass.getName());
            json.writeValue("value", object.valueClass.getName());
            json.writeArrayStart("entries");
            for (Entry<?, ?> entry : object.entries()) {
                json.writeArrayStart();
                json.writeValue(entry.key, object.keyClass);
                json.writeValue(entry.value, object.valueClass);
                json.writeArrayEnd();
            }
            json.writeArrayEnd();
            json.writeObjectEnd();
        }

        @SuppressWarnings("unchecked")
        @Override
        public ComplexKeyMap<?, ?> read(Json json, JsonValue jsonData, Class type) {
            try {
                final Class<?> keyClass = Class.forName(jsonData.getString("key"));
                final Class<?> valueClass = Class.forName(jsonData.getString("value"));
                final ComplexKeyMap result = new ComplexKeyMap(keyClass, valueClass);
                final JsonValue entries = jsonData.get("entries");
                for (JsonValue entry : entries) {
                    final JsonValue keyJson = entry.get(0);
                    final JsonValue valueJson = entry.get(1);
                    final Object key = json.readValue(keyClass, keyJson);
                    final Object value = json.readValue(valueClass, valueJson);
                    result.put(key, value);
                }
                return result;
            } catch (ClassNotFoundException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to read ComplexMap's kay/value classes", e);
                return null;
            }
        }
    };
}
