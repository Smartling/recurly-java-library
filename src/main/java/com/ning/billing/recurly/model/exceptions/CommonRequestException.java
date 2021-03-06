/*
 * Copyright 2010-2013 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.recurly.model.exceptions;

public class CommonRequestException extends RecurlyException
{
    public static final String URL_MESSAGE = "Recurly error while calling: %s\n";
    public static final String BODY_MESSAGE = "Recurly error: error code %d (%s)";

    private String url;
    private String errorMessage;
    private int statusCode;


    public CommonRequestException(String errorMessage, String url, int statusCode)
    {
        super();
        this.url = url;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return String.format(URL_MESSAGE, url) + String.format(BODY_MESSAGE, statusCode, errorMessage);
    }
}
