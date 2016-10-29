package com.groupon.jenkins.DeadlockKiller;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PassthroughInvocationHandler implements InvocationHandler {

    private final Object target;

    public PassthroughInvocationHandler(final Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return method.invoke(this.target, args);
    }
}
