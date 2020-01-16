package com.alibaba.support.junit.conditional

import com.alibaba.noTtlAgentRun
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition

class NoAgentRunOrBelowJava8 : IgnoreCondition {
    override fun isSatisfied(): Boolean = noTtlAgentRun() || BelowJava8().isSatisfied
}
