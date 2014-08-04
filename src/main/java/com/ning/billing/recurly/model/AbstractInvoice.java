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

import org.joda.time.DateTime;

@XmlRootElement(name = "invoice")
public class AbstractInvoice extends RecurlyObject {
    @XmlElement(name = "uuid")
    private String uuid;

    @XmlElement(name = "state")
    private String state;

    @XmlElement(name = "invoice_number")
    private Integer invoiceNumber;

    @XmlElement(name = "po_number")
    private String poNumber;

    @XmlElement(name = "vat_number")
    private String vatNumber;

    @XmlElement(name = "total_in_cents")
    private Integer totalInCents;

    @XmlElement(name = "currency")
    private String currency;

    @XmlElement(name = "net_terms")
    private Integer netTerms;

    @XmlElement(name = "collection_method")
    private String collectionMethod;

    @XmlElement(name = "closed_at")
    private DateTime closedAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final Object uuid) {
        this.uuid = stringOrNull(uuid);
    }

    public String getState() {
        return state;
    }

    public void setState(final Object state) {
        this.state = stringOrNull(state);
    }

    public Integer getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(final Object invoiceNumber) {
        this.invoiceNumber = integerOrNull(invoiceNumber);
    }

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(final Object poNumber) {
        this.poNumber = stringOrNull(poNumber);
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(final Object vatNumber) {
        this.vatNumber = stringOrNull(vatNumber);
    }

    public Integer getTotalInCents() {
        return totalInCents;
    }

    public void setTotalInCents(final Object totalInCents) {
        this.totalInCents = integerOrNull(totalInCents);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final Object currency) {
        this.currency = stringOrNull(currency);
    }

    public Integer getNetTerms() {
        return netTerms;
    }

    public void setNetTerms(final Object netTerms) {
        this.netTerms = integerOrNull(netTerms);
    }

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(final Object collectionMethod) {
        this.collectionMethod = stringOrNull(collectionMethod);
    }

    public DateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(final Object closedAt) {
        this.closedAt = dateTimeOrNull(closedAt);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = null != closedAt ? closedAt.hashCode() : 0;
        result = prime * result + (null != collectionMethod ? collectionMethod.hashCode() : 0);
        result = prime * result + (null != currency ? currency.hashCode() : 0);
        result = prime * result + (null != invoiceNumber ? invoiceNumber.hashCode() : 0);
        result = prime * result + (null != netTerms ? netTerms.hashCode() : 0);
        result = prime * result + (null != poNumber ? poNumber.hashCode() : 0);
        result = prime * result + (null != state ? state.hashCode() : 0);
        result = prime * result + (null != totalInCents ? totalInCents.hashCode() : 0);
        result = prime * result + (null != uuid ? uuid.hashCode() : 0);
        result = prime * result + (null != vatNumber ? vatNumber.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractInvoice))
            return false;

        final AbstractInvoice that = (AbstractInvoice) obj;

        if (null != closedAt ? !closedAt.equals(that.closedAt) : null != that.closedAt) {
            return false;
        }
        if (null != collectionMethod ? !collectionMethod.equals(that.collectionMethod) : null != that.collectionMethod) {
            return false;
        }
        if (null != currency ? !currency.equals(that.currency) : null != that.currency) {
            return false;
        }
        if (null != invoiceNumber ? !invoiceNumber.equals(that.invoiceNumber) : null != that.invoiceNumber) {
            return false;
        }
        if (null != netTerms ? !netTerms.equals(that.netTerms) : null != that.netTerms) {
            return false;
        }
        if (null != poNumber ? !poNumber.equals(that.poNumber) : null != that.poNumber) {
            return false;
        }
        if (null != state ? !state.equals(that.state) : null != that.state) {
            return false;
        }
        if (null != totalInCents ? !totalInCents.equals(that.totalInCents) : null != that.totalInCents) {
            return false;
        }
        if (null != uuid ? !uuid.equals(that.uuid) : null != that.uuid) {
            return false;
        }
        if (null != vatNumber ? !vatNumber.equals(that.vatNumber) : null != that.vatNumber) {
            return false;
        }
        if (null != state ? !state.equals(that.state) : null != that.state) {
            return false;
        }

        return true;
    }

}
