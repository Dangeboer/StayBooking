package com.laioffer.staybooking.exception;


public class InvalidAddressException extends RuntimeException{


    public InvalidAddressException() {
        super("Invalid address");
    }
}
