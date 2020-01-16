package com.alibaba.support.junit.conditional

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils

/**
 * @see [Getting Java version at runtime](https://stackoverflow.com/a/23706899/922688)
 */
class BelowJava7 : IgnoreCondition {
    override fun isSatisfied(): Boolean =
            if (System.getProperty("java.specification.version").toDouble() >= 9) false
            else !SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)
}
