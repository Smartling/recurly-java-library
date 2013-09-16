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

package com.ning.billing.recurly.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.DateTime;

@XmlRootElement(name = "transaction")
public class Transaction extends AbstractTransaction {

    public static final String STATE_PARAMETER_NAME = "state";
    public static enum State
    {
        SUCCESSFUL, FAILED, VOIDED;

        private static final String SUCCESSFUL_PARAMETER_VALUE = "successful";
        private static final String FAILED_PARAMETER_VALUE = "failed";
        private static final String VOIDED_PARAMETER_VALUE = "voided";

        public static String getStringValue(State state)
        {
            switch(state)
            {
                case SUCCESSFUL:
                    return SUCCESSFUL_PARAMETER_VALUE;
                case FAILED:
                    return FAILED_PARAMETER_VALUE;
                case VOIDED:
                    return VOIDED_PARAMETER_VALUE;
                default:
                    return null;
            }
        }
    };

    public static final String TYPE_PARAMETER_NAME = "type";

    public static enum Type
    {
        AUTHORIZATION, REFUND, PURCHASE;

        private static final String AUTHORIZATION_PARAMETER_VALUE = "authorization";
        private static final String REFUND_PARAMETER_VALUE = "refund";
        private static final String PURCHASE_PARAMETER_VALUE = "purchase";

        public static String getStringValue(Type type)
        {
            switch(type)
            {
                case AUTHORIZATION:
                    return AUTHORIZATION_PARAMETER_VALUE;
                case REFUND:
                    return REFUND_PARAMETER_VALUE;
                case PURCHASE:
                    return PURCHASE_PARAMETER_VALUE;
                default:
                    return null;
            }
        }
    };

    @XmlElement(name = "account")
    private Account account;

    @XmlElement(name = "invoice")
    private Invoice invoice;

    @XmlElement(name = "subscription")
    private String subscription;

    @XmlElement(name = "uuid")
    private String uuid;

    @XmlElement(name = "tax_in_cents")
    private Integer taxInCents;

    @XmlElement(name = "currency")
    private String currency;

    @XmlElement(name = "created_at")
    private DateTime createdAt;

    @XmlElement(name = "description")
    private String description;

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(final Invoice invoice) {
        this.invoice = invoice;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(final Object subscription) {
        this.subscription = stringOrNull(subscription);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final Object uuid) {
        this.uuid = stringOrNull(uuid);
    }

    public Integer getTaxInCents() {
        return taxInCents;
    }

    public void setTaxInCents(final Object taxInCents) {
        this.taxInCents = integerOrNull(taxInCents);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final Object currency) {
        this.currency = stringOrNull(currency);
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Object createdAt) {
        this.createdAt = dateTimeOrNull(createdAt);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Transaction");
        sb.append("{account=").append(account);
        sb.append(", invoice=").append(invoice);
        sb.append(", subscription='").append(subscription).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", action='").append(action).append('\'');
        sb.append(", amountInCents=").append(amountInCents);
        sb.append(", taxInCents=").append(taxInCents);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", reference='").append(reference).append('\'');
        sb.append(", test=").append(test);
        sb.append(", voidable=").append(voidable);
        sb.append(", refundable=").append(refundable);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", description=").append(description);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final Transaction that = (Transaction) o;

        if (account != null ? !account.equals(that.account) : that.account != null) {
            return false;
        }
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) {
            return false;
        }
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) {
            return false;
        }
        if (invoice != null ? !invoice.equals(that.invoice) : that.invoice != null) {
            return false;
        }
        if (subscription != null ? !subscription.equals(that.subscription) : that.subscription != null) {
            return false;
        }
        if (taxInCents != null ? !taxInCents.equals(that.taxInCents) : that.taxInCents != null) {
            return false;
        }
        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (account != null ? account.hashCode() : 0);
        result = 31 * result + (invoice != null ? invoice.hashCode() : 0);
        result = 31 * result + (subscription != null ? subscription.hashCode() : 0);
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (taxInCents != null ? taxInCents.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }
}
