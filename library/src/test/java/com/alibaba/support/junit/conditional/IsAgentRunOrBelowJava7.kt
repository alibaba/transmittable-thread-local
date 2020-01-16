package com.alibaba.support.junit.conditional

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition

class IsAgentRunOrBelowJava7 : IgnoreCondition {
    override fun isSatisfied(): Boolean = IsAgentRun().isSatisfied || BelowJava7().isSatisfied
}
