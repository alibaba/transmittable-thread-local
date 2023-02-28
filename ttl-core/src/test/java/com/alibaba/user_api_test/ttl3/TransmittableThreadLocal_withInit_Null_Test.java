package com.alibaba.user_api_test.ttl3;

import com.alibaba.ttl3.TransmittableThreadLocal;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TransmittableThreadLocal_withInit_Null_Test {

    @Test
    public void test_null__withInitial() {
        try {
            TransmittableThreadLocal.<String>withInitial(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }
    }

    @Test
    public void test_null__withInitialAndGenerator_2() {
        try {
            TransmittableThreadLocal.<String>withInitialAndGenerator(null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndGenerator((Supplier<String>) () -> null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("value generator is null", e.getMessage());
        }
    }

    @Test
    public void test_null__withInitialAndGenerator_3() {
        try {
            TransmittableThreadLocal.<String>withInitialAndGenerator(null, null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndGenerator((Supplier<String>) () -> null, null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("value generator for child value is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndGenerator((Supplier<String>) () -> null, parentValue -> null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("value generator for transmittee value is null", e.getMessage());
        }
    }
}
