/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.plugin.management;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import org.iharu.proxy.core.cache.inmem.ProxyFilter;
import org.iharu.proxy.core.plugin.BasePlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iHaru
 */
public class PluginManager {
    private static final Logger LOG = LoggerFactory.getLogger(PluginManager.class);
    
    private static final SecureRandom random = new SecureRandom();
    
    @PreDestroy
    public void destroyMethod() {
        ProxyFilter.RequestFilter.forEach((k, v) -> {
            v.getPlugins().forEach(plugin -> {
                UnloadPlugin(plugin);
            });
        });
        ProxyFilter.ResponseFilter.forEach((k, v) -> {
            v.getPlugins().forEach(plugin -> {
                UnloadPlugin(plugin);
            });
        });
    }
    
    public static boolean LoadPlugin(BasePlugin plugin) throws Exception {
        int size = 0;
        if(plugin.getPluginRequestUrlList()!=null)
            size += plugin.getPluginRequestUrlList().size();
        if(plugin.getPluginResponseUrlList()!=null)
            size += plugin.getPluginResponseUrlList().size();
        if(size == 0){
            LOG.warn("Plugin: {}'s url filter list is empty.", plugin.getPluginName());
            return false;
        }
        
        String id = UUID.randomUUID().toString();
        plugin.setID(id);
        ProxyFilter.handleLoad(plugin);
        plugin.init();
        LOG.info("plugin {} loaded. id: {}", plugin.getPluginName(), plugin.getID());
        return true;
    }
    
    public static boolean UnloadPlugin(BasePlugin plugin) {
        ProxyFilter.handleUnLoad(plugin);
        plugin.destroy();
        LOG.info("plugin {} unloaded. id: {}", plugin.getPluginName(), plugin.getID());
        return true;
    }
    
}
