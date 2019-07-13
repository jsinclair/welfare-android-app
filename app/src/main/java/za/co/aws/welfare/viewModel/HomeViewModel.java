package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.RequestQueueManager;

public class HomeViewModel extends AndroidViewModel {

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Waiting for residence feedback
        SEARCHING_RESIDENCE,
    }

    /** Remember the last searched address entry. Allows us to show the last filter/result that
     * the user entered. SO for example, if they are doing a census in a particular road, the dont
     * have to redo the search (and spend more data) every time. TODO: STORE RESULTS HERE TOO IN A MUTLD</>  */
    public MutableLiveData<String> mResidenceAddressSearch;
    public MutableLiveData<String> mShackIDSearch;
    public MutableLiveData<String> mLatLongSearch; //TODO!

    public MutableLiveData<NetworkStatus> mNetworkHandler;

    public HomeViewModel(Application application) {
        super(application);
        mResidenceAddressSearch = new MutableLiveData<>();
        mShackIDSearch = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();

        mResidenceAddressSearch.setValue("TEST");
    }


    public LiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public void doResidenceSearch() {
        //TODO: LAT LONG PART?
        mNetworkHandler.setValue(NetworkStatus.SEARCHING_RESIDENCE);
//        String uuid = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
//        final String manufacturer = Build.MANUFACTURER;
//        final String model = Build.MODEL;
//        final String versionRelease = Build.VERSION.RELEASE;
//
//        JSONObject params = new JSONObject();
//        try {
//            params.put("username", mUsername.getValue());
//            params.put("password", mPassword.getValue());
//            params.put("os_version", versionRelease);
//            params.put("device", manufacturer + " " + model);
//            params.put("uuid", uuid);
//        } catch (JSONException e) {
//            mEventHandler.setValue(new Pair<>(LoginViewModel.Event.LOG_IN_ERROR, getApplication().getString(R.string.login_call_error)));
//            mNetworkHandler.setValue(LoginViewModel.NetworkStatus.IDLE);
//            return;
//        }
//
//        String URL = getApplication().getString(R.string.kBaseUrl) + "authentication/";
//
//        RequestQueueManager.getInstance().addToRequestQueue(
//                new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        Log.i("LoginTask", "r " + response );
//                        try {
//                            JSONObject data = response.getJSONObject("data");
//                            String token = data.getString("token");
//                            String fullName = data.getString("user_full_name");
//                            String organisationName = data.getString("organisation_name");
//
//                            // Build the animal types list
//                            JSONArray animalTypesJSONArray = data.getJSONArray("animal_types");
//                            List<AnimalType> animalTypes = new ArrayList<>();
//                            for (int i = 0; i < animalTypesJSONArray.length(); i++) {
//                                final JSONObject animalType = animalTypesJSONArray.getJSONObject(i);
//                                animalTypes.add(new AnimalType(animalType.getInt("id"), animalType.getString("description")));
//                            }
//
//                            JSONArray permissionsJSONArray = data.getJSONArray("user_permissions");
//                            List<String> permissions = new ArrayList<>();
//                            for (int i = 0; i < permissionsJSONArray.length(); i++) {
//                                permissions.add(permissionsJSONArray.getString(i));
//                            }
//                            handleLoginComplete(token, fullName, organisationName, animalTypes, permissions);
//                        } catch (JSONException e) {
//                            mEventHandler.setValue(new Pair<>(LoginViewModel.Event.LOG_IN_ERROR, getApplication().getString(R.string.invalid_server_response)));
//                            mNetworkHandler.setValue(LoginViewModel.NetworkStatus.IDLE);
//                        }
//
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        if (error instanceof NoConnectionError) {
//                            mEventHandler.setValue(new Pair<>(LoginViewModel.Event.LOG_IN_ERROR, getApplication().getString(R.string.connection_error)));
//                            mNetworkHandler.setValue(LoginViewModel.NetworkStatus.IDLE);
//                            return;
//                        }
//                        mEventHandler.setValue(new Pair<>(LoginViewModel.Event.LOG_IN_ERROR, getApplication().getString(R.string.invalid_server_response)));
//                        mNetworkHandler.setValue(LoginViewModel.NetworkStatus.IDLE);
//                    }
//                }){
//
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        HashMap<String, String> headers = new HashMap<>();
//                        headers.put("Accept", "application/json");
//                        return headers;
//                    }
//                }, getApplication());
    }

}
