package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.FirebaseTokenUpdater;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;

public class LoginViewModel extends AndroidViewModel {

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy logging in.
        LOGGING_IN,
    }

    public enum Event {
        // When the login has completed successfully.
        LOG_IN_DONE,

        // When an error occurred during login.
        LOG_IN_ERROR,
    }

    /** keeps track of the remember me option. */
    public MutableLiveData<Boolean> mRememberMe;

    /** Remember the user name. */
    public MutableLiveData<String> mUsername;

    /** Remember the user password. */
    public MutableLiveData<String> mPassword;

    /** Handle the network status. */
    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    /** Constructor. */
    public LoginViewModel(Application application) {
        super(application);

        mRememberMe = new MutableLiveData<>();
        mUsername = new MutableLiveData<>();
        mPassword = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mEventHandler = new SingleLiveEvent<>();

        boolean remember = ((WelfareApplication) getApplication()).getRememberMe();
        mRememberMe.setValue(remember);
        if (remember) {
            mUsername.setValue(((WelfareApplication) getApplication()).getUsername());
            mPassword.setValue(((WelfareApplication) getApplication()).getPassword());
//            mEventHandler.setValue(new Pair<Event, String>(Event.DISABLE_EYE, ""));
        } else {
            mUsername.setValue("");
            mPassword.setValue("");
        }
    }

    public LiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public LiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }

    /** Start the fragment to log the user in. */
    public void startLogin() {
        mNetworkHandler.setValue(NetworkStatus.LOGGING_IN);
        String uuid = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
        final String manufacturer = Build.MANUFACTURER;
        final String model = Build.MODEL;
        final String versionRelease = Build.VERSION.RELEASE;

        JSONObject params = new JSONObject();
        try {
            params.put("username", mUsername.getValue());
            params.put("password", mPassword.getValue());
            params.put("os_version", versionRelease);
            params.put("device", manufacturer + " " + model);
            params.put("uuid", uuid);
        } catch (JSONException e) {
            mEventHandler.setValue(new Pair<>(Event.LOG_IN_ERROR, getApplication().getString(R.string.login_call_error)));
            mNetworkHandler.setValue(NetworkStatus.IDLE);
            return;
        }

        String URL = getApplication().getString(R.string.kBaseUrl) + "authentication/";

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

                            // Build the animal types list
                            JSONArray animalTypesJSONArray = data.getJSONArray("animal_types");
                            List<AnimalType> animalTypes = new ArrayList<>();
                            for (int i = 0; i < animalTypesJSONArray.length(); i++) {
                                final JSONObject animalType = animalTypesJSONArray.getJSONObject(i);
                                animalTypes.add(new AnimalType(animalType.getInt("id"), animalType.getString("description")));
                            }

                            JSONArray permissionsJSONArray = data.getJSONArray("user_permissions");
                            List<String> permissions = new ArrayList<>();
                            for (int i = 0; i < permissionsJSONArray.length(); i++) {
                                permissions.add(permissionsJSONArray.getString(i));
                            }
                            handleLoginComplete(token, fullName, organisationName, animalTypes, permissions);
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.LOG_IN_ERROR, getApplication().getString(R.string.invalid_server_response)));
                            mNetworkHandler.setValue(NetworkStatus.IDLE);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError) {
                            mEventHandler.setValue(new Pair<>(Event.LOG_IN_ERROR, getApplication().getString(R.string.connection_error)));
                            mNetworkHandler.setValue(NetworkStatus.IDLE);
                            return;
                        }
                        mEventHandler.setValue(new Pair<>(Event.LOG_IN_ERROR, getApplication().getString(R.string.invalid_server_response)));
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }){

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Accept", "application/json");
                        return headers;
                    }
                }, getApplication());

    }

    private void handleLoginComplete(final String sessionToken, String fullName, String organisationName, List<AnimalType> animalTypes, List<String> permissions) {
        if (mRememberMe.getValue() != null && mRememberMe.getValue()) {
            updateSharedPreferences();
        }

        ((WelfareApplication)getApplication()).setLoginDetails(sessionToken, fullName, organisationName);
        ((WelfareApplication)getApplication()).setPermissions(permissions);

        // Set utility lists
        ((WelfareApplication)getApplication()).setAnimalTypes(animalTypes);

        // Update the firebase token for the user
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("LoginActivity", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Initialise and run a FirebaseTokenUpdater
                        new FirebaseTokenUpdater(getApplication().getApplicationContext(), token, sessionToken).start();

                        // Log and toast
                        Log.d("LoginActivity", token);
                        Toast.makeText(getApplication(), token, Toast.LENGTH_SHORT).show();
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                        mEventHandler.setValue(new Pair<>(Event.LOG_IN_DONE, ""));
                    }
                });
    }

    /**
     * Updates the shared preferences to remember username, password and remember state.
     *
     * Please note: this method does not check for login success! Only saves based on checkbox.
     */
    public void updateSharedPreferences() {
        ((WelfareApplication) getApplication()).setLoginData(
                mUsername.getValue(),
                mPassword.getValue(),
                mRememberMe.getValue());
    }

}
