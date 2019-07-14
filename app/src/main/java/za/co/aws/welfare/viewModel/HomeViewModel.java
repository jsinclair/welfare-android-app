package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.utils.NetworkUtils;
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
    public MutableLiveData<LinkedList<ResidenceSearchData>> mResidenceSearchResults;
    public MutableLiveData<String> mLatLongSearch; //TODO!

    public MutableLiveData<NetworkStatus> mNetworkHandler;

    public HomeViewModel(Application application) {
        super(application);
        mResidenceAddressSearch = new MutableLiveData<>();
        mShackIDSearch = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mResidenceSearchResults = new MutableLiveData<>();

        mResidenceAddressSearch.setValue("TEST");
    }


    public LiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public LiveData<LinkedList<ResidenceSearchData>> getResidentResults() {
        return mResidenceSearchResults;
    }

    public void doResidenceSearch() {
        //TODO: LAT LONG PART?
        mNetworkHandler.setValue(NetworkStatus.SEARCHING_RESIDENCE);


        String shackID = mShackIDSearch.getValue();
        String streetAddress = mResidenceAddressSearch.getValue();
        boolean hasShack = !(shackID == null || shackID.isEmpty());
        boolean hasStreet = !(streetAddress == null || streetAddress.isEmpty());

        if (false && !hasShack && !hasStreet) { //TODO: REMOVE FALSE
            //TODO: Send event
            mNetworkHandler.setValue(NetworkStatus.IDLE);
            return;
        }

        Map<String, String> params = new HashMap<>();
        if (hasShack) {
            params.put("shack_id", shackID);
        }

        if (hasStreet) {
            params.put("street_address", streetAddress);
        }

        //TODO: REMOVE TEST CODE
        params.put("lat", "-34.158124");
        params.put("lon", "18.984279");

        String baseURL = getApplication().getString(R.string.kBaseUrl) + "residences/list/";
        String url = NetworkUtils.createURL(baseURL, params);

        RequestQueueManager.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET,
                url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("EDIT>>>>>>", response.toString());
                        LinkedList<ResidenceSearchData> results = new LinkedList<>();
                        try {
                            JSONObject data = response.getJSONObject("data");
                            if (data != null) {
                                JSONArray resArr = data.getJSONArray("residences");
                                for (int i = 0; i < resArr.length(); i++) {
                                    JSONObject entry = resArr.getJSONObject(i);
                                    int id = entry.getInt("id");
                                    String shackID = entry.optString("shack_id");
                                    String streetAddress = entry.optString("street_address");
                                    String lat = entry.optString("latitude");
                                    String lon = entry.optString("longitude");
                                    int dist = entry.optInt("distance", 0);
                                    results.add(new ResidenceSearchData(id, shackID, streetAddress, lat, lon, dist));
                                }
                            }
                        } catch (JSONException e) {
                            //TODO: SHOW ERRR
                        }
                        mResidenceSearchResults.setValue(results);
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mNetworkHandler.setValue(NetworkStatus.IDLE);
                //TODO: SHOW ERROR>
//                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
//                    mEventHandler.setValue(new Pair<>(Event.NETWORK_ERROR, ""));
//                } else {
//                    mEventHandler.setValue(new Pair<>(Event.SERVER_ERROR, ""));
//                }
            }
        })
        {@Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + ((WelfareApplication)getApplication()).getToken());
            return headers;
        }
        }, getApplication());
//


        ///




    }

}
