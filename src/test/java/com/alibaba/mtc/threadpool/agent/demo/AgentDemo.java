package com.alibaba.mtc.threadpool.agent.demo;

import com.alibaba.mtc.MtContextThreadLocal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author ding.lid
 */
public class AgentDemo {
    static MtContextThreadLocal<String> stringMtContextThreadLocal = new MtContextThreadLocal<String>();

    static MtContextThreadLocal<Person> personReferenceMtContextThreadLocal = new MtContextThreadLocal<Person>() {
        @Override
        protected Person initialValue() {
            return new Person("unnamed", -1);
        }
    };

    static MtContextThreadLocal<Person> personCopyMtContextThreadLocal = new MtContextThreadLocal<Person>() {
        @Override
        protected Person copy(Person parentValue) {
            // copy value to child thread
            return new Person(parentValue.getName(), parentValue.getAge());
        }

        @Override
        protected Person initialValue() {
            return new Person("unnamed", -1);
        }
    };

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        expandThreadPool(executorService);

        stringMtContextThreadLocal.set("foo - main");
        personReferenceMtContextThreadLocal.set(new Person("jerry - reference", 1));
        personCopyMtContextThreadLocal.set(new Person("Tom - value", 2));

        printMtContextInfo("Main - Before execution of thread pool");

        Future<?> submit = executorService.submit(new Runnable() {
            @Override
            public void run() {
                printMtContextInfo("Thread Pool - enter");
                stringMtContextThreadLocal.set("foo - modified in thread pool");
                personReferenceMtContextThreadLocal.get().setName("jerry - reference - modified in thread pool");
                personCopyMtContextThreadLocal.get().setName("Tom - value - modified in thread pool");
                printMtContextInfo("Thread Pool - leave");
            }
        });
        submit.get();

        printMtContextInfo("Main - After execution of thread pool");

        System.exit(0);
    }

    public static void expandThreadPool(ExecutorService executor) throws Exception {
        List<Future<?>> ret = new ArrayList<Future<?>>();
        for (int i = 0; i < 3; ++i) {
            Future<?> submit = executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            ret.add(submit);
        }
        for (Future<?> future : ret) {
            future.get();
        }
    }

    static void printMtContextInfo(String msg) {
        System.out.println("====================================================");
        System.out.println(msg);
        System.out.println("====================================================");
        System.out.println("stringMtContextThreadLocal: " + stringMtContextThreadLocal.get());
        System.out.println("personReferenceMtContextThreadLocal: " + personReferenceMtContextThreadLocal.get());
        System.out.println("personCopyMtContextThreadLocal: " + personCopyMtContextThreadLocal.get());
    }

    public static class Person {
        String name;
        int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
