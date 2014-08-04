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

@XmlRootElement(name = "transaction_error")
public class TransactionError extends RecurlyObject {

    @XmlElement(name = "error_code")
    private String errorCode;

    @XmlElement(name = "error_category")
    private String errorCategory;

    @XmlElement(name = "merchant_message")
    private String merchantMessage;

    @XmlElement(name = "customer_message")
    private String customerMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final Object errorCode) {
        this.errorCode = stringOrNull(errorCode);
    }

    public String getErrorCategory() {
        return errorCategory;
    }

    public void setErrorCategory(final Object errorCategory) {
        this.errorCategory = stringOrNull(errorCategory);
    }

    public String getMerchantMessage() {
        return merchantMessage;
    }

    public void setMerchantMessage(final Object merchantMessage) {
        this.merchantMessage = stringOrNull(merchantMessage);
    }

    public String getCustomerMessage() {
        return customerMessage;
    }

    public void setCustomerMessage(final Object customerMessage) {
        this.customerMessage = stringOrNull(customerMessage);
    }

    
}
