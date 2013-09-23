package com.ning.billing.recurly.exceptions;

public class RecurlyException extends Exception {
    public RecurlyException() {
    }

    public RecurlyException(String message) {
        super(message);
    }

    public RecurlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecurlyException(Throwable cause) {
        super(cause);
    }
}
