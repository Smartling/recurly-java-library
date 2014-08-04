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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import javax.xml.bind.annotation.XmlTransient;
import org.joda.time.DateTime;

public class Invoice extends AbstractInvoice {

    @XmlTransient
    private String href;

    @XmlElement(name = "account")
    private Account account;

    @XmlElement(name = "subtotal_in_cents")
    private Integer subtotalInCents;

    @XmlElement(name = "tax_in_cents")
    private Integer taxInCents;

    @XmlElement(name = "created_at")
    private DateTime createdAt;

    @XmlElementWrapper(name = "line_items")
    @XmlElement(name = "adjustment")
    private Adjustments adjustments;

    @XmlElementWrapper(name = "transactions")
    @XmlElement(name = "transaction")
    private Transactions transactions;

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

    public Integer getSubtotalInCents() {
        return subtotalInCents;
    }

    public void setSubtotalInCents(final Object subtotalInCents) {
        this.subtotalInCents = integerOrNull(subtotalInCents);
    }

    public Integer getTaxInCents() {
        return taxInCents;
    }

    public void setTaxInCents(final Object taxInCents) {
        this.taxInCents = integerOrNull(taxInCents);
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final Object createdAt) {
        this.createdAt = dateTimeOrNull(createdAt);
    }

    public Adjustments getLineItems() {
        return adjustments;
    }

    public void setLineItems(final Adjustments adjustments) {
        this.adjustments = adjustments;
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public void setTransactions(final Transactions transactions) {
        this.transactions = transactions;
    }

    @JsonIgnore
    public String getHref() {
        return href;
    }

    public void setHref(final Object href) {
        this.href = stringOrNull(href);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invoice");
        sb.append("{account=").append(account);
        sb.append(", uuid='").append(getUuid()).append('\'');
        sb.append(", state='").append(getState()).append('\'');
        sb.append(", invoiceNumber=").append(getInvoiceNumber());
        sb.append(", poNumber=").append(getPoNumber());
        sb.append(", vatNumber=").append(getVatNumber());
        sb.append(", subtotalInCents=").append(subtotalInCents);
        sb.append(", taxInCents=").append(taxInCents);
        sb.append(", totalInCents=").append(getTotalInCents());
        sb.append(", currency='").append(getCurrency()).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append(", lineItems=").append(adjustments);
        sb.append(", transactions=").append(transactions);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Invoice)) {
            return false;
        }

        if(!super.equals(o)) {
            return false;
        }

        final Invoice invoice = (Invoice) o;

        if (account != null ? !account.equals(invoice.account) : invoice.account != null) {
            return false;
        }
        if (createdAt != null ? !createdAt.equals(invoice.createdAt) : invoice.createdAt != null) {
            return false;
        }
        if (adjustments != null ? !adjustments.equals(invoice.adjustments) : invoice.adjustments != null) {
            return false;
        }
        if (subtotalInCents != null ? !subtotalInCents.equals(invoice.subtotalInCents) : invoice.subtotalInCents != null) {
            return false;
        }
        if (taxInCents != null ? !taxInCents.equals(invoice.taxInCents) : invoice.taxInCents != null) {
            return false;
        }
        if (transactions != null ? !transactions.equals(invoice.transactions) : invoice.transactions != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (account != null ? account.hashCode() : 0);
        result = prime * result + (subtotalInCents != null ? subtotalInCents.hashCode() : 0);
        result = prime * result + (taxInCents != null ? taxInCents.hashCode() : 0);
        result = prime * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = prime * result + (adjustments != null ? adjustments.hashCode() : 0);
        result = prime * result + (transactions != null ? transactions.hashCode() : 0);
        return result;
    }
}
