package com.alibaba.ttl.threadpool.agent.check;

public class AgentCheckMain {
    public static void main(String[] args) {
        try {
            ExecutorClassesAgentCheck.main(args);
            ForkJoinTaskClassAgentCheck.main(args);
        } catch (Throwable e) {
            System.out.println("Exception when run AgentCheck: ");
            e.printStackTrace(System.out);
            System.exit(2);
        }
    }
}
