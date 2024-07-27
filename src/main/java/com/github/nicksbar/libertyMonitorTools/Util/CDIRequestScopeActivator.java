package com.github.nicksbar.libertyMonitorTools.Util;


import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.spi.CDI;

@RequestScoped
public class CDIRequestScopeActivator {

    @ActivateRequestContext
    public static void activate(Runnable runnable) {
        runnable.run();
    }

    public static <T> T getBean(Class<T> beanClass) {
        return CDI.current().select(beanClass).get();
    }
}

