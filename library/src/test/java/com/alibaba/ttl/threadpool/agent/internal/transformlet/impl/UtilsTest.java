package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class UtilsTest {
    @Test
    public void test_get_unboxing_boolean_fromMap() {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            getUnboxingBoolean(map, "not_existed");
            fail();
        } catch (NullPointerException expected) {
            // do nothing
        }
    }

    private static boolean getUnboxingBoolean(Map<String, Object> map, String key) {
        return (Boolean) map.get(key);
    }
}
