package com.alibaba.support.junit.conditional

import com.alibaba.noTtlAgentRun
import com.alibaba.support.junit.conditional.ConditionalIgnoreRule.IgnoreCondition

/**
 * @see [Getting Java version at runtime](https://stackoverflow.com/a/23706899/922688)
 */
class IsAgentRun : IgnoreCondition {
    override fun isSatisfied(): Boolean = !noTtlAgentRun()
}
