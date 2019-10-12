/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.plugin;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import java.util.List;
import org.iharu.proxy.core.exception.PluginException;

/**
 *
 * @author iHaru
 */
public abstract class BasePlugin {
    
    private String ID;
    
    private long errorNum = 0L;
    
    public abstract String getPluginName();

    public abstract String getPluginVersion();

    public abstract String getPluginDescribe();
    
    public abstract String getPluginAuthor();
    
    public abstract List<String> getPluginRequestUrlList();
    
    public abstract List<String> getPluginResponseUrlList();
    
    public abstract void responseHandler(String uri, String content, HttpRequest httpRequest, FullHttpResponse httpResponse);
    
    public abstract void requestHandler(String uri, FullHttpRequest httpRequest);
    
    public abstract void responseHandlerWithIdentification(String identity, String uri, String content, HttpRequest httpRequest, FullHttpResponse httpResponse);
    
    public abstract void requestHandlerWithIdentification(String identity, String uri, FullHttpRequest httpRequest);
    
    public abstract List<String> getHostBalckList();
    
    public abstract List<String> getHostWhiteList();
    
    public abstract List<String> getMitmBalckList();
    
    public abstract List<String> getMitmWhiteList();
    
    public abstract void init() throws PluginException;
    
    public abstract void destroy() throws PluginException;

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * @return the errorNum
     */
    public long getErrorNum() {
        return errorNum;
    }

    /**
     * @param errorNum the errorNum to set
     */
    public void setErrorNum(long errorNum) {
        this.errorNum = errorNum;
    }
    
    
    
}
