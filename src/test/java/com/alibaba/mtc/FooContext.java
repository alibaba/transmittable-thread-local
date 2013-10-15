package com.alibaba.mtc;

/**
 * @author ding.lid
 */
public class FooContext implements Copyable<FooContext> {
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

    public FooContext() {
    }

    public FooContext(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public FooContext copy() {
        return new FooContext(name, age);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FooContext that = (FooContext) o;

        if (age != that.age) return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + age;
        return result;
    }
}
