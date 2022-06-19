package com.alibaba.ttl.user_api_test;

import com.alibaba.ttl.TransmittableThreadLocal;
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
    public void test_null__withInitialAndCopier_2() {
        try {
            TransmittableThreadLocal.<String>withInitialAndCopier(null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndCopier((Supplier<String>) () -> null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("ttl copier is null", e.getMessage());
        }
    }

    @Test
    public void test_null__withInitialAndCopier_3() {
        try {
            TransmittableThreadLocal.<String>withInitialAndCopier(null, null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndCopier((Supplier<String>) () -> null, null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("ttl copier for child value is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndCopier((Supplier<String>) () -> null, parentValue -> null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("ttl copier for copy value is null", e.getMessage());
        }
    }
}
