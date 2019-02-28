package darkyenuscommand.util;

import com.esotericsoftware.jsonbeans.ObjectMap;

/**
 * ObjectMap with String key generics baked in, for json serialization.
 */
public final class StringMap <V> extends ObjectMap<String, V> {
}
