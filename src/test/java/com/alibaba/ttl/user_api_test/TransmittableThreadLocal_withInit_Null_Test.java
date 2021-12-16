package com.alibaba.ttl.user_api_test;

import com.alibaba.support.junit.conditional.BelowJava8;
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule;
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlCopier;
import org.junit.Rule;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TransmittableThreadLocal_withInit_Null_Test {
    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Test
    @ConditionalIgnore(condition = BelowJava8.class)
    public void test_null__withInitial() {
        try {
            TransmittableThreadLocal.<String>withInitial(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }
    }

    @Test
    @ConditionalIgnore(condition = BelowJava8.class)
    public void test_null__withInitialAndCopier_2() {
        try {
            TransmittableThreadLocal.<String>withInitialAndCopier(null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndCopier(new Supplier<String>() {
                @Override
                public String get() {
                    return null;
                }
            }, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("ttl copier is null", e.getMessage());
        }
    }

    @Test
    @ConditionalIgnore(condition = BelowJava8.class)
    public void test_null__withInitialAndCopier_3() {
        try {
            TransmittableThreadLocal.<String>withInitialAndCopier(null, null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("supplier is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndCopier(new Supplier<String>() {
                @Override
                public String get() {
                    return null;
                }
            }, null, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("ttl copier for child value is null", e.getMessage());
        }

        try {
            TransmittableThreadLocal.withInitialAndCopier(new Supplier<String>() {
                @Override
                public String get() {
                    return null;
                }
            }, new TtlCopier<String>() {
                @Override
                public String copy(String parentValue) {
                    return null;
                }
            }, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("ttl copier for copy value is null", e.getMessage());
        }
    }
}
