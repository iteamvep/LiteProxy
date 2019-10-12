/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.interceptor;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.iharu.proxy.core.exception.ProxyInitException;
import org.iharu.type.error.ErrorType;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iHaru
 */
public class InterceptorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(InterceptorAdapter.class);
    
    protected ProxyInterceptor proxyInterceptor = null;
    
    public InterceptorAdapter(ProxyInterceptor proxyInterceptor){
        this.proxyInterceptor = proxyInterceptor;
    }
    
    public HttpFilters genFilterRequestAdapter(HttpRequest originalRequest, String url) {
        if(proxyInterceptor == null)
            throw new ProxyInitException(ErrorType.Not_NULL_ERROR, "ProxyInterceptor could not be null.");
        return new HttpFiltersAdapter(originalRequest) {
                        @Override
                        public HttpResponse clientToProxyRequest(
                                HttpObject httpObject) {
                            if(httpObject instanceof FullHttpRequest){
                                FullHttpRequest httpRequest = (FullHttpRequest) httpObject;
                                String regex = proxyInterceptor.requestMatch(httpRequest);
                                if(regex != null){
                                    try{
                                        proxyInterceptor.handelFullRequestMatch(httpRequest, regex);
                                    } catch (Exception ex) {
                                        LOG.error(ExceptionUtils.getStackTrace(ex));
                                    }
                                } 
                                
                                matchRegex = proxyInterceptor.responseMatch(httpRequest);
                                if(matchRegex != null){
                                    isResponseMatch = true;
                                }
                            } 
                            return null;
                        }

                        @Override
                        public HttpObject proxyToClientResponse(
                                HttpObject httpObject) {
                            if(isResponseMatch) {
                                if (httpObject instanceof FullHttpResponse) {
                                    try{
                                        proxyInterceptor.handelFullResponseMatch(originalRequest, (FullHttpResponse) httpObject, matchRegex);
                                    } catch (Exception ex) {
                                        LOG.error(ExceptionUtils.getStackTrace(ex));
                                    }
                                }  
                            }
                            return httpObject;
                        }

                        @Override
                        public HttpResponse proxyToServerRequest(
                                HttpObject httpObject) {
                            return null;
                        }
                        @Override
                        public HttpObject serverToProxyResponse(
                                HttpObject httpObject) {
                            return httpObject;
                        }
                    };
    }
}
