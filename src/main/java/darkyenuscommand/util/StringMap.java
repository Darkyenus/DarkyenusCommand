package darkyenuscommand.util;

import com.esotericsoftware.jsonbeans.ObjectMap;

/**
 * ObjectMap with String key generics baked in, for json serialization.
 */
public class StringMap <V> extends ObjectMap<String, V> {
}
