package com.alibaba.support.junit.conditional

import com.alibaba.noTtlAgentRun
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition

class NoAgentRun : IgnoreCondition {
    override fun isSatisfied(): Boolean = noTtlAgentRun()
}
