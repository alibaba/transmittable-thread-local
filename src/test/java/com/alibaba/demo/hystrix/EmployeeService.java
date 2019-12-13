package com.alibaba.demo.hystrix;

import org.springframework.stereotype.Component;

@Component
public abstract class EmployeeService {
    public abstract void getEmployee(int employeeId);

    public void fallbackMethod(int employeeid) {
        ThreadLocalUtil.addDataToThreadLocalMap("ErrorResponse", "Fallback response:: No employee details available temporarily");
    }
}
