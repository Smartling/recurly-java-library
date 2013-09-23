package com.ning.billing.recurly.exceptions;

public class RequestException extends RecurlyException
{
    public static final String URL_MESSAGE = "Recurly error while calling: %s\n";
    public static final String BODY_MESSAGE = "Recurly error: %s";

    private String url;
    private String body;

    public RequestException(String url, String body) {
        this.url = url;
        this.body = body;
    }

    @Override
    public String getMessage() {
        return String.format(URL_MESSAGE, url)
               + String.format(BODY_MESSAGE, body);
    }

}
