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

package com.ning.billing.recurly.model.errors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ning.billing.recurly.model.RecurlyObject;
import com.ning.billing.recurly.model.Transaction;

@XmlRootElement(name = "errors")
public class Errors extends RecurlyObject {

    @XmlElement(name = "transaction_error")
    private TransactionError transactionError;

    @XmlElement(name = "transaction")
    private Transaction transaction;

    @XmlElement(name = "error")
    private RecurlyErrors recurlyErrors;

    public TransactionError getTransactionError() {
        return transactionError;
    }

    public void setTransactionError(TransactionError transactionError) {
        this.transactionError = transactionError;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public RecurlyErrors getRecurlyErrors() {
        return recurlyErrors;
    }

    public void setRecurlyErrors(final RecurlyErrors recurlyErrors) {
        this.recurlyErrors = recurlyErrors;
    }
}
