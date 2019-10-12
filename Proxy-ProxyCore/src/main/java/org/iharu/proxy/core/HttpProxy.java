/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import org.iharu.proxy.core.config.ProxyConfig;
import org.iharu.proxy.core.interceptor.ProxyInterceptor;
import org.iharu.proxy.core.entity.CertificateData;
import org.iharu.proxy.core.interceptor.InterceptorAdapter;
import org.littleshoot.proxy.ActivityTracker;
import org.littleshoot.proxy.ActivityTrackerAdapter;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.FullFlowContext;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.TransportProtocol;
import org.littleshoot.proxy.impl.ClientDetails;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iHaru
 */
public class HttpProxy {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProxy.class);
    
    private static HttpProxy instance = null;
    private static boolean isInit = false;
    private HttpProxyServerBootstrap httpProxyServerBootstrap;
    private HttpProxyServer httpProxyServer;
    private ProxyConfig proxyConfig;
    
    private static final AttributeKey<String> CONNECTED_URL = AttributeKey.valueOf("connected_url");
    
    protected final AtomicLong REQUESTS_SENT_BY_DOWNSTREAM = new AtomicLong(
            0l);
    protected final AtomicLong REQUESTS_RECEIVED_BY_UPSTREAM = new AtomicLong(
            0l);
    protected final ConcurrentSkipListSet<TransportProtocol> TRANSPORTS_USED = new ConcurrentSkipListSet();

    protected final ActivityTracker DOWNSTREAM_TRACKER = new ActivityTrackerAdapter() {
        @Override
        public void requestSentToServer(FullFlowContext flowContext, HttpRequest httpRequest) {
            REQUESTS_SENT_BY_DOWNSTREAM.incrementAndGet();
            TRANSPORTS_USED.add(flowContext.getChainedProxy()
                    .getTransportProtocol());
        }
    };
    
    private HttpProxy(){
        
    }
    
    public static HttpProxy getInstance(){
        if(instance==null){
            instance=new HttpProxy();
        }
        return instance;
    }
    
    public void InitProxy(InterceptorAdapter filterAdapter, String proxyname, int listenPort, ProxyConfig proxyConfig, boolean localOnly, CertificateData certificateData) throws RootCertificateException {
        if(isInit){
            return;
        }
        this.proxyConfig = proxyConfig;
        
        ThreadPoolConfiguration threadPoolConfiguration = new ThreadPoolConfiguration()
                .withAcceptorThreads(12)
                .withClientToProxyWorkerThreads(12)
                .withProxyToServerWorkerThreads(12);
        
        httpProxyServerBootstrap = DefaultHttpProxyServer
            .bootstrap();
        
        httpProxyServerBootstrap.withAllowLocalOnly(localOnly)
            .withName(proxyname)
            .withPort(listenPort)
            .withThreadPoolConfiguration(threadPoolConfiguration)
//            .plusActivityTracker(DOWNSTREAM_TRACKER)
            .withFiltersSource(new HttpFiltersSourceAdapter() {
                
                @Override
                public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext clientCtx) {
                    String uri = originalRequest.uri();
                    if (originalRequest.method()== HttpMethod.CONNECT) {
                        if (clientCtx != null) {
                            LOG.info("https uri: {}", uri);
                            String prefix = "https://" + uri.replaceFirst(":443$", "");
                            clientCtx.channel().attr(CONNECTED_URL).set(prefix);
                        }
                        return new HttpFiltersAdapter(originalRequest, clientCtx);
                    }
                    String connectedUrl = clientCtx.channel().attr(CONNECTED_URL).get();
                    if (connectedUrl == null) {
                        return filterAdapter.genFilterRequestAdapter(originalRequest, uri);
                    }
                    return filterAdapter.genFilterRequestAdapter(originalRequest, connectedUrl + uri);
                }
                
                @Override
                public int getMaximumRequestBufferSizeInBytes() {
                    return 1024 * 1024;
                }
                @Override
                public int getMaximumResponseBufferSizeInBytes() {
                    return 10 * 1024 * 1024;
                }
            });
        if(certificateData != null)
            httpProxyServerBootstrap.withManInTheMiddle(new CertificateSniffingMitmManager(certificateData));
        if(proxyConfig != null)
            this.httpProxyServerBootstrap.withChainProxyManager(chainedProxyManager());
    }
    
    protected ChainedProxyManager chainedProxyManager() {
        return new ChainedProxyManager() {
            @Override
            public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies, ClientDetails clientDetails) {
                chainedProxies.add(newChainedProxy());
            }
        };
    }
    
    protected ChainedProxy newChainedProxy() {
        return new BaseChainedProxy();
    }
    
    protected class BaseChainedProxy extends ChainedProxyAdapter {
        @Override
        public InetSocketAddress getChainedProxyAddress() {
            try {
                return new InetSocketAddress(
                        InetAddress.getByName(proxyConfig.getHost()), 
                        proxyConfig.getPort());
            } catch (UnknownHostException uhe) {
                throw new RuntimeException(
                        "Unable to resolve 127.0.0.1?!");
            }
        }
    }
    
    public void StartProxy(){
        this.httpProxyServer = httpProxyServerBootstrap.start();
    }
    
    public void CloseProxy(){
        this.httpProxyServer.stop();
        isInit = false;
        instance = null;
    }
    
    protected void tearDown() throws Exception {
        this.httpProxyServer.abort();
    }
    
    public static void waitUntilInterupted() {
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                        break;
                    }
                }
            }
        }.run();
    }
}
