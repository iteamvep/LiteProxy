/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iharu.proxy.core.exception;

import org.iharu.exception.BaseException;
import org.iharu.type.error.ErrorType;

/**
 *
 * @author iHaru
 */
public class PluginException extends BaseException {
    
    public PluginException(ErrorType errorType, String module, String msg, Throwable sourceException) {
        super(errorType, module, msg, sourceException);
    }
    
    public PluginException(ErrorType errorType, String module, String msg) {
        super(errorType, module, msg);
    }
    
    public PluginException(ErrorType errorType, String module) {
        super(errorType, module);
    }
    
    public PluginException(ErrorType errorType, Throwable sourceException) {
        super(errorType, sourceException);
    }
    
    public PluginException(ErrorType errorType) {
        super(errorType);
    }
    
}
