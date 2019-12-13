package com.alibaba.demo.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends EmployeeService {

    @HystrixCommand(fallbackMethod = "fallbackMethod", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "900"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "10") })
    public void getEmployee(int employeeId) {
        System.out.println("Getting Employee details for " + employeeId + ", threadLocalUtil : " + ThreadLocalUtil.getDataFromThreadLocalMap("EMPLOYEE_ID"));
        String response = restTemplate.exchange("http://localhost:8011/findEmployeeDetails/{employeeid}",
                HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                }, employeeId).getBody();

        ThreadLocalUtil.addDataToThreadLocalMap("Response", response);
    }

    @Autowired
    RestTemplate restTemplate;
}
