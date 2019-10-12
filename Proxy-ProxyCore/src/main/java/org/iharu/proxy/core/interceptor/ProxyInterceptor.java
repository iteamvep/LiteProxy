/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.interceptor;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 *
 * @author iHaru
 */
public interface ProxyInterceptor {
    
    public org.slf4j.Logger GetImplLogger();
    
    public String requestMatch(HttpRequest httpRequest);
    
    public String responseMatch(HttpRequest httpRequest);
    
    public void handelFullRequestMatch(FullHttpRequest httpRequest, String matchRegex);
    
    public void handelFullResponseMatch(HttpRequest httpRequest, FullHttpResponse httpResponse, String matchRegex);
    
    public void handelFullRequestMatchWithIdentification(String identification, FullHttpRequest httpRequest, String matchRegex);
    
    public void handelFullResponseMatchWithIdentification(String identification, HttpRequest httpRequest, FullHttpResponse httpResponse, String matchRegex);
    
}
