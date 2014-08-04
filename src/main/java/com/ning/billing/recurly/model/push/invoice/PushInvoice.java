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

package com.ning.billing.recurly.model.push.invoice;

import javax.xml.bind.annotation.XmlElement;

import org.joda.time.DateTime;

import com.ning.billing.recurly.model.AbstractInvoice;

public class PushInvoice extends AbstractInvoice {
    @XmlElement(name = "subscription_id")
    private String subscriptionId;

    @XmlElement(name = "date")
    private DateTime date;

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(final Object subscriptionId) {
        this.subscriptionId = stringOrNull(subscriptionId);
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(final Object date) {
        this.date = dateTimeOrNull(date);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (null != date ? date.hashCode() : 0);
        result = prime * result + (null != subscriptionId ? subscriptionId.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PushInvoice)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }

        final PushInvoice that = (PushInvoice) obj;

        if (null != date ? !date.equals(that.date) : null != date) {
            return false;
        }
        if (null != subscriptionId ? !subscriptionId.equals(that.subscriptionId) : null != subscriptionId) {
            return false;
        }

        return true;
    }

}
