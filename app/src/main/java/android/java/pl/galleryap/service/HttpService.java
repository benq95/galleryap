package android.java.pl.galleryap.service;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Service class which manages internet connection
 */

public class HttpService {
    private static final String url = "http://192.168.0.3:8080/spring/webservice/"; //url pattern for webservice server

    protected HttpService(){}

    private static HttpService instance = null;

    private RequestQueue mRequestQueue;

    private static Context context;

    private String token, username, password;

    //singleton pattern implementation
    public static HttpService getInstance(Context context){
        HttpService.context = context;
        if(instance==null){
            instance = new HttpService();
            return instance;
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * Send photo to the server
     * @param picture picture binary data
     * @param activity listener which captures http response events
     * @throws ServiceException - when some error occured
     */
    public void sendPhoto(byte[] picture, final HttpResponseListener activity) throws ServiceException{
        JSONObject request = new JSONObject();
        try{
            //prepare json request object
            String encodedString = Base64.encodeToString(picture,Base64.DEFAULT);
            request.put("token",token);
            request.put("picture",encodedString);
        } catch(JSONException e) {
            throw new ServiceException("Internal error");
        }
        JsonRequest jsonRequest = new JsonRequest
                (Request.Method.POST, url+"newpicture",request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        activity.GetResponse();//invoke repsonse event method
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(!isConnected(error,activity)){
                            return;
                        }
                        int statusCode = error.networkResponse.statusCode;
                        activity.GetError("Server connection error. Check your internet connection or try again later.");//send error to activity
                        error.printStackTrace();
                    }
                });
        addToRequestQueue(jsonRequest);
    }

    /**
     * Log in to the webservice
     * @param username username
     * @param password password
     * @param activity listener for http response
     * @throws ServiceException - when some error occurs
     */
    public void login(String username, String password, final HttpResponseListener activity) throws ServiceException{
        JSONObject request = new JSONObject();
        //prepare json with username and password
        this.password = password;
        this.username = username;
        try{
            request.put("username",username);
            request.put("password",password);
        } catch(JSONException e) {
            throw new ServiceException("Internal error");
        }
        JsonRequest jsonRequest = new JsonRequest
                (Request.Method.POST, url+"login",request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // the response is already constructed as a JSONObject!
                        try {
                            token = response.getString("token");//save token
                            activity.GetResponse();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            activity.GetError("Server connection error. Check your internet connection or try again later.");//if bad token show error
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(!isConnected(error,activity)){
                            return;
                        }
                        int statusCode = error.networkResponse.statusCode;

                        if(statusCode==401){
                            activity.GetError("Invalid username or password");//show invalid username if 401 error
                            return;
                        }
                        activity.GetError("Server connection error. Check your internet connection or try again later.");//show connection error if different error
                        error.printStackTrace();
                    }
                });
        addToRequestQueue(jsonRequest);
    }

    /**
     * Create new user
     * @param username username
     * @param password password
     * @param activity response listener
     * @throws ServiceException - when some error occured
     */
    public void newUser(String username, String password, final HttpResponseListener activity) throws ServiceException{
        JSONObject request = new JSONObject();
        //prepare json with username and password
        try{
            request.put("username",username);
            request.put("password",password);
        } catch(JSONException e) {
            throw new ServiceException("Internal error");
        }
        JsonRequest jsonRequest = new JsonRequest
                (Request.Method.POST, url+"insert",request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        activity.GetResponse();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(!isConnected(error,activity)){
                            return;
                        }
                        int statusCode = error.networkResponse.statusCode;
                        if(statusCode==400){
                            activity.GetError("User already exists.");//if 400 request ask for different username
                            return;
                        }
                        activity.GetError("Server connection error. Check your internet connection or try again later.");//show conn error if any other code
                    }
                });
        addToRequestQueue(jsonRequest);
    }

    //check network connection and send error if not connected
    private boolean isConnected(VolleyError error, HttpResponseListener activity){
        if(error.networkResponse==null){
            activity.GetError("No internet connection. Please, check your internet connection and try again later.");
            return false;
        }
        return true;
    }

    //JsonRequest class to implement error interpretation
    private class JsonRequest extends JsonObjectRequest{

        public JsonRequest(int method,String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener,
                           Response.ErrorListener errorListener){
            super(method,url,jsonRequest,listener,errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json; charset=utf-8");
            headers.put("accept","application/json");
            return headers;
        }

        @Override
        public String getBodyContentType() {
            return "application/json; charset=utf-8";
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                if (response.data.length == 0) {
                    byte[] responseData = "{}".getBytes("UTF8");
                    response = new NetworkResponse(response.statusCode, responseData, response.headers, response.notModified);
                }
                if(response.statusCode!=200){//if http response different than 200(ok) signalize error
                    return Response.error(new VolleyError(response));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return super.parseNetworkResponse(response);
        }
    };
}
