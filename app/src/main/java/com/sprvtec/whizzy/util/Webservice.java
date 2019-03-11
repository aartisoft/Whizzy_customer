package com.sprvtec.whizzy.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 */
public class Webservice {


    private static final String BASE_C = "http://services.whizzy.in:8080/";
    private static final String BASE = "http://services.whizzy.in:8080/";
    public static final String HOME_IMAGE_PATH = BASE + "public/app_intro_images/";
    public static final String PROFILE_IMAGE_PATH = BASE + "public/user_images/";
    private static final String RAZORPAY_PATH = "http://services.whizzy.in:3001/";
    public static final String VERIFY_CHECKSUM = BASE + "VerifyChecksum";
    public static final String GENERATE_CHECKSUM = BASE + "GenerateChecksum";
    public static final String DRIVER_IMAGE_PATH_NEW = "http://services.whizzy.in:8081/uploads/";


    private static final String RESPONSE = "Response";

    //CreateConsumer
    public static final String REGISTER_USER = "RegisterUser";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_MOBILE = "user_mobile";
    public static final String FULL_NAME = "full_name";
    public static final String DEVICE_ID = "device_id";
    public static final String OPERATING_SYSTEM = "operating_system";
    public static final String CREATE_USER = "CreateUser";
    public static final String FCM_TOKEN = "fcm_token";
    public static final String USER_IMAGE_URL = "user_image_url";
    public static final String APP_VERSION = "app_version";


    public static final String USER_ID = "user_id";


    //CreateOrder
    public static final String CREATE_ORDER = "CreateOrder_V7";
    public static final String VEHICLE_ID = "vehicle_id";
    public static final String VEHICLE_NAME = "vehicle_name";
    public static final String PAYMENT_REQUIRED = "payment_required";
    public static final String PICKUP_NAME = "pickup_contact_name";
    public static final String PICKUP_NUMBER = "pickup_contact_number";
    public static final String PICKUP_MORE_INFO = "pickup_contact_more_info";
    public static final String PICKUP_LATITUDE = "pickup_latitude";
    public static final String PICKUP_LONGITUDE = "pickup_longitude";
    public static final String DROPOFF_NAME = "drop_contact_name";
    public static final String DROPOFF_NUMBER = "drop_contact_number";
    public static final String DROP_MORE_INFO = "drop_contact_more_info";
    public static final String DROPOFF_LATITUDE = "drop_latitude";
    public static final String DROPOFF_LONGITUDE = "drop_longitude";
    public static final String TRANSPORT_DESC = "transport_description";
    public static final String PICKUP_FULL_ADDRESS = "pickup_full_address";
    public static final String DROPOFF_FULL_ADDRESS = "drop_full_address";
    public static final String PICKUP_LANDMARK = "pickup_landmark";
    public static final String DROP_LANDMARK = "drop_landmark";
    public static final String ORDER_TYPE = "order_type";
    public static final String BUY_LOCATION = "buy_location";
    public static final String SCHEDULED_ORDER = "scheduled_order";
    public static final String BUSINESS_NAME = "business_name";
    public static final String BUSINESS_ID = "business_id";
    public static final String SCHEDULE_DATE_TIME = "scheduled_date_time";
    public static final String SCHEDULE_ON_MY_BEHALF="schedule_on_my_behalf";


    //sendOTP
    public static final String SEND_OTP = "SendOTP";

    //GetWhizzyVehicles
    public static final String GET_WHIZZY_VEHICLES = "GetWhizzyVehicles";
    public static final String FROM_LATITUDE = "from_latitude";
    public static final String FROM_LONGITUDE = "from_longitude";
    public static final String TO_LATITUDE = "to_latitude";
    public static final String TO_LONGITUDE = "to_longitude";
    public static final String TRIP_TYPE = "trip_type";



    //GetDriverDetails
    public static final String ORDER_ID = "order_id";


    //CustomerCancelTrip
    public static final String CUSTOMER_CANCEL_TRIP = "CustomerCancelTrip";
    public static final String CANCEL_OPTION = "cancel_option";

    //EditScheduledOrder
    public static final String EDIT_SCHEDULED_ORDER = "EditScheduledOrder";
    //GetCustomerFeedbackOptions
    public static final String RATINGS = "ratings";

    //GiveDriverFeedback
    public static final String GIVE_DRIVER_FEEDBACK = "GiveDriverFeedback";
    public static final String DRIVER_ID = "driver_id";
    public static final String FEEDBACK_OPTION = "feedback_option";

    //coupn stuff here
    public static final String COUPN_CODE = "coupon_code";
    public static final String DISCOUNTED_FARE = "discounted_fare";
    public static final String DISCOUNT = "discount";
    public static final String DROP_CONTACT_DETAILS = "drop_contact_details";
    public static final String ORDER_TYPE_FLOW = "order_type_flow";

    public static final String GET_COUPN_VALID = "ValidateCoupon";
    public static final String COUPNCODE = "coupon_code";
    public static final String WHIZZYFARE = "fare";



    //GetTripSummary
    public static final String GET_TRIP_SUMMARY = "GetTripSummary";

    //getdriverdetail
    public static final String DRIVER_GET_ORDER_DETAIL = "DriverGetOrderDetails";

    //Expenses Show
    public static final String GET_FARE_BREAKFOWN = "GetFareBreakDown_V6";

    //GetProfileDetails
    public static final String GET_PROFILE_DETAILS = "GetProfileDetails_V8";

    //UpdateUserProfile
    public static final String UPDATE_USER_PROFILE = "UpdateUserProfile";

    //GetMyOrders
    public static final String GET_MY_ORDERS = "GetMyOrders";

    //GetMyOrders
    public static final String GET_MY_SCHEDULED_ORDERS = "GetMyScheduledOrders";
    //Google places api
    public static final String RADUIS = "radius";
    public static final String TYPES = "types";
    public static final String KEY = "key";
    public static final String LOCATION = "location";
    //getdriverdetail
    private static final String BUSINESS_DETAILS = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";





    //UpdateOrderPaymentStatus
    public static final String UPDATE_ORDER_PAYMENT_STATUS = "UpdateOrderPaymentStatus";
    public static final String PAYMENT_TRANSACTION_NUMBER = "payment_transaction_number";
    public static final String PAYMENT_TRANSACTION_DESCRIPTION = "payment_transaction_description";
    public static final String PAYMENT_STATUS = "payment_status";
    public static final String PAYMENT_GATEWAY_CHARGES = "payment_gateway_charges";
    public static final String PAYMENT_GATEWAY_PERCENTAGE = "payment_gateway_percentage";
    public static final String ORDER_FARE = "order_fare";
    public static final String MONTHLY_BILLING = "monthly_billing";
    public static final String EXPENSE_PAYMENT_MODE = "expenses_payment_mode";

    //getbusinesses

    public static final String GET_BUSINESSES = "getBusinesses";
    public static final String KEYWORD = "keyword";
    public static final String CATEGORY = "category";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String RADIUS = "radius";
    public static final String OFFSET = "Offset";
    public static final String PAGE_COUNT = "PageCount";
    public static final String SUB_CATEGORY = "subcategory";



    //GetIntroImages
    public static final String GET_INTRO_IMAGES = "GetIntroImages";


    public static final String RAZOR_PAYMENT = "razorpay";
    public static final String AMOUNT_ID = "amount";
    public static final String PAYMENT_ID = "payment_id";
    public static final String PAYMENT_MODE = "payment_mode";
    /**
     * Time out for http connection
     */
    private static int timeOut = 150 * 1000; //2:30min

    /**
     * This method is used to call Post method webservice.
     *
     * @param url            URL of the webservice
     * @param nameValuePairs parameters to the Post method
     * @return response string which is returned by the server.
     */


    public static String callPostServiceRazorPay(String url, List<NameValuePair> nameValuePairs) {
        String response = "";
        try {
            AtomicReference<BasicHttpParams> params = new AtomicReference<>(new BasicHttpParams());

            HttpProtocolParams.setVersion(params.get(), HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params.get(), HTTP.UTF_8);

            HttpClient httpclient = new DefaultHttpClient(params.get());
            HttpPost httppost = new HttpPost(RAZORPAY_PATH + url);
            Log.i("", RAZORPAY_PATH + url);
            JSONObject object = new JSONObject();
            String message = "";
            for (NameValuePair np : nameValuePairs) {
                try {
                    object.put(np.getName(), np.getValue());
                    Log.e(np.getName(), np.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            message = object.toString();


            httppost.setEntity(new StringEntity(message, "UTF8"));
            httppost.setHeader("Content-type", "application/json");
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient
                    .execute(httppost);
//            int StatusCode = httpResponse.getStatusLine().getStatusCode();
//            if (StatusCode == HttpURLConnection.HTTP_OK) {
            InputStream is = httpResponse.getEntity().getContent();

            java.util.Scanner s = new java.util.Scanner(is)
                    .useDelimiter("\\A");
            response = s.hasNext() ? s.next() : "";
//            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String callPostService1(String url, List<NameValuePair> nameValuePairs) {
        String response = "";
        try {
            AtomicReference<BasicHttpParams> params = new AtomicReference<>(new BasicHttpParams());

            HttpProtocolParams.setVersion(params.get(), HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params.get(), HTTP.UTF_8);

            HttpClient httpclient = new DefaultHttpClient(params.get());
            HttpPost httppost = new HttpPost(BASE + url);
            Log.i("urlll", BASE + url);
            JSONObject object = new JSONObject();
            String message = "";
            for (NameValuePair np : nameValuePairs) {
                try {
                    object.put(np.getName(), np.getValue());
                    Log.e(np.getName(), np.getValue()+"");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            message = object.toString();


            httppost.setEntity(new StringEntity(message, "UTF8"));
            httppost.setHeader("Content-type", "application/json");
//            for (NameValuePair header : headers) {
//                httppost.setHeader(header.getName(), header.getValue());
//            }
//            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient
                    .execute(httppost);
            int StatusCode = httpResponse.getStatusLine().getStatusCode();
            Log.e("response", httpResponse + "resss  " + StatusCode + httpResponse.getStatusLine() + HttpURLConnection.HTTP_OK);
//            if (StatusCode == HttpURLConnection.HTTP_OK) {
            InputStream is = httpResponse.getEntity().getContent();

            java.util.Scanner s = new java.util.Scanner(is)
                    .useDelimiter("\\A");
            response = s.hasNext() ? s.next() : "";
//            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String callPostService12(String url, List<NameValuePair> nameValuePairs) {
        String response = "";
        try {
            AtomicReference<BasicHttpParams> params = new AtomicReference<>(new BasicHttpParams());

            HttpProtocolParams.setVersion(params.get(), HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params.get(), HTTP.UTF_8);

            HttpClient httpclient = new DefaultHttpClient(params.get());
            HttpPost httppost = new HttpPost(BASE_C + url);
            Log.i("", BASE_C + url);
            JSONObject object = new JSONObject();
            String message = "";
            for (NameValuePair np : nameValuePairs) {
                try {
                    object.put(np.getName(), np.getValue());
                    Log.e(np.getName(), np.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            message = object.toString();


            httppost.setEntity(new StringEntity(message, "UTF8"));
            httppost.setHeader("Content-type", "application/json");
//            for (NameValuePair header : headers) {
//                httppost.setHeader(header.getName(), header.getValue());
//            }
//            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            BasicHttpResponse httpResponse = (BasicHttpResponse) httpclient
                    .execute(httppost);
//            int StatusCode = httpResponse.getStatusLine().getStatusCode();
//            if (StatusCode == HttpURLConnection.HTTP_OK) {
            InputStream is = httpResponse.getEntity().getContent();

            java.util.Scanner s = new java.util.Scanner(is)
                    .useDelimiter("\\A");
            response = s.hasNext() ? s.next() : "";
//            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    /**
     * This method is used to call Get method webservice.
     *
     * @param url URL of the webservice
     * @return response string which is returned by the server.
     */
    @SuppressWarnings("deprecation")
    public static String callGetService(String url) {
        String response = "";
        Log.i("URL", BASE + url);
        HttpParams params = new BasicHttpParams();
//		AtomicReference<BasicHttpParams> params = new AtomicReference<>(new BasicHttpParams());

        HttpConnectionParams.setConnectionTimeout(params, timeOut);
        HttpConnectionParams.setSoTimeout(params, timeOut);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        HttpClient httpclient = new DefaultHttpClient(params);
        // Prepare a request object
        HttpGet httpget;
        if (url.indexOf("yathams.com") != -1) {
            httpget = new HttpGet(url.replaceAll(" ", "%20"));
        } else
            httpget = new HttpGet(BASE + url.replaceAll(" ", "%20"));
        // Execute the request
        Log.e("", BASE + url.replaceAll(" ", "%20"));
        HttpResponse httpResponse;

        try {

            httpResponse = httpclient.execute(httpget);
            // Get hold of the response entity
            HttpEntity entity = httpResponse.getEntity();

            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                java.util.Scanner s = new java.util.Scanner(instream)
                        .useDelimiter("\\A");
                response = s.hasNext() ? s.next() : "";
                Log.e(RESPONSE, response);
                // now you have the string representation of the HTML request
                instream.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("Response", response);
        return response;

    }



    public static String callGetServicePlaces(String url) {
        String response = "";
        Log.i("URL", url);
        HttpParams params = new BasicHttpParams();
//		AtomicReference<BasicHttpParams> params = new AtomicReference<>(new BasicHttpParams());

        HttpConnectionParams.setConnectionTimeout(params, timeOut);
        HttpConnectionParams.setSoTimeout(params, timeOut);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

        HttpClient httpclient = new DefaultHttpClient(params);
        // Prepare a request object
        HttpGet httpget;
        if (url.indexOf("yathams.com") != -1) {
            httpget = new HttpGet(url.replaceAll(" ", "%20"));
        } else
            httpget = new HttpGet(BUSINESS_DETAILS + url.replaceAll(" ", "%20"));
        // Execute the request
        Log.e("mainurl", BUSINESS_DETAILS + url.replaceAll(" ", "%20"));
        HttpResponse httpResponse;

        try {

            httpResponse = httpclient.execute(httpget);
            // Get hold of the response entity
            HttpEntity entity = httpResponse.getEntity();

            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null) {

                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                java.util.Scanner s = new java.util.Scanner(instream)
                        .useDelimiter("\\A");
                response = s.hasNext() ? s.next() : "";
                Log.e(RESPONSE + "45", response);
                // now you have the string representation of the HTML request

                instream.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("Response7", response);
        return response;

    }
}
