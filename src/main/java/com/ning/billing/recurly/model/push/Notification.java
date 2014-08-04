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

package com.ning.billing.recurly.model.push;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.ning.billing.recurly.model.RecurlyObject;
import com.ning.billing.recurly.model.push.account.BillingInfoUpdatedNotification;
import com.ning.billing.recurly.model.push.account.CanceledAccountNotification;
import com.ning.billing.recurly.model.push.account.NewAccountNotification;
import com.ning.billing.recurly.model.push.invoice.ClosedInvoiceNotification;
import com.ning.billing.recurly.model.push.invoice.NewInvoiceNotification;
import com.ning.billing.recurly.model.push.invoice.PastDueInvoiceNotification;
import com.ning.billing.recurly.model.push.payment.FailedPaymentNotification;
import com.ning.billing.recurly.model.push.payment.SuccessfulPaymentNotification;
import com.ning.billing.recurly.model.push.payment.SuccessfulRefundNotification;
import com.ning.billing.recurly.model.push.payment.VoidedPaymentNotification;
import com.ning.billing.recurly.model.push.subscription.CanceledSubscriptionNotification;
import com.ning.billing.recurly.model.push.subscription.ExpiredSubscriptionNotification;
import com.ning.billing.recurly.model.push.subscription.NewSubscriptionNotification;
import com.ning.billing.recurly.model.push.subscription.ReactivatedAccountNotification;
import com.ning.billing.recurly.model.push.subscription.RenewedSubscriptionNotification;
import com.ning.billing.recurly.model.push.subscription.UpdatedSubscriptionNotification;

public abstract class Notification extends RecurlyObject {

    private static Logger log = LoggerFactory.getLogger(Notification.class);
    private static Pattern ROOT_NAME = Pattern.compile("<([0-9A-Z_a-z]*_notification)>");

    public static enum Type {
        BillingInfoUpdatedNotification(BillingInfoUpdatedNotification.class),
        CanceledAccountNotification(CanceledAccountNotification.class),
        NewAccountNotification(NewAccountNotification.class),
        FailedPaymentNotification(FailedPaymentNotification.class),
        SuccessfulPaymentNotification(SuccessfulPaymentNotification.class),
        SuccessfulRefundNotification(SuccessfulRefundNotification.class),
        VoidedPaymentNotification(VoidedPaymentNotification.class),
        CanceledSubscriptionNotification(CanceledSubscriptionNotification.class),
        ExpiredSubscriptionNotification(ExpiredSubscriptionNotification.class),
        NewSubscriptionNotification(NewSubscriptionNotification.class),
        ReactivatedAccountNotification(ReactivatedAccountNotification.class),
        RenewedSubscriptionNotification(RenewedSubscriptionNotification.class),
        UpdatedSubscriptionNotification(UpdatedSubscriptionNotification.class),
        NewInvoiceNotification(NewInvoiceNotification.class),
        ClosedInvoiceNotification(ClosedInvoiceNotification.class),
        PastDueInvoiceNotification(PastDueInvoiceNotification.class);

        private Class<? extends Notification> javaType;

        private Type(final Class<? extends Notification> javaType) {
            this.javaType = javaType;
        }

        public Class<? extends Notification> getJavaType() {
            return javaType;
        }

        public void setJavaType(final Class<? extends Notification> javaType) {
            this.javaType = javaType;
        }
    }

    public static <T> T read(final String payload, final Class<T> clazz) {
        try {
            // TODO Should we cache the mapper?
            return RecurlyObject.newXmlMapper().readValue(payload, clazz);
        } catch (IOException e) {
            log.warn("Enable to read notification, de-serialization failed : {}", e.getMessage());
            return null;
        }
    }

    /**
     * Detect notification type based on the xml root name.
     *
     * @param payload
     * @return notification type or null if root name is not found or if there
     *         is no type corresponding to the root name
     */
    public static Type detect(final String payload) {
        final Matcher m = ROOT_NAME.matcher(payload);
        if (m.find() && m.groupCount() >= 1) {
            final String root = m.group(1);
            try {
                return Type.valueOf(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, root));
            } catch (IllegalArgumentException e) {
                log.warn("Enable to detect notification type, no type for {}", root);
                return null;
            }
        }
        log.warn("Enable to detect notification type");
        return null;
    }
}
