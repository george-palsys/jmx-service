package com.example;

public class Hello implements HelloMBean {
    private String message = "Hello, world!";

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void sayHello() {
        System.out.println(message);
    }
}
