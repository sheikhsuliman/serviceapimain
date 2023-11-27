/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 *
 * @author hng
 */
public abstract class BaseResponseException extends ResponseStatusException {
    
    public BaseResponseException(HttpStatus status) {
        super(status);
    }
    
    public BaseResponseException(HttpStatus status, String reason) {
        super(status, reason);
    }
    
    public BaseResponseException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }
}
