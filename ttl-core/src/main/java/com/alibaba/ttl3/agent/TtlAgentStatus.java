package com.alibaba.ttl3.agent;

import edu.umd.cs.findbugs.annotations.NonNull;

import static com.alibaba.ttl3.agent.EmptyTtlAgentStatus.getLoadedAgentOrEmpty;

public interface TtlAgentStatus {
    /**
     * Whether TTL agent is loaded.
     */
    boolean isTtlAgentLoaded();

    @NonNull
    static TtlAgentStatus getInstance() {
        return getLoadedAgentOrEmpty();
    }
}
