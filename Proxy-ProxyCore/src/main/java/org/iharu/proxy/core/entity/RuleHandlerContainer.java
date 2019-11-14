/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.iharu.proxy.core.plugin.BasePlugin;

/**
 *
 * @author iHaru
 */
public class RuleHandlerContainer {
    private String uri;
    private List<BasePlugin> plugins = new ArrayList();
    private Pattern pattern;
    
    public RuleHandlerContainer(){}
    
    public RuleHandlerContainer(String uri){
        this.uri = uri;
        this.pattern = Pattern.compile(uri);
    }
    
    public RuleHandlerContainer(String uri, Pattern pattern){
        this.uri = uri;
        this.pattern = pattern;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the plugins
     */
    public List<BasePlugin> getPlugins() {
        return plugins;
    }

    /**
     * @param plugins the plugins to set
     */
    public void setPlugins(List<BasePlugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * @return the pattern
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
    
}
