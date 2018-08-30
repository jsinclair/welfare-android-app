package za.co.aws.welfare.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.Utils;

/**
 * Created by zynique on 2018/06/11.
 *
 * Task used to log in. This helps reduce issues on a orientation change, as the instance is retained
 * regardless of orientation or status changes.
 */
public class LoginTask extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity. Calling activity MUST implement this.
     */
    public interface LoginTaskCallbacks {
        void onLoginComplete(boolean error, String errorMessage, String token, String fullName,
                             String organisationName);
    }

    /**
     * The calling activity.
     */
    private LoginTaskCallbacks mCallbacks;

    /**
     * Create a new instance.
     */
    public static LoginTask newInstance(String email, String password, String uuid) {
        LoginTask f = new LoginTask();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("email", email);
        args.putString("password", password);
        args.putString("uuid", uuid);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof LoginTaskCallbacks) {
            mCallbacks = (LoginTaskCallbacks) activity;
        } else {
            Log.w("LOGIN TASK", "CALLING CLASS DOES NOT IMPLEMENT INTERFACE!");
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        final String email = getArguments().getString("email");
        final String password = getArguments().getString("password");
        final String uuid = getArguments().getString("uuid");
        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        final String versionRelease = Build.VERSION.RELEASE;

        JSONObject params = new JSONObject();
        try {
            params.put("username", email);
            params.put("password", password);
            params.put("os_version", versionRelease);
            params.put("device", manufacturer + " " + model);
            params.put("uuid", uuid);
        } catch (JSONException e) {
            respond(true, getString(R.string.login_call_error), "", "", "");
        }

        String URL = getString(R.string.kBaseUrl) + "authentication/";

        RequestQueueManager.getInstance().addToRequestQueue(
                new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("LoginTask", "r " + response );
                        try {
                            JSONObject data = response.getJSONObject("data");
                            String token = data.getString("token");
                            String fullName = data.getString("user_full_name");
                            String organisationName = data.getString("organisation_name");
                            respond(false, "", token, fullName, organisationName);
                        } catch (JSONException e) {
                            respond(true, getString(R.string.invalid_server_response), "", "", "");
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            String message = getString(R.string.connection_error);
                            respond(true, message, "", "", "");
                            return;
                        }

                        String message = Utils.generateErrorMessage(error, getString(R.string.invalid_server_response));
                        respond(true, message, "", "", "");
                    }
                }){

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Accept", "application/json");
                        return headers;
                    }
                }, getContext());
    }

    /** Convenience method to send back a response. Checks that the state is correct before sending.*/
    private void respond(boolean isError, String message, @NonNull String token, String fullName,
                         String organisationName) {
        if (isAdded() && mCallbacks != null) {
            mCallbacks.onLoginComplete(isError, message, token, fullName, organisationName);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}
