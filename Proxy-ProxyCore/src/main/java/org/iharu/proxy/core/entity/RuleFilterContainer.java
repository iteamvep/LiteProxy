/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.entity;

import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

/**
 *
 * @author iHaru
 */
public class RuleFilterContainer {
    private String uri;
    private LongAdder counter = new LongAdder();
    private Pattern pattern;
    
    public RuleFilterContainer(){}
    
    public RuleFilterContainer(String uri){
        this.counter.increment();
        this.uri = uri;
        this.pattern = Pattern.compile(uri);
    }
    
    public RuleFilterContainer(String uri, Pattern pattern){
        this.counter.increment();
        this.uri = uri;
        this.pattern =pattern;
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
     * @return the counter
     */
    public LongAdder getCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(LongAdder counter) {
        this.counter = counter;
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
