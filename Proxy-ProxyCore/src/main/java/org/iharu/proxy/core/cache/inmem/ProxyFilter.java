/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.cache.inmem;

import io.netty.handler.codec.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.iharu.proxy.core.entity.RuleFilterContainer;
import org.iharu.proxy.core.entity.RuleHandlerContainer;
import org.iharu.proxy.core.plugin.BasePlugin;
import org.littleshoot.proxy.impl.ProxyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iHaru
 */
public class ProxyFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ProxyFilter.class);
    
    private static final Map<String, RuleFilterContainer> HostBlackList = new ConcurrentHashMap();
    private static final Map<String, RuleFilterContainer> HostWhiteList = new ConcurrentHashMap();
    private static final Map<String, RuleFilterContainer> MitmBlackList = new ConcurrentHashMap();
    private static final Map<String, RuleFilterContainer> MitmWhiteList = new ConcurrentHashMap();
    private static boolean HostWhitelistFirst = true;
    private static boolean MitmWhitelistFirst = true;
    
    public static final Map<String, RuleHandlerContainer> RequestFilter = new ConcurrentHashMap();
    public static final Map<String, RuleHandlerContainer> ResponseFilter = new ConcurrentHashMap();
    
    public static void handleLoad(BasePlugin plugin) {
        List<String> uris = getRealUri(plugin.getPluginRequestUrlList());
        if(uris != null) {
            uris.forEach(uri -> {
                if(!RequestFilter.containsKey(uri)){
                    RequestFilter.put(uri, new RuleHandlerContainer(uri));
                }
                RequestFilter.get(uri).getPlugins().add(plugin);
            });
        }
        uris = getRealUri(plugin.getPluginResponseUrlList());
        if(uris != null) {
            uris.forEach(uri -> {
                if(!ResponseFilter.containsKey(uri)){
                    ResponseFilter.put(uri, new RuleHandlerContainer(uri));
                }
                ResponseFilter.get(uri).getPlugins().add(plugin);
            });
        }
        uris = getRealUri(plugin.getHostBalckList());
        if(uris != null){
            addList(uris, HostBlackList);
        }
        uris = getRealUri(plugin.getHostWhiteList());
        if(uris != null){
            addList(uris, HostWhiteList);
        }
        uris = plugin.getMitmBalckList();
        if(uris != null){
            addList(uris, MitmBlackList);
        }
        uris = plugin.getMitmWhiteList();
        if(uris != null){
            addList(uris, MitmWhiteList);
        }
    }
    
    public static void handleUnLoad(BasePlugin plugin) {
        String id = plugin.getID();
        List<String> uris = getRealUri(plugin.getPluginRequestUrlList());
        uris.forEach(uri -> {
            if(ProxyFilter.RequestFilter.containsKey(uri)){
                ProxyFilter.RequestFilter.get(uri).setPlugins(ProxyFilter.RequestFilter.get(uri).getPlugins()
                        .stream().filter(_plugin -> !id.equals(_plugin.getID())).collect(Collectors.toList()));
            }
        });
        uris = getRealUri(plugin.getPluginResponseUrlList());
        uris.forEach(uri -> {
            if(ProxyFilter.ResponseFilter.containsKey(uri)){
                ProxyFilter.ResponseFilter.get(uri).setPlugins(ProxyFilter.ResponseFilter.get(uri).getPlugins()
                        .stream().filter(_plugin -> !id.equals(_plugin.getID())).collect(Collectors.toList()));
            }
        });
        uris = getRealUri(plugin.getHostBalckList());
        if(uris != null){
            removeList(uris, HostBlackList);
        }
        uris = getRealUri(plugin.getHostWhiteList());
        if(uris != null){
            removeList(uris, HostWhiteList);
        }
        uris = plugin.getMitmBalckList();
        if(uris != null){
            removeList(uris, MitmBlackList);
        }
        uris = plugin.getMitmWhiteList();
        if(uris != null){
            removeList(uris, MitmWhiteList);
        }
    }
    
    private static List<String> getRealUri(List<String> uri){
        if(uri == null || uri.isEmpty())
            return null;
        List<String> rs = new ArrayList();
        uri.forEach(item -> {
            String _uri = item;
            if(_uri.endsWith("/"))
                _uri = _uri.substring(0, _uri.lastIndexOf("/"));
            rs.add(_uri);
        });
        return rs;
    }
    
    public static void addList(List<String> uris, Map<String, RuleFilterContainer> map){
        if(uris.isEmpty())
            return;
        uris.forEach(uri -> {
            if(map.containsKey(uri))
                map.get(uri).getCounter().increment();
            else
                map.put(uri, new RuleFilterContainer(uri));
        });
    }
    
    public static void removeList(List<String> uris, Map<String, RuleFilterContainer> map){
        if(uris.isEmpty())
            return;
        uris.forEach(uri -> {
            if(map.containsKey(uri)){
                map.get(uri).getCounter().decrement();
                if(map.get(uri).getCounter().intValue() == 0)
                    map.remove(uri);
            } else {
                LOG.warn("List: {} not exist.", uri);
            }
        });
    }
    
    public static boolean isBlackList(String url){
        return HostBlackList.containsKey(url);
    }
    
    public static boolean isWhiteList(String url){
        return HostWhiteList.containsKey(url);
    }
    
    public static boolean isBlockConnection(HttpRequest httpRequest){
        String host = ProxyUtils.getHost(httpRequest);
//        Pattern pattern;
        if(!HostWhiteList.isEmpty() && HostWhitelistFirst){
            boolean block = true;
            for(RuleFilterContainer filter:HostWhiteList.values()){
                if(filter.getPattern().matcher(host).find()){
                    block = false;
                    break;
                }
            }
            return block;
        } else if(!HostBlackList.isEmpty()) {
            for(RuleFilterContainer filter:HostBlackList.values()){
                if(filter.getPattern().matcher(host).find())
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isIgnoreMitm(HttpRequest httpRequest){
        String host = ProxyUtils.getHost(httpRequest);
//        Pattern pattern;
        if(!MitmWhiteList.isEmpty() && MitmWhitelistFirst){
            boolean ignore = true;
            for(RuleFilterContainer filter:MitmWhiteList.values()){
                if(filter.getPattern().matcher(host).find()){
                    ignore = false;
                    break;
                }
            }
            return ignore;
        } else if(!MitmBlackList.isEmpty()) {
            for(RuleFilterContainer filter:MitmBlackList.values()){
                if(filter.getPattern().matcher(host).find())
                    return true;
            }
        }
        return false;
    }

    public static String isRequestMatch(String uri){
        for(Entry<String,RuleHandlerContainer> entry:RequestFilter.entrySet()){
            if(entry.getValue().getPattern().matcher(uri).find())
                return entry.getKey();
        }
        return null;
    }
    
    public static String isResponseMatch(String uri){
        for(Entry<String,RuleHandlerContainer> entry:ResponseFilter.entrySet()){
            if(entry.getValue().getPattern().matcher(uri).find())
                return entry.getKey();
        }
        return null;
    }
    
    public static List<BasePlugin> getRequestHandler(String uri){
        for(RuleHandlerContainer handler:RequestFilter.values()){
            if(handler.getPattern().matcher(uri).find())
                return handler.getPlugins();
        }
        return new ArrayList();
    }
    
    public static List<BasePlugin> getResponseHandler(String uri){
        for(RuleHandlerContainer handler:ResponseFilter.values()){
            if(handler.getPattern().matcher(uri).find())
                return handler.getPlugins();
        }
        return new ArrayList();
    }
    
    /**
     * @param aMitmWhitelistFirst the MitmWhitelistFirst to set
     */
    public static void setMitmWhitelistFirst(boolean aMitmWhitelistFirst) {
        MitmWhitelistFirst = aMitmWhitelistFirst;
    }

    /**
     * @param aHostWhitelistFirst the HostWhitelistFirst to set
     */
    public static void setHostWhitelistFirst(boolean aHostWhitelistFirst) {
        HostWhitelistFirst = aHostWhitelistFirst;
    }
    
}
