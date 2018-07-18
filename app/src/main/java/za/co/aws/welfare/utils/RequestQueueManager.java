package za.co.aws.welfare.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by jamessinclair on 2017/03/30.
 *
 * PLEASE NOTE THAT VOLLEY USED IS NOT THE DEFAULT ONE. As suggested here:
 *
 * https://stackoverflow.com/questions/39630712/anonymous-listener-of-volley-request-causing-memory-leak
 *
 * The default one has a memory leak.
 *
 */
public class RequestQueueManager {

    private static RequestQueueManager mInstance;
    private RequestQueue mRequestQueue;

    private RequestQueueManager() {
    }

    public static synchronized RequestQueueManager getInstance() {
        if (mInstance == null) {
            mInstance = new RequestQueueManager();
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                    && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

                //For lower android versions we need to force the TLS socket.

                try {
                    ProviderInstaller.installIfNeeded(context.getApplicationContext());
                } catch (GooglePlayServicesRepairableException e) {
                    Log.w("RQManager", "Provider update failed!");
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.w("RQManager", "Provider update failed!");
                }

                HttpStack stack;
                try {
                    stack = new HurlStack(null, new TLSSocketFactory());
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                    Log.d("Your Wrapper Class", "Could not create new stack for TLS v1.2");
                    stack = new HurlStack();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    Log.d("Your Wrapper Class", "Could not create new stack for TLS v1.2");
                    stack = new HurlStack();
                }

                mRequestQueue = Volley.newRequestQueue(context.getApplicationContext(), stack);
            } else {
                mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
            }
//            mRequestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
//                @Override
//                public void onRequestFinished(Request<Object> request) {
//                    Log.i("REQUEST DONE", request.getTag().toString());
//                }
//            });
        }
        mRequestQueue.getCache().clear();
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(final Request<T> req, Context context) {
        req.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        getRequestQueue(context).add(req);
    }
}
