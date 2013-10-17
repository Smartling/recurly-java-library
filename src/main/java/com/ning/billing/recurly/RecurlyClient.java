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

import com.ning.billing.recurly.model.errors.Error404;
import com.ning.billing.recurly.model.errors.ErrorMessage404;
import com.ning.billing.recurly.model.exceptions.RecurlyException;
import com.ning.billing.recurly.model.exceptions.RequestException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;

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
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.MDC;
import org.slf4j.impl.StaticMDCBinder;

public class RecurlyClient {

    private static final Logger log = LoggerFactory.getLogger(RecurlyClient.class);

    public static final String RECURLY_DEBUG_KEY = "recurly.debug";
    public static final String RECURLY_PAGE_SIZE_KEY = "recurly.page.size";

    private static final Integer DEFAULT_PAGE_SIZE = 20;
    private static final String PER_PAGE = "per_page=";

    public static final String FETCH_RESOURCE = "/recurly_js/result";

    private static final String PAGING_ITEMS_PER_PAGE_PARAMETER_NAME = "per_page";
    private static final String PARAMETER_DELIMITER = "&";

    private static final String ERROR_MESSAGE_TEMPLATE = "error code %d (%s)";

    private static final Logger anotherLog  = LoggerFactory.getLogger("recurlyClientLogger");
    private static final String LOG_HTTP_MESSAGE_PREFIX = "RecurlyClient : %s";

    private static final String LOG_HTTP_MESSAGE_TEMPLATE = "status code: %d request: %s response: %s";
    private static final String LOG_HTTP_MESSAGE_FROM_RECURLY_API_TEMPLATE = "message from API: %s";

    private static final String LOG_MDC = "RecurlyClient.recurlyClientIdentifier";

    private final static String XML_TAG_PATTERN_MATCH_TEMPLATE = "<\\s?%s.*?>.*?</\\s?%s.*?>";
    private final static String XML_HIDED_NODE_VALUES_MASK = "****";
    private final static String XML_TAG_NODE_TEMPLATE = "<%s>%s</%s>";

    /**
     * Checks a system property to see if debugging output is
     * required. Used internally by the client to decide whether to
     * generate debug output
     */
    private static boolean debug() {
        return Boolean.getBoolean(RECURLY_DEBUG_KEY);
    }

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
            StringBuffer stringBuffer = new StringBuffer();
            for (String parameterNameValuePair : parameterListHolder)
            {
                if (!isItFirst)
                    stringBuffer.append(PARAMETER_DELIMITER);
                stringBuffer.append(parameterNameValuePair);
                isItFirst = false;
            }

            return stringBuffer.toString();
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

    private String instanceIdentifier;
    private Set<String> hideXMLResponseNodeValues;

    public RecurlyClient(final String apiKey) {
        this(apiKey, "api.recurly.com", 443, "v2");
    }

    public RecurlyClient(final String apiKey, final String host, final int port, final String version) {
        this.key = DatatypeConverter.printBase64Binary(apiKey.getBytes());
        this.baseUrl = String.format("https://%s:%d/%s", host, port, version);
        this.xmlMapper = RecurlyObject.newXmlMapper();

        instanceIdentifier = getLog4jMDCIdentifier();
    }

    private String getLog4jMDCIdentifier()
    {
        return String.format("recurlyClientId=%s", UUID.randomUUID().toString());
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
    public Account createAccount(final Account account) {
        return doPOST(Account.ACCOUNT_RESOURCE, account, Account.class);
    }

    /**
     * Get Accounts
     * <p/>
     * Returns information about all accounts.
     *
     * @return account object on success, null otherwise
     */
    public Accounts getAccounts() {
        return doGET(Accounts.ACCOUNTS_RESOURCE, Accounts.class);
    }

    public Coupons getCoupons() {
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
    public Account getAccount(final String accountCode) {
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
    public Account updateAccount(final String accountCode, final Account account) {
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
    public void closeAccount(final String accountCode) {
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
    public Subscription createSubscription(final Subscription subscription) {
        return doPOST(Subscription.SUBSCRIPTION_RESOURCE,
                subscription, Subscription.class
        );
    }

    /**
     * Get a particular {@link Subscription} by it's UUID
     * <p/>
     * Returns information about a single account.
     *
     * @param uuid UUID of the subscription to lookup
     * @return Subscriptions for the specified user
     */
    public Subscription getSubscription(final String uuid) {
        return doGET(Subscriptions.SUBSCRIPTIONS_RESOURCE
                + "/" + uuid,
                Subscription.class
        );
    }

    /**
     * Cancel a subscription
     * <p/>
     * Cancel a subscription so it remains active and then expires at the end of the current bill cycle.
     *
     * @param subscription Subscription object
     * @return -?-
     */
    public Subscription cancelSubscription(final Subscription subscription) {
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
    public Subscription reactivateSubscription(final Subscription subscription) {
        return doPUT(Subscription.SUBSCRIPTION_RESOURCE + "/" + subscription.getUuid() + "/reactivate",
                subscription, Subscription.class
        );
    }

    /**
     * Update a particular {@link Subscription} by it's UUID
     * <p/>
     * Returns information about a single account.
     *
     * @param uuid UUID of the subscription to update
     * @return Subscription the updated subscription
     */
    public Subscription updateSubscription(final String uuid, final SubscriptionUpdate subscriptionUpdate) {
        return doPUT(Subscriptions.SUBSCRIPTIONS_RESOURCE
                + "/" + uuid,
                subscriptionUpdate,
                Subscription.class
        );
    }

    /**
     * Get the subscriptions for an {@link Account}.
     * <p/>
     * Returns information about a single {@link Account}.
     *
     * @param accountCode recurly account id
     * @return Subscriptions for the specified user
     */
    public Subscriptions getAccountSubscriptions(final String accountCode) {
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
    public Subscriptions getAccountSubscriptions(final String accountCode, final String status) {
        return doGET(Account.ACCOUNT_RESOURCE
                + "/" + accountCode
                + Subscriptions.SUBSCRIPTIONS_RESOURCE
                + "?state="
                + status,
                Subscriptions.class
        );
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
    public BillingInfo createOrUpdateBillingInfo(final BillingInfo billingInfo) {
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
    public BillingInfo getBillingInfo(final String accountCode) {
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
    public void clearBillingInfo(final String accountCode) {
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
    public Transactions getAccountTransactions(final String accountCode) {
        return doGET(Accounts.ACCOUNTS_RESOURCE + "/" + accountCode + Transactions.TRANSACTIONS_RESOURCE,
                Transactions.class
        );
    }

    /**
     * Lookup an account's transactions history by type and state
     * <p/>
     * Returns the account's transaction history
     *
     * @param accountCode recurly account id
     * @return the transaction history associated with this account on success, null otherwise
     */
    public Transactions getAccountTransactions(final String accountCode, final TransactionFilter transactionFilter, PagingParams pagingParams) {

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
    public Transaction createTransaction(final Transaction trans) {
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
    public Invoices getAccountInvoices(final String accountCode) {
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
    public Invoice getInvoice(final Integer invoiceId) {
        return doGET(Invoices.INVOICES_RESOURCE + "/" + invoiceId.toString(),
                Invoice.class
        );
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a Plan's info
     * <p/>
     *
     * @param plan The plan to create on recurly
     * @return the plan object as identified by the passed in ID
     */
    public Plan createPlan(final Plan plan) {
        return doPOST(Plan.PLANS_RESOURCE, plan, Plan.class);
    }

    /**
     * Get a Plan's details
     * <p/>
     *
     * @param planCode recurly id of plan
     * @return the plan object as identified by the passed in ID
     */
    public Plan getPlan(final String planCode) {
        return doGET(Plan.PLANS_RESOURCE + "/" + planCode, Plan.class);
    }

    /**
     * Return all the plans
     * <p/>
     *
     * @return the plan object as identified by the passed in ID
     */
    public Plans getPlans() {
        return doGET(Plans.PLANS_RESOURCE, Plans.class);
    }

    /**
     * Deletes a {@link Plan}
     * <p/>
     *
     * @param planCode The {@link Plan} object to delete.
     */
    public void deletePlan(final String planCode) {
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
    public AddOn createPlanAddOn(final String planCode, final AddOn addOn) {
        return doPOST(Plan.PLANS_RESOURCE +
                "/" +
                planCode +
                AddOn.ADDONS_RESOURCE,
                addOn, AddOn.class
        );
    }

    /**
     * Get an AddOn's details
     * <p/>
     *
     * @param addOnCode recurly id of {@link AddOn}
     * @param planCode  recurly id of {@link Plan}
     * @return the {@link AddOn} object as identified by the passed in plan and add-on IDs
     */
    public AddOn getAddOn(final String planCode, final String addOnCode) {
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
    public AddOn getAddOns(final String planCode) {
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
    public void deleteAddOn(final String planCode, final String addOnCode) {
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
    public Coupon createCoupon(final Coupon coupon) {
        return doPOST(Coupon.COUPON_RESOURCE, coupon, Coupon.class);
    }

    /**
     * Get a Coupon
     * <p/>
     *
     * @param couponCode The code for the {@link Coupon}
     * @return The {@link Coupon} object as identified by the passed in code
     */
    public Coupon getCoupon(final String couponCode) {
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
    public Subscription fetchSubscription(final String recurlyToken) {
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
    public BillingInfo fetchBillingInfo(final String recurlyToken) {
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
    public Invoice fetchInvoice(final String recurlyToken) {
        return fetch(recurlyToken, Invoice.class);
    }

    private <T> T fetch(final String recurlyToken, final Class<T> clazz) {
        return doGET(FETCH_RESOURCE + "/" + recurlyToken, clazz);
    }

    ///////////////////////////////////////////////////////////////////////////

    private <T> T doGET(final String resource, final Class<T> clazz) {
        final StringBuffer url = new StringBuffer(baseUrl);
        url.append(resource);
        if (resource != null && !resource.contains("?")) {
            url.append("?");
        } else {
            url.append("&");
            url.append("&");
        }
        url.append(getPageSizeGetParam());

        if (debug()) {
            log.info("Msg to Recurly API [GET] :: URL : {}", url);
        }
        return callRecurlySafe(client.prepareGet(url.toString()), clazz);
    }

    private <T> T doGET(final String resource, final String parameters, final Class<T> clazz) {
        final StringBuffer url = new StringBuffer(baseUrl);
        url.append(resource);
        url.append("?");
        url.append(parameters);

        if (debug()) {
            log.info("Msg to Recurly API [GET] :: URL : {}", url);
        }
        return callRecurlySafe(client.prepareGet(url.toString()), clazz);
    }


    private <T> T doPOST(final String resource, final RecurlyObject payload, final Class<T> clazz) {
        final String xmlPayload;
        try {
            xmlPayload = xmlMapper.writeValueAsString(payload);
            if (debug()) {
                log.info("Msg to Recurly API [POST]:: URL : {}", baseUrl + resource);
                log.info("Payload for [POST]:: {}", xmlPayload);
            }
        } catch (IOException e) {
            log.warn("Unable to serialize {} object as XML: {}", clazz.getName(), payload.toString());
            return null;
        }

        return callRecurlySafe(client.preparePost(baseUrl + resource).setBody(xmlPayload), clazz);
    }

    private <T> T doPUT(final String resource, final RecurlyObject payload, final Class<T> clazz) {
        final String xmlPayload;
        try {
            xmlPayload = xmlMapper.writeValueAsString(payload);
            if (debug()) {
                log.info("Msg to Recurly API [PUT]:: URL : {}", baseUrl + resource);
                log.info("Payload for [PUT]:: {}", xmlPayload);
            }
        } catch (IOException e) {
            log.warn("Unable to serialize {} object as XML: {}", clazz.getName(), payload.toString());
            return null;
        }

        return callRecurlySafe(client.preparePut(baseUrl + resource).setBody(xmlPayload), clazz);
    }

    private void doDELETE(final String resource) {
        callRecurlySafe(client.prepareDelete(baseUrl + resource), null);
    }

    private <T> T callRecurlySafe(final AsyncHttpClient.BoundRequestBuilder builder, @Nullable final Class<T> clazz) {
        try {
            return callRecurly(builder, clazz);
        } catch (IOException e) {
            log.warn("Error while calling Recurly", e);
            return null;
        } catch (ExecutionException e) {
            log.error("Execution error", e);
            return null;
        } catch (InterruptedException e) {
            log.error("Interrupted while calling Recurly", e);
            return null;
        }
    }

    private class HttpResponseContainer
    {
        private Object object;
        private RecurlyException exception;

        public Object getObject()
        {
            return object;
        }

        public void setObject(final Object object)
        {
            this.object = object;
        }

        public RecurlyException getException()
        {
            return exception;
        }

        public void setException(final RecurlyException exception)
        {
            this.exception = exception;
        }
    }

    private <T> T callRecurly(final AsyncHttpClient.BoundRequestBuilder builder, @Nullable final Class<T> clazz)
            throws IOException, ExecutionException, InterruptedException {


        org.apache.log4j.MDC.put(LOG_MDC, instanceIdentifier);
        try
        {
            logHttpInfoMessage(anotherLog, "callRecurly() starting ...");

            HttpResponseContainer httpResponseContainer = builder.addHeader("Authorization", "Basic " + key)
                                                                 .addHeader("Accept", "application/xml")
                                                                 .addHeader("Content-Type", "application/xml; charset=utf-8")
                                                                 .execute(new AsyncCompletionHandler<HttpResponseContainer>()
                                                                 {
                                                                     @Override
                                                                     public HttpResponseContainer onCompleted(final Response response) throws Exception
                                                                     {
                                                                         final HttpResponseContainer httpResponseContainer = new HttpResponseContainer();
                                                                         try
                                                                         {
                                                                             int statusCode = response.getStatusCode();
                                                                             String responseUri = response.getUri().toString();
                                                                             String responseBody = response.getResponseBody();

                                                                             if (statusCode >= 300)
                                                                             {
                                                                                 log.warn("Recurly error whilst calling: {}", responseUri);
                                                                                 log.warn("Recurly error: {}", hideResponseXMLNodeValues(responseBody));

                                                                                 logHttpErrorMessage(anotherLog, getLogHttpMessage(statusCode, responseUri, responseBody));

                                                                                 String errorMessage = getErrorMessage(statusCode, responseBody);

                                                                                 httpResponseContainer.setException(new RequestException(responseUri,
                                                                                         String.format(ERROR_MESSAGE_TEMPLATE, statusCode, errorMessage)));

                                                                                 return httpResponseContainer;
                                                                             }
                                                                             else
                                                                             {
                                                                                 logHttpInfoMessage(anotherLog, getLogHttpMessage(statusCode, responseUri, responseBody));
                                                                             }

                                                                             if (clazz == null)
                                                                             {
                                                                                 return null;
                                                                             }

                                                                             final InputStream in = response.getResponseBodyAsStream();
                                                                             try
                                                                             {
                                                                                 final String payload = convertStreamToString(in);
                                                                                 if (debug())
                                                                                 {
                                                                                     String logPayload = hideResponseXMLNodeValues(payload);
                                                                                     log.info("Msg from Recurly API :: {}", logPayload);
                                                                                     logHttpInfoMessage(anotherLog, String.format(LOG_HTTP_MESSAGE_FROM_RECURLY_API_TEMPLATE,
                                                                                             logPayload));
                                                                                 }

                                                                                 T obj = xmlMapper.readValue(payload, clazz);

                                                                                 httpResponseContainer.setObject(obj);

                                                                                 return httpResponseContainer;
                                                                             }
                                                                             finally
                                                                             {
                                                                                 closeStream(in);
                                                                             }
                                                                         }
                                                                         catch (Exception exception)
                                                                         {
                                                                             httpResponseContainer.setException(new RecurlyException(exception));
                                                                         }

                                                                         return httpResponseContainer;
                                                                     }
                                                                 }
                                                                 ).get();

            if (null != httpResponseContainer.getException())
                throw new RequestException(httpResponseContainer.getException());

            logHttpInfoMessage(anotherLog, "callRecurly() finished");

            return null == httpResponseContainer ? null : (T)httpResponseContainer.getObject();
        }
        finally
        {
            org.apache.log4j.MDC.remove(LOG_MDC);
        }
    }

    private String getErrorMessage(int statusCode, String responseBody)
    {
        String errorMessage = null;

        switch (statusCode)
        {
            case 404:
                try
                {
                    ErrorMessage404 errorObject = xmlMapper.readValue(responseBody, ErrorMessage404.class);
                    if (null != errorObject.getDescription())
                        errorMessage = errorObject.getDescription();
                    else
                    {
                        Error404 errorObjectAnother = xmlMapper.readValue(responseBody, Error404.class);
                        errorMessage = errorObjectAnother.getError();
                    }
                }
                catch (Exception exceptionMapping)
                {
                    errorMessage = responseBody;
                }
                break;

            default:
                errorMessage = responseBody;
        }

        return errorMessage;
    }

    private String convertStreamToString(final java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next().replace("\n", "");
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    private void closeStream(final InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                log.warn("Failed to close http-client - provided InputStream: {}", e.getLocalizedMessage());
            }
        }
    }

    private AsyncHttpClient createHttpClient() {
        // Don't limit the number of connections per host
        // See https://github.com/ning/async-http-client/issues/issue/28
        final AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setMaximumConnectionsPerHost(-1);
        return new AsyncHttpClient(builder.build());
    }

    private String getLogHttpMessage(int statusCode, String responseUri, String responseBody)
    {
        return String.format(LOG_HTTP_MESSAGE_TEMPLATE, statusCode, responseUri, hideResponseXMLNodeValues(responseBody));
    }

    private String formatLogHttpInfoMessage(String message)
    {
        return String.format(LOG_HTTP_MESSAGE_PREFIX, message);
    }

    private void logHttpInfoMessage(Logger log, String message)
    {
        log.info(formatLogHttpInfoMessage(message));
    }

    private void logHttpWarningMessage(Logger log, String message)
    {
        log.warn(formatLogHttpInfoMessage(message));
    }

    private void logHttpDebugMessage(Logger log, String message)
    {
        log.debug(formatLogHttpInfoMessage(message));
    }

    private void logHttpErrorMessage(Logger log, String message)
    {
        log.error(formatLogHttpInfoMessage(message));
    }

    public Set<String> getHideXMLResponseNodeValues()
    {
        return hideXMLResponseNodeValues;
    }

    public void setHideXMLResponseNodeValues(final Set<String> hideXMLResponseNodeValues)
    {
        this.hideXMLResponseNodeValues = hideXMLResponseNodeValues;
    }

    private String hideResponseXMLNodeValues(String responseXml)
    {
        String processedResponseXml = responseXml;
        for (String nodeName : hideXMLResponseNodeValues)
        {
            Pattern pattern = Pattern.compile(String.format(XML_TAG_PATTERN_MATCH_TEMPLATE, nodeName, nodeName), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(processedResponseXml);
            processedResponseXml = matcher.replaceAll(String.format(XML_TAG_NODE_TEMPLATE, nodeName, XML_HIDED_NODE_VALUES_MASK, nodeName));
        }

        return processedResponseXml;
    }
}
