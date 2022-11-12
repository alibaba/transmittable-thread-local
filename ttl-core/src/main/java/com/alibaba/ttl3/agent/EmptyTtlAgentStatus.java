package com.alibaba.ttl3.agent;

import edu.umd.cs.findbugs.annotations.NonNull;

final class EmptyTtlAgentStatus implements TtlAgentStatus {
    @Override
    public boolean isTtlAgentLoaded() {
        return false;
    }

    private EmptyTtlAgentStatus() {
    }


    ///////////////////////////////////////////////////////////////////////////
    // Singleton maintenance logic
    ///////////////////////////////////////////////////////////////////////////

    private static volatile TtlAgentStatus ttlAgentStatus = null;

    @NonNull
    static TtlAgentStatus getLoadedAgentOrEmpty() {
        if (ttlAgentStatus != null) return ttlAgentStatus;

        synchronized (EmptyTtlAgentStatus.class) {
            // double check
            if (ttlAgentStatus != null) return ttlAgentStatus;

            final String TTL_AGENT_CLASS = "com.alibaba.ttl3.agent.TtlAgent";

            TtlAgentStatus ret;
            try {
                ret = (TtlAgentStatus) Class.forName(TTL_AGENT_CLASS).getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException e) {
                ret = new EmptyTtlAgentStatus();
            } catch (Exception e) {
                throw new IllegalStateException("Fail to create the TTL agent instance(" + TTL_AGENT_CLASS
                        + "), should be a bug! report to the TTL project. cause: " + e, e);
            }

            ttlAgentStatus = ret;
            return ret;
        }
    }
}
