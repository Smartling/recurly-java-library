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

package com.ning.billing.recurly;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;

import com.ning.billing.recurly.exceptions.RequestException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.billing.recurly.model.Account;
import com.ning.billing.recurly.model.Accounts;
import com.ning.billing.recurly.model.AddOn;
import com.ning.billing.recurly.model.BillingInfo;
import com.ning.billing.recurly.model.Coupon;
import com.ning.billing.recurly.model.Coupons;
import com.ning.billing.recurly.model.Invoice;
import com.ning.billing.recurly.model.Invoices;
import com.ning.billing.recurly.model.Plan;
import com.ning.billing.recurly.model.Plans;
import com.ning.billing.recurly.model.RecurlyObject;
import com.ning.billing.recurly.model.Subscription;
import com.ning.billing.recurly.model.SubscriptionUpdate;
import com.ning.billing.recurly.model.Subscriptions;
import com.ning.billing.recurly.model.Transaction;
import com.ning.billing.recurly.model.Transactions;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class RecurlyClient {

    private static final Logger log = LoggerFactory.getLogger(RecurlyClient.class);

    public static final String RECURLY_PAGE_SIZE_KEY = "recurly.page.size";

    private static final Integer DEFAULT_PAGE_SIZE = 20;
    private static final String PER_PAGE = "per_page=";

    public static final String FETCH_RESOURCE = "/recurly_js/result";

    private static final String PAGING_ITEMS_PER_PAGE_PARAMETER_NAME = "per_page";
    private static final String PARAMETER_DELIMITER = "&";

    private class UrlParameterList
    {
        private List<String> parameterListHolder;

        public UrlParameterList()
        {
            parameterListHolder = new ArrayList<String>();
        }

        public void addParameter(String parameterName,String parameterValue)
        {
            parameterListHolder.add(parameterName + "=" + parameterValue);
        }

        public String getUrlParameterString()
        {
            boolean isItFirst = true;
            StringBuilder parameters = new StringBuilder();
            for (String parameterNameValuePair : parameterListHolder)
            {
                if (!isItFirst)
                    parameters.append(PARAMETER_DELIMITER);
                parameters.append(parameterNameValuePair);
                isItFirst = false;
            }

            return parameters.toString();
        }
    }


    /**
     * Returns the page Size to use when querying. The page size
     * is set as System.property: recurly.page.size
     */
    public static Integer getPageSize() {
        Integer pageSize;
        try {
            pageSize = new Integer(System.getProperty(RECURLY_PAGE_SIZE_KEY));
        } catch (NumberFormatException nfex) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public static String getPageSizeGetParam() {
        return PER_PAGE + getPageSize().toString();
    }

    // TODO: should we make it static?
    private final XmlMapper xmlMapper;

    private final String key;
    private final String baseUrl;
    private AsyncHttpClient client;

    public RecurlyClient(final String apiKey) {
        this(apiKey, "api.recurly.com", 443, "v2");
    }

    public RecurlyClient(final String apiKey, final String host, final int port, final String version) {
        this.key = DatatypeConverter.printBase64Binary(apiKey.getBytes());
        this.baseUrl = String.format("https://%s:%d/%s", host, port, version);
        this.xmlMapper = RecurlyObject.newXmlMapper();
    }

    /**
     * Open the underlying http client
     */
    public synchronized void open() {
        client = createHttpClient();
    }

    /**
     * Close the underlying http client
     */
    public synchronized void close() {
        if (client != null) {
            client.close();
        }
    }

    /**
     * Create Account
     * <p/>
     * Creates a new account. You may optionally include billing information.
     *
     * @param account account object
     * @return the newly created account object on success, null otherwise
     */
    public Account createAccount(final Account account)
            throws InterruptedException, ExecutionException, IOException, RequestException {
        return doPOST(Account.ACCOUNT_RESOURCE, account, Account.class);
    }

    /**
     * Get Accounts
     * <p/>
     * Returns information about all accounts.
     *
     * @return account object on success, null otherwise
     */
    public Accounts getAccounts() throws IOException, RequestException {
        return doGET(Accounts.ACCOUNTS_RESOURCE, Accounts.class);
    }

    public Coupons getCoupons() throws IOException, RequestException {
        return doGET(Coupons.COUPONS_RESOURCE, Coupons.class);
    }

    /**
     * Get Account
     * <p/>
     * Returns information about a single account.
     *
     * @param accountCode recurly account id
     * @return account object on success, null otherwise
     */
    public Account getAccount(final String accountCode) throws IOException, RequestException {
        return doGET(Account.ACCOUNT_RESOURCE + "/" + accountCode, Account.class);
    }

    /**
     * Update Account
     * <p/>
     * Updates an existing account.
     *
     * @param accountCode recurly account id
     * @param account     account object
     * @return the updated account object on success, null otherwise
     */
    public Account updateAccount(final String accountCode, final Account account) throws IOException, RequestException {
        return doPUT(Account.ACCOUNT_RESOURCE + "/" + accountCode, account, Account.class);
    }

    /**
     * Close Account
     * <p/>
     * Marks an account as closed and cancels any active subscriptions. Any saved billing information will also be
     * permanently removed from the account.
     *
     * @param accountCode recurly account id
     */
    public void closeAccount(final String accountCode) throws IOException, RequestException {
        doDELETE(Account.ACCOUNT_RESOURCE + "/" + accountCode);
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a subscription
     * <p/>
     * Creates a subscription for an account.
     *
     * @param subscription Subscription object
     * @return the newly created Subscription object on success, null otherwise
     */
    public Subscription createSubscription(final Subscription subscription)
            throws InterruptedException, ExecutionException, IOException, RequestException {
        return doPOST(Subscription.SUBSCRIPTION_RESOURCE,
                      subscription, Subscription.class);
    }

    /**
     * Get a particular {@link Subscription} by it's UUID
     * <p/>
     * Returns information about a single account.
     *
     * @param uuid UUID of the subscription to lookup
     * @return Subscriptions for the specified user
     */
    public Subscription getSubscription(final String uuid) throws IOException, RequestException {
        return doGET(Subscriptions.SUBSCRIPTIONS_RESOURCE
                     + "/" + uuid,
                     Subscription.class);
    }

    /**
     * Cancel a subscription
     * <p/>
     * Cancel a subscription so it remains active and then expires at the end of the current bill cycle.
     *
     * @param subscription Subscription object
     * @return -?-
     */
    public Subscription cancelSubscription(final Subscription subscription) throws IOException, RequestException {
        return doPUT(Subscription.SUBSCRIPTION_RESOURCE + "/" + subscription.getUuid() + "/cancel",
                     subscription, Subscription.class);
    }

    /**
     * Reactivating a canceled subscription
     * <p/>
     * Reactivate a canceled subscription so it renews at the end of the current bill cycle.
     *
     * @param subscription Subscription object
     * @return -?-
     */
    public Subscription reactivateSubscription(final Subscription subscription) throws IOException, RequestException {
        return doPUT(Subscription.SUBSCRIPTION_RESOURCE + "/" + subscription.getUuid() + "/reactivate",
                     subscription, Subscription.class);
    }

    /**
     * Update a particular {@link Subscription} by it's UUID
     * <p/>
     * Returns information about a single account.
     *
     * @param uuid UUID of the subscription to update
     * @return Subscription the updated subscription
     */
    public Subscription updateSubscription(final String uuid, final SubscriptionUpdate subscriptionUpdate)
            throws IOException, RequestException {
        return doPUT(Subscriptions.SUBSCRIPTIONS_RESOURCE
                     + "/" + uuid,
                     subscriptionUpdate,
                     Subscription.class);
    }

    /**
     * Get the subscriptions for an {@link Account}.
     * <p/>
     * Returns information about a single {@link Account}.
     *
     * @param accountCode recurly account id
     * @return Subscriptions for the specified user
     */
    public Subscriptions getAccountSubscriptions(final String accountCode) throws IOException, RequestException {
        return doGET(Account.ACCOUNT_RESOURCE
                     + "/" + accountCode
                     + Subscriptions.SUBSCRIPTIONS_RESOURCE,
                     Subscriptions.class);
    }

    /**
     * Get the subscriptions for an account.
     * <p/>
     * Returns information about a single account.
     *
     * @param accountCode recurly account id
     * @param status      Only accounts in this status will be returned
     * @return Subscriptions for the specified user
     */
    public Subscriptions getAccountSubscriptions(final String accountCode, final String status)
            throws IOException, RequestException {
        return doGET(Account.ACCOUNT_RESOURCE
                     + "/" + accountCode
                     + Subscriptions.SUBSCRIPTIONS_RESOURCE
                     + "?state="
                     + status,
                     Subscriptions.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Update an account's billing info
     * <p/>
     * When new or updated credit card information is updated, the billing information is only saved if the credit card
     * is valid. If the account has a past due invoice, the outstanding balance will be collected to validate the
     * billing information.
     * <p/>
     * If the account does not exist before the API request, the account will be created if the billing information
     * is valid.
     * <p/>
     * Please note: this API end-point may be used to import billing information without security codes (CVV).
     * Recurly recommends requiring CVV from your customers when collecting new or updated billing information.
     *
     * @param billingInfo billing info object to create or update
     * @return the newly created or update billing info object on success, null otherwise
     */
    public BillingInfo createOrUpdateBillingInfo(final BillingInfo billingInfo) throws IOException, RequestException {
        final String accountCode = billingInfo.getAccount().getAccountCode();
        // Unset it to avoid confusing Recurly
        billingInfo.setAccount(null);
        return doPUT(Account.ACCOUNT_RESOURCE + "/" + accountCode + BillingInfo.BILLING_INFO_RESOURCE,
                     billingInfo, BillingInfo.class);
    }

    /**
     * Lookup an account's billing info
     * <p/>
     * Returns only the account's current billing information.
     *
     * @param accountCode recurly account id
     * @return the current billing info object associated with this account on success, null otherwise
     */
    public BillingInfo getBillingInfo(final String accountCode) throws IOException, RequestException {
        return doGET(Account.ACCOUNT_RESOURCE + "/" + accountCode + BillingInfo.BILLING_INFO_RESOURCE,
                     BillingInfo.class);
    }

    /**
     * Clear an account's billing info
     * <p/>
     * You may remove any stored billing information for an account. If the account has a subscription, the renewal will
     * go into past due unless you update the billing info before the renewal occurs
     *
     * @param accountCode recurly account id
     */
    public void clearBillingInfo(final String accountCode) throws IOException, RequestException {
        doDELETE(Account.ACCOUNT_RESOURCE + "/" + accountCode + BillingInfo.BILLING_INFO_RESOURCE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // User transactions

    /**
     * Lookup an account's transactions history
     * <p/>
     * Returns the account's transaction history
     *
     * @param accountCode recurly account id
     * @return the transaction history associated with this account on success, null otherwise
     */
    public Transactions getAccountTransactions(final String accountCode) throws IOException, RequestException {
        return doGET(Accounts.ACCOUNTS_RESOURCE + "/" + accountCode + Transactions.TRANSACTIONS_RESOURCE,
                     Transactions.class);
    }

    /**
     * Lookup an account's transactions history by type and state
     * <p/>
     * Returns the account's transaction history
     *
     * @param accountCode recurly account id
     * @return the transaction history associated with this account on success, null otherwise
     */
    public Transactions getAccountTransactions(final String accountCode, final TransactionFilter transactionFilter, PagingParams pagingParams) throws IOException, RequestException {

        UrlParameterList urlParameterList = new UrlParameterList();

        if (null != transactionFilter)
        {
            if (null != transactionFilter.getState())
                urlParameterList.addParameter(Transaction.STATE_PARAMETER_NAME, Transaction.State.getStringValue(transactionFilter.getState()));

            if (null != transactionFilter.getType())
                urlParameterList.addParameter(Transaction.TYPE_PARAMETER_NAME , Transaction.Type.getStringValue(transactionFilter.getType()));
        }

        if (null != pagingParams)
        {
            urlParameterList.addParameter(PAGING_ITEMS_PER_PAGE_PARAMETER_NAME, Integer.toString(pagingParams.getItemsPerPage()));
//            urlParameterList.addParameter(PAGING_PAGE_ID_PARAMETER_NAME, Integer.toString(pagingParams.getPageId()));
        }
        String parameters = urlParameterList.getUrlParameterString();

        return doGET(Accounts.ACCOUNTS_RESOURCE + "/" + accountCode + Transactions.TRANSACTIONS_RESOURCE, parameters, Transactions.class);
    }

    /**
     * Creates a {@link Transaction} throgh the Recurly API.
     *
     * @param trans The {@link Transaction} to create
     * @return The created {@link Transaction} object
     */
    public Transaction createTransaction(final Transaction trans)
            throws InterruptedException, ExecutionException, IOException, RequestException {
        return doPOST(Transactions.TRANSACTIONS_RESOURCE, trans, Transaction.class);
    }

    ///////////////////////////////////////////////////////////////////////////
    // User invoices

    /**
     * Lookup an account's invoices
     * <p/>
     * Returns the account's invoices
     *
     * @param accountCode recurly account id
     * @return the invoices associated with this account on success, null otherwise
     */
    public Invoices getAccountInvoices(final String accountCode) throws IOException, RequestException {
        return doGET(Accounts.ACCOUNTS_RESOURCE + "/" + accountCode + Invoices.INVOICES_RESOURCE,
                     Invoices.class);
    }

    /**
     * Lookup the invoice's details
     * <p/>
     * Returns the invoice's details
     *
     * @param invoiceId recurly invoice id
     * @return the invoices associated with this account on success, null otherwise
     */
    public Invoice getInvoice(final Integer invoiceId) throws IOException, RequestException {
        return doGET(Invoices.INVOICES_RESOURCE + "/" + invoiceId.toString(),
                Invoice.class);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a Plan's info
     * <p/>
     *
     * @param plan The plan to create on recurly
     * @return the plan object as identified by the passed in ID
     */
    public Plan createPlan(final Plan plan)
            throws InterruptedException, ExecutionException, IOException, RequestException {
        return doPOST(Plan.PLANS_RESOURCE, plan, Plan.class);
    }

    /**
     * Get a Plan's details
     * <p/>
     *
     * @param planCode recurly id of plan
     * @return the plan object as identified by the passed in ID
     */
    public Plan getPlan(final String planCode) throws IOException, RequestException {
        return doGET(Plan.PLANS_RESOURCE + "/" + planCode, Plan.class);
    }

    /**
     * Return all the plans
     * <p/>
     *
     * @return the plan object as identified by the passed in ID
     */
    public Plans getPlans() throws IOException, RequestException {
        return doGET(Plans.PLANS_RESOURCE, Plans.class);
    }

    /**
     * Deletes a {@link Plan}
     * <p/>
     *
     * @param planCode The {@link Plan} object to delete.
     */
    public void deletePlan(final String planCode) throws IOException, RequestException {
        doDELETE(Plan.PLANS_RESOURCE +
                 "/" +
                 planCode);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create an AddOn to a Plan
     * <p/>
     *
     * @param planCode The planCode of the {@link Plan } to create within recurly
     * @param addOn    The {@link AddOn} to create within recurly
     * @return the {@link AddOn} object as identified by the passed in object
     */
    public AddOn createPlanAddOn(final String planCode, final AddOn addOn)
            throws InterruptedException, ExecutionException, IOException, RequestException {
        return doPOST(Plan.PLANS_RESOURCE +
                      "/" +
                      planCode +
                      AddOn.ADDONS_RESOURCE,
                      addOn, AddOn.class);
    }

    /**
     * Get an AddOn's details
     * <p/>
     *
     * @param addOnCode recurly id of {@link AddOn}
     * @param planCode  recurly id of {@link Plan}
     * @return the {@link AddOn} object as identified by the passed in plan and add-on IDs
     */
    public AddOn getAddOn(final String planCode, final String addOnCode) throws IOException, RequestException {
        return doGET(Plan.PLANS_RESOURCE +
                     "/" +
                     planCode +
                     AddOn.ADDONS_RESOURCE +
                     "/" +
                     addOnCode, AddOn.class);
    }

    /**
     * Return all the {@link AddOn} for a {@link Plan}
     * <p/>
     *
     * @return the {@link AddOn} objects as identified by the passed plan ID
     */
    public AddOn getAddOns(final String planCode) throws IOException, RequestException {
        return doGET(Plan.PLANS_RESOURCE +
                     "/" +
                     planCode +
                     AddOn.ADDONS_RESOURCE, AddOn.class);
    }

    /**
     * Deletes a {@link AddOn} for a Plan
     * <p/>
     *
     * @param planCode  The {@link Plan} object.
     * @param addOnCode The {@link AddOn} object to delete.
     */
    public void deleteAddOn(final String planCode, final String addOnCode) throws IOException, RequestException {
        doDELETE(Plan.PLANS_RESOURCE +
                 "/" +
                 planCode +
                 AddOn.ADDONS_RESOURCE +
                 "/" +
                 addOnCode);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a {@link Coupon}
     * <p/>
     *
     * @param coupon The coupon to create on recurly
     * @return the {@link Coupon} object
     */
    public Coupon createCoupon(final Coupon coupon)
            throws InterruptedException, ExecutionException, IOException, RequestException {
        return doPOST(Coupon.COUPON_RESOURCE, coupon, Coupon.class);
    }

    /**
     * Get a Coupon
     * <p/>
     *
     * @param couponCode The code for the {@link Coupon}
     * @return The {@link Coupon} object as identified by the passed in code
     */
    public Coupon getCoupon(final String couponCode) throws IOException, RequestException {
        return doGET(Coupon.COUPON_RESOURCE + "/" + couponCode, Coupon.class);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Recurly.js API
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fetch Subscription
     * <p/>
     * Returns subscription from a recurly.js token.
     *
     * @param recurlyToken token given by recurly.js
     * @return subscription object on success, null otherwise
     */
    public Subscription fetchSubscription(final String recurlyToken) throws IOException, RequestException {
        return fetch(recurlyToken, Subscription.class);
    }

    /**
     * Fetch BillingInfo
     * <p/>
     * Returns billing info from a recurly.js token.
     *
     * @param recurlyToken token given by recurly.js
     * @return billing info object on success, null otherwise
     */
    public BillingInfo fetchBillingInfo(final String recurlyToken) throws IOException, RequestException {
        return fetch(recurlyToken, BillingInfo.class);
    }

    /**
     * Fetch Invoice
     * <p/>
     * Returns invoice from a recurly.js token.
     *
     * @param recurlyToken token given by recurly.js
     * @return invoice object on success, null otherwise
     */
    public Invoice fetchInvoice(final String recurlyToken) throws IOException, RequestException {
        return fetch(recurlyToken, Invoice.class);
    }

    private <T> T fetch(final String recurlyToken, final Class<T> clazz) throws IOException, RequestException {
        return doGET(FETCH_RESOURCE + "/" + recurlyToken, clazz);
    }

    ///////////////////////////////////////////////////////////////////////////

    private <T> T doGET(final String resource, final Class<T> clazz) throws IOException, RequestException {
        final StringBuffer url = new StringBuffer(baseUrl);
        url.append(resource);
        if (resource != null && !resource.contains("?")) {
            url.append("?");
        } else {
            url.append("&");
            url.append("&");
        }
        url.append(getPageSizeGetParam());

        log.debug("Msg to Recurly API [GET] :: URL : {}", url);
        return callRecurly(url.toString(), clazz);
    }

    private <T> T doGET(final String resource, final String parameters, final Class<T> clazz) throws IOException, RequestException {

        final StringBuffer url = new StringBuffer(baseUrl);
        url.append(resource);
        url.append("?");
        url.append(parameters);

        log.debug("Msg to Recurly API [GET] :: URL : {}", url);

        return callRecurly(url.toString(), clazz);
    }


    private <T> T doPOST(final String resource, final RecurlyObject payload, final Class<T> clazz)
            throws InterruptedException, ExecutionException, IOException, RequestException {

        final String xmlPayload = xmlMapper.writeValueAsString(payload);
        log.debug("Msg to Recurly API [POST]:: URL : {}", baseUrl + resource);
        log.debug("Payload for [POST]:: {}", xmlPayload);

        return callRecurly(baseUrl + resource, xmlPayload, clazz);
    }

    private <T> T doPUT(final String resource, final RecurlyObject payload, final Class<T> clazz)
            throws IOException, RequestException {
        final String xmlPayload = xmlMapper.writeValueAsString(payload);
        log.debug("Msg to Recurly API [PUT]:: URL : {}", baseUrl + resource);
        log.debug("Payload for [PUT]:: {}", xmlPayload);

        return callRecurly(baseUrl + resource, xmlPayload, clazz);
    }

    private void doDELETE(final String resource) throws IOException, RequestException {
        callRecurly(baseUrl + resource, null);
    }

    private <T> T callRecurly(final String baseUrl, @Nullable final Class<T> clazz)
            throws IOException, RequestException {
        return callRecurly(baseUrl, null, clazz);
    }

    private <T> T callRecurly(final String baseUrl, final String xmlPayload, @Nullable final Class<T> clazz)
            throws IOException, RequestException {
        if (clazz == null) {
            return null;
        }

        T obj = null;
        InputStream stream = null;
        DefaultHttpClient  httpClient = new DefaultHttpClient();
        try
        {
            HttpPost httpPost = new HttpPost(baseUrl);
            httpPost.addHeader("Authorization", "Basic " + key);
            httpPost.addHeader("Accept", "application/xml");
            httpPost.addHeader("Content-Type", "application/xml; charset=utf-8");
            if (xmlPayload != null && !xmlPayload.isEmpty())
                httpPost.setEntity(new StringEntity(xmlPayload, "text/xml", "ISO-8859-1"));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() >= 300)
                throw new RequestException(baseUrl, EntityUtils.toString(entity));

            stream = entity.getContent();
            final String payload = convertStreamToString(stream);
            log.debug("Msg from Recurly API :: {}", payload);
            obj = xmlMapper.readValue(payload, clazz);
        } finally {
            httpClient.getConnectionManager().shutdown();
            if (stream != null)
                stream.close();
        }

        return obj;
    }

    private String convertStreamToString(final java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    private AsyncHttpClient createHttpClient() {
        // Don't limit the number of connections per host
        // See https://github.com/ning/async-http-client/issues/issue/28
        final AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setMaximumConnectionsPerHost(-1);
        return new AsyncHttpClient(builder.build());
    }
}
