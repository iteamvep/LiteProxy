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
public class ProxyInitException extends BaseException {
    
    public ProxyInitException(ErrorType errorType) {
        super(errorType);
    }
    
    public ProxyInitException(ErrorType errorType, String module) {
        super(errorType, module);
    }
    
    public ProxyInitException(ErrorType errorType, String module, String msg) {
        super(errorType, module, msg);
    }
    
}
