package com.nguyenhuuquang.hotelmanagement.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}