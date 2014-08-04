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

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import com.ning.billing.recurly.model.TestModelBase;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TestNewInvoiceNotification  extends TestModelBase {

    @Test(groups = "fast")
    public void testDeserialization() throws Exception {
        final String notificationData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<new_invoice_notification>\n" +
                "  <account>\n" +
                "    <account_code>1</account_code>\n" +
                "    <username>username</username>\n" +
                "    <email>email@example.com</email>\n" +
                "    <first_name>firstname</first_name>\n" +
                "    <last_name>lastname</last_name>\n" +
                "    <company_name>companyName</company_name>\n" +
                "  </account>\n" +
                "  <invoice>\n" +
                "    <uuid>ffc64d71d4b5404e93f13aac9c63b007</uuid>\n" +
                "    <subscription_id>subscriptionId</subscription_id>\n" +
                "    <state>open</state>\n" +
                "    <invoice_number type=\"integer\">1000</invoice_number>\n" +
                "    <po_number>poNumber</po_number>\n" +
                "    <vat_number>vatNumber</vat_number>\n" +
                "    <total_in_cents type=\"integer\">1100</total_in_cents>\n" +
                "    <currency>USD</currency>\n" +
                "    <date type=\"datetime\">2014-01-01T20:20:29Z</date>\n" +
                "    <closed_at type=\"datetime\">2014-01-01T20:24:02Z</closed_at>\n" +
                "    <net_terms type=\"integer\">0</net_terms>" +
                "    <collection_method>manual</collection_method>" +
                "  </invoice>\n" +
                "</new_invoice_notification>\n";
        final NewInvoiceNotification notification = xmlMapper.readValue(notificationData, NewInvoiceNotification.class);

        assertNotNull(notification);
        assertEquals(notification.getAccount().getAccountCode(), "1");
        assertEquals(notification.getAccount().getUsername(), "username");
        assertEquals(notification.getAccount().getEmail(), "email@example.com");
        assertEquals(notification.getAccount().getFirstName(), "firstname");
        assertEquals(notification.getAccount().getLastName(), "lastname");
        assertEquals(notification.getAccount().getCompanyName(), "companyName");
        assertEquals(notification.getInvoice().getUuid(), "ffc64d71d4b5404e93f13aac9c63b007");
        assertEquals(notification.getInvoice().getSubscriptionId(), "subscriptionId");
        assertEquals(notification.getInvoice().getState(), "open");
        assertEquals(notification.getInvoice().getInvoiceNumber(), (Integer)1000);
        assertEquals(notification.getInvoice().getPoNumber(), "poNumber");
        assertEquals(notification.getInvoice().getVatNumber(), "vatNumber");
        assertEquals(notification.getInvoice().getTotalInCents(), (Integer)1100);
        assertEquals(notification.getInvoice().getCurrency(), "USD");
        assertEquals(notification.getInvoice().getDate(), new DateTime("2014-01-01T20:20:29Z"));
        assertEquals(notification.getInvoice().getClosedAt(), new DateTime("2014-01-01T20:24:02Z"));
        assertEquals(notification.getInvoice().getNetTerms(), (Integer)0);
        assertEquals(notification.getInvoice().getCollectionMethod(), "manual");
    }
}
