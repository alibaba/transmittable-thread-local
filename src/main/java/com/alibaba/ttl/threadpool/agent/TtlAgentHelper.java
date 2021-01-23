package com.alibaba.ttl.threadpool.agent;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.*;

/**
 * @author Jerry Lee (oldratlee at gmail dot com)
 * @since 2.13.0
 */
final class TtlAgentHelper {
    static boolean isBooleanOptionSet(@Nullable final Map<String, String> kvs, @NonNull String key, boolean defaultValue) {
        if (null == kvs) return defaultValue;

        final boolean containsKey = kvs.containsKey(key);
        if (!containsKey) return defaultValue;

        return !"false".equalsIgnoreCase(kvs.get(key));
    }

    @SuppressWarnings("unchecked")
    static List<String> getStringListOptionValue(@Nullable final Map<String, String> kvs, @NonNull String key) {
        if (null == kvs) return Collections.EMPTY_LIST;

        final String value = kvs.get(key);
        return splitListStringToStringList(value);
    }

    /**
     * Split {@code json} like String({@code "k1:v1,k2:v2"}) to KV map({@code "k1"->"v1", "k2"->"v2"}).
     */
    @NonNull
    static Map<String, String> splitCommaColonStringToKV(@Nullable final String commaColonString) {
        final Map<String, String> ret = new HashMap<String, String>();
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
    static List<String> splitListStringToStringList(@Nullable String listString) {
        final List<String> ret = new ArrayList<String>();
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
