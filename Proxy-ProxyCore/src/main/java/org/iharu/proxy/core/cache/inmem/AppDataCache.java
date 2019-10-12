/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.cache.inmem;

import java.util.concurrent.atomic.AtomicBoolean;
import org.iharu.proxy.core.interceptor.InterceptorAdapter;
import org.iharu.proxy.core.interceptor.ProxyInterceptor;

/**
 *
 * @author iHaru
 */
public class AppDataCache {
    
    private static AtomicBoolean InterceptorReady = new AtomicBoolean(false);

    /**
     * @return the InterceptorReady
     */
    public static boolean isInterceptorReady() {
        return InterceptorReady.get();
    }

    /**
     * @param aInterceptorReady the InterceptorReady to set
     */
    public static void setInterceptorReady(boolean aInterceptorReady) {
        InterceptorReady.set(aInterceptorReady);
    }
    
}
