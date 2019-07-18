package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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

public class ResidenceViewModel extends AndroidViewModel {

    //TODO: Use this for a new entry as wel!
    //TODO: :On add animal, new fragment should be aware that we are not editing an animal but adding one!

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy retrieving data for this residence.
        RETRIEVING_DATA,
    }

    /** Remember the user name. */
    private Integer residenceID; //TODO: on new this must not be used.

    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public MutableLiveData<String> mAddress;
    public MutableLiveData<String> mShackID;
    public MutableLiveData<String> mLat;
    public MutableLiveData<String> mLon;
    public MutableLiveData<String> mNotes;
    public MutableLiveData<String> mAnimalList; //TODO: will be objects

    public MutableLiveData<NetworkStatus> mNetworkHandler;

    private String mAddressSave, mShackIDSave, mLatSave, mLongSave, mNotesSave; //Use these to store
    //// values on edit, so that values may be reset on cancel edit

    public ResidenceViewModel(Application app) {
        super(app);
        mAddress = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();

        mShackID = new MutableLiveData<>();
        mLat = new MutableLiveData<>(); //TODO
        mLon = new MutableLiveData<>(); //TODO
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>(); //TODO
        //todo: saved instance!
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int resID) {
        //TODO: On edit if isnew, should we have special actions?
        // On edit if isnew, cancel button sould finish the activity.
        mEditMode.setValue(isNew);
        if (!isNew) {
            loadData(resID);
        }

    }

    private void loadData(int resID) {
        if(resID >= 0) {
            mNetworkHandler.setValue(NetworkStatus.RETRIEVING_DATA);

            Map<String, String> params = new HashMap<>();
            params.put("residence_id", Integer.toString(resID));

            String baseURL = getApplication().getString(R.string.kBaseUrl) + "residences/details";
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
                                    JSONObject res = data.getJSONObject("residence_details");


                                        int id = res.getInt("id");
                                        String shackID = res.optString("shack_id");
                                        String streetAddress = res.optString("street_address");
                                        String lat = res.optString("latitude");
                                        String lon = res.optString("longitude");
                                        String notes = res.optString("notes");
//                                        int dist = entry.optInt("distance", 0); TODO
//                                        String animals = entry.optString("animals");
                                    residenceID = id;
                                    mShackID.setValue(shackID);
                                    mAddress.setValue(streetAddress);
                                    mLat.setValue(lat);
                                    mLon.setValue(lon);
                                    mNotes.setValue(notes);
                                }
                            } catch (JSONException e) {
                                //TODO: HANDLE ERROR
//                                mEventHandler.setValue(new Pair<>(HomeViewModel.Event.SEARCH_RES_ERROR, getApplication().getString(R.string.internal_error_res_search)));
                            }
                            mNetworkHandler.setValue(NetworkStatus.IDLE);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    mNetworkHandler.setValue(NetworkStatus.IDLE);
                    //TODO:
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
//                        mEventHandler.setValue(new Pair<>(HomeViewModel.Event.SEARCH_RES_ERROR, getApplication().getString(R.string.conn_error_res_search)));
//                    } else {
//                        mEventHandler.setValue(new Pair<>(HomeViewModel.Event.SEARCH_RES_ERROR, getApplication().getString(R.string.unknown_error_res_search)));
                    }
//                    mResidenceSearchResults.setValue(null);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + ((WelfareApplication) getApplication()).getToken());
                    return headers;
                }
            }, getApplication());
        }
    }

    // Should set to TRUE if editable.
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
    }

    /** Either enable edit, or if its enabled already start the saving process. */
    public void toggleSaveEdit() {
        Boolean currentEdit = mEditMode.getValue();
        if (currentEdit != null) {

            if (!currentEdit) {
                mAddressSave = mAddress.getValue();
                mShackIDSave = mShackID.getValue();
                mShackIDSave = mShackID.getValue();
                mLatSave = mLat.getValue();
                mLongSave = mLon.getValue();
                mNotesSave = mNotes.getValue();
                mEditMode.setValue(true);
            } else {
                // Do save actions to backend.
                // set editable back to false once done
                saveData();
            }
        }
    }

    /* Cancel the current edit and reset the values. */
    public void cancelEdit() {
        mEditMode.setValue(false);
        mAddress.setValue(mAddressSave);
        mShackID.setValue(mShackIDSave);
        mLat.setValue(mLatSave);
        mLon.setValue(mLongSave);
        mNotes.setValue(mNotesSave);
    }

    private void saveData() {
        mEditMode.setValue(false);
    }
}
//
//	"residence_id":5, to be null or missing on ADD
//            "shack_id":"56",
//            "street_address":"1 test street",
//            "latitude":55.5555,
//            "longitude":55.5555,
//            "notes":"test notes"
