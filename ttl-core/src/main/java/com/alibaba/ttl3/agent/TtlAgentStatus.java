package com.alibaba.ttl3.agent;

import static com.alibaba.ttl3.agent.EmptyTtlAgentStatus.getLoadedAgentOrEmpty;

public interface TtlAgentStatus {
    /**
     * Whether TTL agent is loaded.
     */
    boolean isTtlAgentLoaded();

    static TtlAgentStatus getInstance() {
        return getLoadedAgentOrEmpty();
    }
}
