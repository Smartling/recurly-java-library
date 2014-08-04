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

import com.ning.billing.recurly.model.Transaction;
import com.ning.billing.recurly.model.errors.Errors;
import com.ning.billing.recurly.model.errors.TransactionError;

public class TransactionException extends RequestException {

    public TransactionException(Errors errors, String message, String url, int statusCode) {
        super(errors, message, url, statusCode);
    }

    public Transaction getTransaction() {
        return null != getErrors() ? getErrors().getTransaction() : null;
    }

    public TransactionError getTransactionError() {
        return null != getErrors() ? getErrors().getTransactionError() : null;
    }
}
