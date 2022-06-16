package com.alibaba.ttl.threadpool.agent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
final class TtlAgentHelper {

    // ======== Option Getter Methods ========

    static boolean isBooleanOptionSet(
        @Nullable final Map<String, String> kvs, @NonNull String key,
        boolean defaultValueIfKeyAbsent
    ) {
        return isBooleanOptionSet(kvs, key, defaultValueIfKeyAbsent, true);
    }

    static boolean isBooleanOptionSet(
        @Nullable final Map<String, String> kvs, @NonNull String key,
        boolean defaultValueIfKeyAbsent, boolean defaultValueIfValueAbsent
    ) {
        final String value;

        final Properties properties = System.getProperties();
        if (properties.containsKey(key)) {
            value = properties.getProperty(key).trim();
        } else {
            if (kvs == null) return defaultValueIfKeyAbsent;

            final boolean containsKey = kvs.containsKey(key);
            if (!containsKey) return defaultValueIfKeyAbsent;

            value = kvs.get(key).trim();
        }

        // if value is blank
        if (value.isEmpty()) return defaultValueIfValueAbsent;

        return !"false".equalsIgnoreCase(value);
    }

    @NonNull
    static String getStringOptionValue(
        @Nullable final Map<String, String> kvs, @NonNull String key,
        @NonNull String defaultValue
    ) {
        final String value;

        final Properties properties = System.getProperties();
        if (properties.containsKey(key)) {
            value = properties.getProperty(key).trim();
        } else {
            if (kvs == null) return defaultValue;

            final boolean containsKey = kvs.containsKey(key);
            if (!containsKey) return defaultValue;

            value = kvs.get(key).trim();
        }

        // if value is blank
        if (value.isEmpty()) return defaultValue;

        return value;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    static List<String> getOptionStringListValues(@Nullable final Map<String, String> kvs, @NonNull String key) {
        final String value;

        final Properties properties = System.getProperties();
        if (properties.containsKey(key)) {
            value = properties.getProperty(key);
        } else {
            if (kvs == null) return Collections.EMPTY_LIST;

            value = kvs.get(key);
        }

        return splitListStringToStringList(value);
    }

    // ======== Simple Parse Util Methods ========

    /**
     * Split {@code json} like String({@code "k1:v1,k2:v2"}) to KV map({@code "k1"->"v1", "k2"->"v2"}).
     */
    @NonNull
    static Map<String, String> splitCommaColonStringToKV(@Nullable final String commaColonString) {
        final Map<String, String> ret = new HashMap<>();
        if (commaColonString == null || commaColonString.trim().length() == 0) return ret;

        final String[] splitKvArray = commaColonString.trim().split("\\s*,\\s*");
        for (String kvString : splitKvArray) {
            final String[] kv = kvString.trim().split("\\s*:\\s*");
            if (kv.length == 0) continue;

            if (kv.length == 1) ret.put(kv[0], "");
            else ret.put(kv[0], kv[1]);
        }

        return ret;
    }

    /**
     * Split String {@code "v1|v2|v3"} to String List({@code [v1, v2, v3]}).
     */
    @NonNull
    static List<String> splitListStringToStringList(@Nullable String listString) {
        final List<String> ret = new ArrayList<>();
        if (listString == null || listString.trim().length() == 0) return ret;

        final String[] split = listString.trim().split("\\s*\\|\\s*");
        for (String s : split) {
            if (s.length() == 0) continue;

            ret.add(s);
        }

        return ret;
    }


    private TtlAgentHelper() {
        throw new InstantiationError("Must not instantiate this class");
    }
}
