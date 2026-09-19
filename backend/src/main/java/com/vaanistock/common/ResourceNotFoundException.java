package com.vaanistock.common;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
