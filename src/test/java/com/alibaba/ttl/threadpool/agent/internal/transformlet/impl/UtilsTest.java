package com.alibaba.ttl.threadpool.agent.internal.transformlet.impl;

import com.alibaba.ttl.threadpool.agent.internal.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.ttl.threadpool.agent.internal.transformlet.impl.Utils.*;
import static org.junit.Assert.*;

public class UtilsTest {
    @BeforeClass
    public static void beforeClass() {
        Logger.setLoggerImplTypeIfNotSetYet("stderr");
    }

    @Test
    public void test_get_unboxing_boolean_fromMap() {
        Map<String, Object> map = new HashMap<>();

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

    @Test
    public void test_className_package() {
        assertEquals("", getPackageName("Hello"));
        assertEquals("com.foo", getPackageName("com.foo.Hello"));

        assertTrue(isClassAtPackage("java.util.TimerTask", "java.util"));
        assertFalse(isClassAtPackage("java.util.TimerTask", "java.utils"));
        assertFalse(isClassAtPackage("java.util.TimerTask", "java"));
        assertFalse(isClassAtPackage("java.util.TimerTask", "java.util.zip"));

        assertTrue(isClassUnderPackage("java.util.TimerTask", "java.util"));
        assertFalse(isClassUnderPackage("java.util.TimerTask", "java.utils"));
        assertTrue(isClassUnderPackage("java.util.TimerTask", "java"));
        assertFalse(isClassUnderPackage("java.util.TimerTask", "javax"));

        assertTrue(isClassAtPackageJavaUtil("java.util.PriorityQueue"));
        assertFalse(isClassAtPackageJavaUtil("java.util.zip.ZipInputStream"));

        assertTrue(isClassOrInnerClass(Map.class.getName(), Map.class.getName()));
        assertTrue(isClassOrInnerClass(Map.Entry.class.getName(), Map.class.getName()));
        assertTrue(isClassOrInnerClass(Map.Entry.class.getName(), Map.Entry.class.getName()));
    }
}
