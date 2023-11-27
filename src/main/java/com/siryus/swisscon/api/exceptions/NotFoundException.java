/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siryus.swisscon.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NotFoundException extends ResponseStatusException { 
    public NotFoundException() {
        super(HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }

    public NotFoundException(String reason, Throwable cause) {
        super(HttpStatus.NOT_FOUND, reason, cause);
    }
}
