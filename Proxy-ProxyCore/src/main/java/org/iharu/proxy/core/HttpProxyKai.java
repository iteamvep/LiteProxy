/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import org.iharu.proxy.core.config.ProxyConfig;
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
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.iharu.exception.BaseException;
import org.iharu.proxy.core.cache.inmem.AppDataCache;
import org.iharu.proxy.core.cache.inmem.ProxyFilter;
import org.iharu.type.error.ErrorType;
import org.littleshoot.proxy.ChainedProxyType;
import org.littleshoot.proxy.UnknownChainedProxyTypeException;
import org.littleshoot.proxy.impl.ProxyUtils;

/**
 *
 * @author iHaru
 */
public class HttpProxyKai {
    private static final Logger LOG = LoggerFactory.getLogger(HttpProxyKai.class);
    
    private static HttpProxyKai instance = null;
    private static boolean isInit = false;
    private HttpProxyServerBootstrap httpProxyServerBootstrap;
    private HttpProxyServer httpProxyServer;
    private ProxyConfig proxyConfig;
    private List<String> hostBypassWhiteList = null;
    private List<String> hostBypassBlackList = null;
    
    private static final AttributeKey<String> CONNECTED_URL = AttributeKey.valueOf("connected_url");
    
    protected final AtomicLong REQUESTS_SENT_BY_DOWNSTREAM = new AtomicLong(
            0l);
    protected final AtomicLong REQUESTS_RECEIVED_BY_UPSTREAM = new AtomicLong(
            0l);
    protected final ConcurrentSkipListSet<TransportProtocol> TRANSPORTS_USED = new ConcurrentSkipListSet();

    private static final String OPTION_DNSSEC = "dnssec";

    private static final String OPTION_PORT = "port";

    private static final String OPTION_HELP = "help";

    private static final String OPTION_MITM = "mitm";

    private static final String OPTION_NIC = "nic";

    protected final ActivityTracker DOWNSTREAM_TRACKER = new ActivityTrackerAdapter() {
        @Override
        public void requestSentToServer(FullFlowContext flowContext,
                io.netty.handler.codec.http.HttpRequest httpRequest) {
            REQUESTS_SENT_BY_DOWNSTREAM.incrementAndGet();
            TRANSPORTS_USED.add(flowContext.getChainedProxy()
                    .getTransportProtocol());
        }
    };

    private static void printHelp(final Options options,
            final String errorMessage) {
        if (!StringUtils.isBlank(errorMessage)) {
            LOG.error(errorMessage);
            System.err.println(errorMessage);
        }

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("littleproxy", options);
    }
    
    private HttpProxyKai(){
        
    }
    
    public static HttpProxyKai getInstance(){
        if(instance==null){
            instance=new HttpProxyKai();
        }
        return instance;
    }
    
    public void setPriorityRule(boolean host, boolean mitm) throws Exception{
        if(AppDataCache.isInterceptorReady())
            throw new BaseException(ErrorType.CONFIGURATION_ERROR, "proxy have been started");
        ProxyFilter.setHostWhitelistFirst(host);
        ProxyFilter.setMitmWhitelistFirst(mitm);
    }
    
    public void InitProxy(InterceptorAdapter filterAdapter, String proxyname, int listenPort, ProxyConfig proxyConfig, boolean localOnly, CertificateData certificateData) throws RootCertificateException {
        if(isInit){
            return;
        }
        this.proxyConfig = proxyConfig;
        
//        final Options options = new Options();
//        options.addOption(null, OPTION_DNSSEC, true,
//                "Request and verify DNSSEC signatures.");
//        options.addOption(null, OPTION_PORT, true, "Run on the specified port.");
//        options.addOption(null, OPTION_NIC, true, "Run on a specified Nic");
//        options.addOption(null, OPTION_HELP, false,
//                "Display command line help.");
//        options.addOption(null, OPTION_MITM, false, "Run as man in the middle.");
        
//        ThreadPoolConfiguration threadPoolConfiguration = new ThreadPoolConfiguration()
//                .withAcceptorThreads(12)
//                .withClientToProxyWorkerThreads(12)
//                .withProxyToServerWorkerThreads(12);

        LOG.info("About to start server on port: " + listenPort);
        httpProxyServerBootstrap = DefaultHttpProxyServer
                .bootstrap()
                .withName(proxyname)
                .withPort(listenPort)
                .withAllowLocalOnly(localOnly)
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                
                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext clientCtx) {
//                        LOG.info("incoming req: {}{}", ProxyUtils.getHost(originalRequest), originalRequest.uri());
                        if(ProxyFilter.isBlockConnection(originalRequest)){
                            return null;
                        }
                        
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
        
        if(certificateData != null) {
            httpProxyServerBootstrap.withManInTheMiddle(new CertificateSniffingMitmManager(certificateData));
        }
        if(proxyConfig != null) {
            httpProxyServerBootstrap.plusActivityTracker(DOWNSTREAM_TRACKER);
            httpProxyServerBootstrap.withChainProxyManager(chainedProxyManager());
        }

//        if (cmd.hasOption(OPTION_NIC)) {
//            final String val = cmd.getOptionValue(OPTION_NIC);
//            httpProxyServerBootstrap.withNetworkInterface(new InetSocketAddress(val, 0));
//        }

//        if (cmd.hasOption(OPTION_MITM)) {
//            LOG.info("Running as Man in the Middle");
//            httpProxyServerBootstrap.withManInTheMiddle(new SelfSignedMitmManager());
//        }
        
//        if (cmd.hasOption(OPTION_DNSSEC)) {
//            final String val = cmd.getOptionValue(OPTION_DNSSEC);
//            if (ProxyUtils.isTrue(val)) {
//                LOG.info("Using DNSSEC");
//                httpProxyServerBootstrap.withUseDnsSec(true);
//            } else if (ProxyUtils.isFalse(val)) {
//                LOG.info("Not using DNSSEC");
//                httpProxyServerBootstrap.withUseDnsSec(false);
//            } else {
//                printHelp(options, "Unexpected value for " + OPTION_DNSSEC
//                        + "=:" + val);
//                return;
//            }
//        }
        
        
        
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
                throw new BaseException(ErrorType.CONFIGURATION_ERROR,
                            "Unable to resolve " + proxyConfig.getHost());
            }
        }
    }
    
    private ChainedProxyManager chainedSocksProxyManager() {
        return (httpRequest, chainedProxies, details) -> chainedProxies.add(new ChainedProxyAdapter() {
            @Override
            public InetSocketAddress getChainedProxyAddress() {
                try {
                    return new InetSocketAddress(
                            InetAddress.getByName(proxyConfig.getHost()), 
                            proxyConfig.getPort());
                } catch (UnknownHostException uhe) {
                    throw new BaseException(ErrorType.CONFIGURATION_ERROR,
                            "Unable to resolve " + proxyConfig.getHost());
                }
            }
            @Override
            public ChainedProxyType getChainedProxyType() {
                final ChainedProxyType socksProxyType = proxyConfig.getChainedProxyType();
                switch (socksProxyType) {
                    case SOCKS4:
                    case SOCKS5:
                        return socksProxyType;
                    default:
                        LOG.error(socksProxyType + " is not a type of SOCKS proxy");
                        throw new UnknownChainedProxyTypeException(socksProxyType);
                }
            }
        });
    }
    
    public void StartProxy(){
        this.httpProxyServer = httpProxyServerBootstrap.start();
        AppDataCache.setInterceptorReady(true);
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
