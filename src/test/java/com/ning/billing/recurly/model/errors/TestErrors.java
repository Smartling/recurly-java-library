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

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.billing.recurly.model.TestModelBase;
import com.ning.billing.recurly.model.Transaction;

public class TestErrors extends TestModelBase {

    @Test(groups = "fast")
    public void testDeserialization() throws Exception {
        //See https://docs.recurly.com/api/transactions/error-codes
        final String errorsData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<errors>\n" + 
                "  <transaction_error>\n" + 
                "    <error_code>fraud_security_code</error_code>\n" + 
                "    <error_category>fraud</error_category>\n" + 
                "    <merchant_message>The payment gateway declined the transaction because the security code (CVV) did not match.</merchant_message>\n" + 
                "    <customer_message>The security code you entered does not match. Please update the CVV and try again.</customer_message>\n" + 
                "  </transaction_error>\n" + 
                "  <error field=\"transaction.account.billing_info.verification_value\" symbol=\"declined_bad\">did not match</error>\n" + 
                "  <transaction href=\"https://your-subdomain.recurly.com/v2/transactions/3d1c6aa86e3d447eb0f3b4a6e3e074d9\" type=\"credit_card\">\n" + 
                "    <uuid>3d1c6aa86e3d447eb0f3b4a6e3e074d9</uuid>\n" + 
                "    <action>purchase</action>\n" + 
                "    <amount_in_cents type=\"integer\">4900</amount_in_cents>\n" + 
                "    <tax_in_cents type=\"integer\">0</tax_in_cents>\n" + 
                "    <currency>USD</currency>\n" + 
                "    <status>declined</status>\n" + 
                "    <reference nil=\"nil\"></reference>\n" + 
                "    <test type=\"boolean\">true</test>\n" + 
                "    <voidable type=\"boolean\">false</voidable>\n" + 
                "    <refundable type=\"boolean\">false</refundable>\n" + 
                "    <transaction_error>\n" + 
                "      <error_code>fraud_security_code</error_code>\n" + 
                "      <error_category>fraud</error_category>\n" + 
                "      <merchant_message>The payment gateway declined the transaction because the security code (CVV) did not match.</merchant_message>\n" + 
                "      <customer_message>The security code you entered does not match. Please update the CVV and try again.</customer_message>\n" + 
                "    </transaction_error>\n" + 
                "    <cvv_result code=\"N\">No Match</cvv_result>\n" + 
                "    <avs_result code=\"D\">Street address and postal code match.</avs_result>\n" + 
                "    <avs_result_street>Y</avs_result_street>\n" + 
                "    <avs_result_postal>Y</avs_result_postal>\n" + 
                "    <created_at type=\"datetime\">2011-10-17T17:24:53-07:00</created_at>\n" + 
                "    <details>\n" + 
                "      <account>\n" + 
                "        <account_code>1</account_code>\n" + 
                "        <first_name nil=\"nil\"></first_name>\n" + 
                "        <last_name nil=\"nil\"></last_name>\n" + 
                "        <company nil=\"nil\"></company>\n" + 
                "        <email>verena@example.com</email>\n" + 
                "        <billing_info type=\"credit_card\">\n" + 
                "          <first_name nil=\"nil\"></first_name>\n" + 
                "          <last_name nil=\"nil\"></last_name>\n" + 
                "          <address1 nil=\"nil\"></address1>\n" + 
                "          <address2 nil=\"nil\"></address2>\n" + 
                "          <city nil=\"nil\"></city>\n" + 
                "          <state nil=\"nil\"></state>\n" + 
                "          <zip nil=\"nil\"></zip>\n" + 
                "          <country nil=\"nil\"></country>\n" + 
                "          <phone nil=\"nil\"></phone>\n" + 
                "          <vat_number nil=\"nil\"></vat_number>\n" + 
                "          <card_type>Visa</card_type>\n" + 
                "          <year type=\"integer\">2015</year>\n" + 
                "          <month type=\"integer\">11</month>\n" + 
                "          <first_six>400000</first_six>\n" + 
                "          <last_four>0101</last_four>\n" + 
                "        </billing_info>\n" + 
                "      </account>\n" + 
                "    </details>\n" + 
                "  </transaction>\n" + 
                "</errors>\n";

        final Errors errors = xmlMapper.readValue(errorsData, Errors.class);

        Assert.assertNotNull(errors);

        verifyTransactionError(errors.getTransactionError());

        Assert.assertNotNull(errors.getRecurlyErrors());
        Assert.assertEquals(errors.getRecurlyErrors().size(), 1);

        final RecurlyError recurlyError = errors.getRecurlyErrors().get(0);
        Assert.assertNotNull(recurlyError);
        Assert.assertEquals(recurlyError.getField(), "transaction.account.billing_info.verification_value");
        Assert.assertEquals(recurlyError.getSymbol(), "declined_bad");
        Assert.assertEquals(recurlyError.getValue(), "did not match");

        final Transaction transaction = errors.getTransaction();
        Assert.assertNotNull(transaction);
        Assert.assertEquals(transaction.getUuid(), "3d1c6aa86e3d447eb0f3b4a6e3e074d9");
        Assert.assertEquals(transaction.getAction(), "purchase");
        Assert.assertEquals(transaction.getAmountInCents(), (Integer) 4900);
        Assert.assertEquals(transaction.getTaxInCents(), (Integer) 0);
        Assert.assertEquals(transaction.getCurrency(), "USD");
        Assert.assertEquals(transaction.getStatus(), "declined");
        Assert.assertEquals(transaction.getReference(), null);
        Assert.assertEquals(transaction.getTest(), Boolean.TRUE);
        Assert.assertEquals(transaction.getVoidable(), Boolean.FALSE);
        Assert.assertEquals(transaction.getRefundable(), Boolean.FALSE);
        
        verifyTransactionError(transaction.getTransactionError());
        Assert.assertEquals(transaction.getCreatedAt(), new DateTime("2011-10-17T17:24:53-07:00"));
    }

    @Test(groups = "fast")
    public void testDeserializationValidationErrors() throws Exception {
        //See https://docs.recurly.com/api/basics/validation-errors
        final String errorsData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<errors>\n" + 
                "  <error field=\"model_name.field_name\" symbol=\"not_a_number\" lang=\"en-US\">is not a number</error>\n" + 
                "</errors>\n";
        final Errors errors = xmlMapper.readValue(errorsData, Errors.class);

        Assert.assertNotNull(errors);
        Assert.assertNotNull(errors.getRecurlyErrors());
        Assert.assertEquals(errors.getRecurlyErrors().size(), 1);

        final RecurlyError recurlyError = errors.getRecurlyErrors().get(0);
        Assert.assertNotNull(recurlyError);
        Assert.assertEquals(recurlyError.getField(), "model_name.field_name");
        Assert.assertEquals(recurlyError.getSymbol(), "not_a_number");
        Assert.assertEquals(recurlyError.getValue(), "is not a number");
    }

    private void verifyTransactionError(TransactionError transactionError) {
        Assert.assertNotNull(transactionError);
        Assert.assertEquals(transactionError.getCustomerMessage(),
                "The security code you entered does not match. Please update the CVV and try again.");
        Assert.assertEquals(transactionError.getErrorCategory(), "fraud");
        Assert.assertEquals(transactionError.getErrorCode(), "fraud_security_code");
        Assert.assertEquals(transactionError.getMerchantMessage(),
                "The payment gateway declined the transaction because the security code (CVV) did not match.");
    }
}
