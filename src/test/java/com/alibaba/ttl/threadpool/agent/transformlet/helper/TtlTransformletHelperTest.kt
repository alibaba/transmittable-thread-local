package com.alibaba.ttl.threadpool.agent.transformlet.helper

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.ConditionalIgnore
import com.alibaba.support.junit.conditional.IsAgentRun
import com.alibaba.ttl.threadpool.agent.TtlAgentLoggerInitializer
import com.alibaba.ttl.threadpool.agent.transformlet.helper.TtlTransformletHelper.getLocationFileOfClass
import org.apache.commons.lang3.StringUtils
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class TtlTransformletHelperTest {
    @Rule
    @JvmField
    val rule = ConditionalIgnoreRule()

    @Test
    fun test_getFileLocationOfClass_javaClass() {
        TtlAgentLoggerInitializer

        Assert.assertNull(getLocationFileOfClass(String::class.java))

        MatcherAssert.assertThat(
            getLocationFileOfClass(StringUtils::class.java),
            CoreMatchers.endsWith("/commons-lang3-3.5.jar")
        )
    }
}
