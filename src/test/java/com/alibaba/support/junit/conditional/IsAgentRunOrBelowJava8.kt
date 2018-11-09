package com.alibaba.support.junit.conditional

import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition

/**
 * @see [Getting Java version at runtime](https://stackoverflow.com/a/23706899/922688)
 */
class IsAgentRunOrBelowJava8 : IgnoreCondition {
    override fun isSatisfied(): Boolean = IsAgentRun().isSatisfied || BelowJava8().isSatisfied
}
