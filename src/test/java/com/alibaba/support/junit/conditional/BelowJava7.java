package com.alibaba.support.junit.conditional;

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

/**
 * @see <a href="https://stackoverflow.com/a/23706899/922688">Getting Java version at runtime</a>
 */
public class BelowJava7 implements IgnoreCondition {
    public boolean isSatisfied() {
        return !SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7);
    }
}
