package com.alibaba.support.junit.conditional

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition

class IsAgentRunOrBelowJava8 : IgnoreCondition {
    override fun isSatisfied(): Boolean = IsAgentRun().isSatisfied || BelowJava8().isSatisfied
}
