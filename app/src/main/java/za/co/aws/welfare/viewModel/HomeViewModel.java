package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.core.util.Pair;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;

/** Handles the search fragments. */
public class HomeViewModel extends AndroidViewModel {

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Waiting for residence feedback
        SEARCHING_RESIDENCE,
    }

    /** Use this for one-time events. */
    public enum Event {
        // If an error occurred while trying to search for residence.
        SEARCH_RES_ERROR,

        // If the user did not provide search data
        SEARCH_RES_DATA_REQ,
    }

    public enum Navigate {
        RESIDENCE, ADD_RESIDENCE, ANIMAL
    }

    /** Remember the last searched address entry. Allows us to show the last filter/result that
     * the user entered. SO for example, if they are doing a census in a particular road, the dont
     * have to redo the search (and spend more data) every time. */
    public MutableLiveData<String> mResidenceAddressSearch;

    /** Remember the last entry used for shack id. */ //todo: might change to only update on SEARCH pressed.
    public MutableLiveData<String> mShackIDSearch;

    /** Remember the last entry used for lat / lon. STILL TODO!*/
    public MutableLiveData<String> mLatLongSearch; //TODO!

    /** Holds the results of the last search done. */
    public MutableLiveData<LinkedList<ResidenceSearchData>> mResidenceSearchResults;

    /** Use this for indicating the network usage. Remember to always reset back to idle. */
    public MutableLiveData<NetworkStatus> mNetworkHandler;

    /** Use this for one-time events. */
    public SingleLiveEvent<Pair<Event, String>> mEventHandler;


    public SingleLiveEvent<Pair<Navigate, Integer>> mNavigationHandler;

    public HomeViewModel(Application application) {
        super(application);
        mResidenceAddressSearch = new MutableLiveData<>();
        mShackIDSearch = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mEventHandler = new SingleLiveEvent<>();
        mNavigationHandler = new SingleLiveEvent<>();
        mResidenceSearchResults = new MutableLiveData<>();
    }

    /** Use this to respond to network changes. */
    public LiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    /** Use this to respond to one time events. */
    public LiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }

    /** Use this to respond to one time events. */
    public LiveData<Pair<Navigate, Integer>> getNavigationHandler() {
        return mNavigationHandler;
    }

    /** Use this to respond to search result changes. */
    public LiveData<LinkedList<ResidenceSearchData>> getResidentResults() {
        return mResidenceSearchResults;
    }

    public void doResidenceSearch() {
        //TODO: LAT LONG PART?
        String shackID = mShackIDSearch.getValue();
        String streetAddress = mResidenceAddressSearch.getValue();
        boolean hasShack = !(shackID == null || shackID.isEmpty());
        boolean hasStreet = !(streetAddress == null || streetAddress.isEmpty());

        if (false && !hasShack && !hasStreet) { //TODO: REMOVE FALSE & add lat/lon check too
            mEventHandler.setValue(new Pair<>(Event.SEARCH_RES_DATA_REQ, getApplication().getString(R.string.res_search_data_required)));
            return;
        }

        mNetworkHandler.setValue(NetworkStatus.SEARCHING_RESIDENCE);

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
                                    String animals = entry.optString("animals");
                                    results.add(new ResidenceSearchData(id, shackID, streetAddress, lat, lon, animals));
                                }
                            }
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.SEARCH_RES_ERROR, getApplication().getString(R.string.internal_error_res_search)));
                        }
                        mResidenceSearchResults.setValue(results);
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mNetworkHandler.setValue(NetworkStatus.IDLE);
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    mEventHandler.setValue(new Pair<>(Event.SEARCH_RES_ERROR, getApplication().getString(R.string.conn_error_res_search)));
                } else {
                    mEventHandler.setValue(new Pair<>(Event.SEARCH_RES_ERROR, getApplication().getString(R.string.unknown_error_res_search)));
                }
                mResidenceSearchResults.setValue(null);
            }
        })
        {@Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + ((WelfareApplication)getApplication()).getToken());
            return headers;
        }
        }, getApplication());
    }

    public void triggerViewResident(int id) {
        mNavigationHandler.setValue(new Pair<>(Navigate.RESIDENCE, id));
    }

    public void triggerAddResident() {
        mNavigationHandler.setValue(new Pair<>(Navigate.ADD_RESIDENCE, -1));

    }

}
