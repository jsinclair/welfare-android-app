package za.co.aws.welfare.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import za.co.aws.welfare.R;

public class FirebaseTokenUpdater extends Thread {

    private static final String TAG = "FirebaseTokenUpdater";

    private final Runnable runnable;

    public FirebaseTokenUpdater(final Context context, final String firebaseToken, final String sessionToken) {

        runnable = new Runnable() {
            public void run() {

                JSONObject params = new JSONObject();
                try {
                    params.put("firebase_token", firebaseToken);
                } catch (JSONException e) {
                    Log.e(TAG, "run: " + e.getLocalizedMessage());
                }

                String URL = context.getString(R.string.kBaseUrl) + "update_firebase/";

                RequestQueueManager.getInstance().addToRequestQueue(
                        new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i(TAG, "r " + response );
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "run: " + error.getLocalizedMessage());
                            }
                        }){

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                HashMap<String, String> headers = new HashMap<>();
                                headers.put("Accept", "application/json");
                                headers.put("Authorization", "Bearer " + sessionToken);
                                return headers;
                            }
                        }, context);
            }
        };
    }

    @Override
    public synchronized void run() {

        runnable.run();
    }
}
