package com.alibaba.ttl3.agent;

public interface TtlAgentStatus {
    /**
     * Whether TTL agent is loaded.
     */
    boolean isTtlAgentLoaded();

    static TtlAgentStatus getInstance() {
        return EmptyTtlAgentStatus.getInstance();
    }
}
