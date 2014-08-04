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

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestTransaction extends TestModelBase {

    @Test(groups = "fast")
    public void testDeserialization() throws Exception {
        //See https://docs.recurly.com/api/transactions
        final String transactionData =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<transaction href=\"https://your-subdomain.recurly.com/v2/transactions/a13acd8fe4294916b79aec87b7ea441f\" type=\"credit_card\">\n" + 
                "  <account href=\"https://your-subdomain.recurly.com/v2/accounts/verena100\"/>\n" + 
                "  <invoice href=\"https://your-subdomain.recurly.com/v2/invoices/1108\"/>\n" + 
                "  <subscription href=\"https://your-subdomain.recurly.com/v2/subscriptions/17caaca1716f33572edc8146e0aaefde\"/>\n" + 
                "  <uuid>a13acd8fe4294916b79aec87b7ea441f</uuid>\n" + 
                "  <action>purchase</action>\n" + 
                "  <amount_in_cents type=\"integer\">1000</amount_in_cents>\n" + 
                "  <tax_in_cents type=\"integer\">0</tax_in_cents>\n" + 
                "  <currency>USD</currency>\n" + 
                "  <status>success</status>\n" + 
                "  <payment_method>credit_card</payment_method>\n" + 
                "  <reference nil=\"nil\"></reference>\n" + 
                "  <source>subscription</source>\n" + 
                "  <recurring type=\"boolean\">true</recurring>\n" + 
                "  <test type=\"boolean\">true</test>\n" + 
                "  <voidable type=\"boolean\">true</voidable>\n" + 
                "  <refundable type=\"boolean\">true</refundable>\n" + 
                "  <cvv_result code=\"\" nil=\"nil\"></cvv_result>\n" + 
                "  <avs_result code=\"\" nil=\"nil\"></avs_result>\n" + 
                "  <avs_result_street nil=\"nil\"></avs_result_street>\n" + 
                "  <avs_result_postal nil=\"nil\"></avs_result_postal>\n" + 
                "  <created_at type=\"datetime\">2011-06-27T12:34:56Z</created_at>\n" + 
                "  <details>\n" + 
                "    <account>\n" + 
                "      <account_code>verena100</account_code>\n" + 
                "      <first_name>Verena</first_name>\n" + 
                "      <last_name>Example</last_name>\n" + 
                "      <company nil=\"nil\"></company>\n" + 
                "      <email>verena@test.com</email>\n" + 
                "      <billing_info type=\"credit_card\">\n" + 
                "        <first_name nil=\"nil\"></first_name>\n" + 
                "        <last_name nil=\"nil\"></last_name>\n" + 
                "        <address1 nil=\"nil\"></address1>\n" + 
                "        <address2 nil=\"nil\"></address2>\n" + 
                "        <city nil=\"nil\"></city>\n" + 
                "        <state nil=\"nil\"></state>\n" + 
                "        <zip nil=\"nil\"></zip>\n" + 
                "        <country nil=\"nil\"></country>\n" + 
                "        <phone nil=\"nil\"></phone>\n" + 
                "        <vat_number nil=\"nil\"></vat_number>\n" + 
                "        <card_type>Visa</card_type>\n" + 
                "        <year type=\"integer\">2015</year>\n" + 
                "        <month type=\"integer\">11</month>\n" + 
                "        <first_six>411111</first_six>\n" + 
                "        <last_four>1111</last_four>\n" + 
                "      </billing_info>\n" + 
                "    </account>\n" + 
                "  </details>\n" + 
                "  <a name=\"refund\" href=\"http://api.test.host/v2/transactions/a13acd8fe4294916b79aec87b7ea441f\" method=\"delete\"/>\n" + 
                "</transaction>\n";

        final Transaction transaction = xmlMapper.readValue(transactionData, Transaction.class);

        Assert.assertNotNull(transaction);
        Assert.assertEquals(transaction.getUuid(), "a13acd8fe4294916b79aec87b7ea441f");
        Assert.assertEquals(transaction.getAction(), "purchase");
        Assert.assertEquals(transaction.getAmountInCents(), (Integer)1000);
        Assert.assertEquals(transaction.getTaxInCents(), (Integer)0);
        Assert.assertEquals(transaction.getCurrency(), "USD");
        Assert.assertEquals(transaction.getStatus(), "success");
        Assert.assertEquals(transaction.getPaymentMethod(), "credit_card");
        Assert.assertEquals(transaction.getReference(), null);
        Assert.assertEquals(transaction.getSource(), "subscription");
        Assert.assertEquals(transaction.getRecurring(), Boolean.TRUE);
        Assert.assertEquals(transaction.getTest(), Boolean.TRUE);
        Assert.assertEquals(transaction.getVoidable(), Boolean.TRUE);
        Assert.assertEquals(transaction.getRefundable(), Boolean.TRUE);
        Assert.assertEquals(transaction.getCreatedAt(), new DateTime("2011-06-27T12:34:56Z"));

        Assert.assertNotNull(transaction.getAccount());
        Assert.assertEquals(transaction.getAccount().getAccountCode(), "verena100");
    }
}
