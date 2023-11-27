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
 * @author EVO
 */
public class BusinessLogicException extends ResponseStatusException { 
    public BusinessLogicException() {
        super(HttpStatus.CONFLICT);
    }

    public BusinessLogicException(String reason) {
        super(HttpStatus.CONFLICT, reason);
    }

    public BusinessLogicException(String reason, Throwable cause) {
        super(HttpStatus.CONFLICT, reason, cause);
    }
}