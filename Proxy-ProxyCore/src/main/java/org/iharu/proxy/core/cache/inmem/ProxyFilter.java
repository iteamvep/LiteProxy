/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.cache.inmem;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    
    private static final Map<String, Integer> HostBlackList = new ConcurrentHashMap();
    private static final Map<String, Integer> HostWhiteList = new ConcurrentHashMap();
    private static final Map<String, Integer> MitmBlackList = new ConcurrentHashMap();
    private static final Map<String, Integer> MitmWhiteList = new ConcurrentHashMap();
    private static boolean HostWhitelistFirst = true;
    private static boolean MitmWhitelistFirst = true;
    
    public static final Map<String, List<BasePlugin>> RequestFilter = new ConcurrentHashMap();
    public static final Map<String, List<BasePlugin>> ResponseFilter = new ConcurrentHashMap();
    
    public static void handleLoad(BasePlugin plugin) {
        List<String> uris = getRealUri(plugin.getPluginRequestUrlList());
        if(uris != null) {
            uris.forEach(uri -> {
                List<BasePlugin> _list = ProxyFilter.RequestFilter.get(uri);
                if(_list == null){
                    _list = new ArrayList();
                }
                _list.add(plugin);
                RequestFilter.put(uri, _list);
            });
        }
        uris = getRealUri(plugin.getPluginResponseUrlList());
        if(uris != null) {
            uris.forEach(uri -> {
                List<BasePlugin> _list = ResponseFilter.get(uri);
                if(_list == null){
                    _list = new ArrayList();
                }
                _list.add(plugin);
                ResponseFilter.put(uri, _list);
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
                List<BasePlugin> _list = ProxyFilter.RequestFilter.get(uri)
                        .stream().filter(_plugin -> !id.equals(_plugin.getID())).collect(Collectors.toList());
                ProxyFilter.RequestFilter.put(uri, _list);
            }
        });
        uris = getRealUri(plugin.getPluginResponseUrlList());
        uris.forEach(uri -> {
            if(ProxyFilter.ResponseFilter.containsKey(uri)){
                List<BasePlugin> _list = ProxyFilter.ResponseFilter.get(uri)
                        .stream().filter(_plugin -> !id.equals(_plugin.getID())).collect(Collectors.toList());
                ProxyFilter.ResponseFilter.put(uri, _list);
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
    
    public static void addList(List<String> uris, Map<String, Integer> map){
        if(uris.isEmpty())
            return;
        uris.forEach(uri -> {
            if(map.containsKey(uri))
                map.put(uri, map.get(uri)+1);
            else
                map.put(uri, 1);
        });
    }
    
    public static void removeList(List<String> uris, Map<String, Integer> map){
        if(uris.isEmpty())
            return;
        uris.forEach(uri -> {
            if(map.containsKey(uri)){
                int count = map.get(uri)-1;
                if(count == 0)
                    map.remove(uri);
                else
                    map.put(uri, count);
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
        Pattern pattern;
        if(!HostWhiteList.isEmpty() && HostWhitelistFirst){
            boolean block = true;
            for(String regex:HostWhiteList.keySet()){
                pattern = Pattern.compile(regex);
                if(pattern.matcher(host).find()){
                    block = false;
                    break;
                }
            }
            return block;
        } else if(!HostBlackList.isEmpty()) {
            for(String regex:HostBlackList.keySet()){
                pattern = Pattern.compile(regex);
                if(pattern.matcher(host).find())
                    return true;
            }
        }
        return false;
    }
    
    public static boolean isIgnoreMitm(HttpRequest httpRequest){
        String host = ProxyUtils.getHost(httpRequest);
        Pattern pattern;
        if(!MitmWhiteList.isEmpty() && MitmWhitelistFirst){
            boolean ignore = true;
            for(String regex:MitmWhiteList.keySet()){
                pattern = Pattern.compile(regex);
                if(pattern.matcher(host).find()){
                    ignore = false;
                    break;
                }
            }
            return ignore;
        } else if(!MitmBlackList.isEmpty()) {
            for(String regex:MitmBlackList.keySet()){
                pattern = Pattern.compile(regex);
                if(pattern.matcher(host).find())
                    return true;
            }
        }
        return false;
    }

    public static String isRequestMatch(String uri){
        Pattern pattern;
        for(String regex:RequestFilter.keySet()){
            pattern = Pattern.compile(regex);
            if(pattern.matcher(uri).find())
                return regex;
        }
        return null;
    }
    
    public static String isResponseMatch(String uri){
        Pattern pattern;
        for(String regex:ResponseFilter.keySet()){
            pattern = Pattern.compile(regex);
            if(pattern.matcher(uri).find())
                return regex;
        }
        return null;
    }
    
    public static List<BasePlugin> getRequestHandler(String uri){
        Pattern pattern;
        for(String regex:RequestFilter.keySet()){
            pattern = Pattern.compile(regex);
            if(pattern.matcher(uri).find())
                return RequestFilter.get(uri);
        }
        return new ArrayList();
    }
    
    public static List<BasePlugin> getResponseHandler(String uri){
        Pattern pattern;
        for(String regex:ResponseFilter.keySet()){
            pattern = Pattern.compile(regex);
            if(pattern.matcher(uri).find())
                return ResponseFilter.get(uri);
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
