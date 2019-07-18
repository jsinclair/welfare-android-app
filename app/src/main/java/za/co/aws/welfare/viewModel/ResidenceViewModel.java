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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;
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
    public MutableLiveData<List<ResidentAnimalDetail>> mAnimalList; //TODO: show on UI

    public MutableLiveData<NetworkStatus> mNetworkHandler;

    // These store the values to revert to if the user 'cancels' an edit.
    private String mAddressSave, mShackIDSave, mLatSave, mLongSave, mNotesSave;

    public ResidenceViewModel(Application app) {
        super(app);
        mAddress = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();

        mShackID = new MutableLiveData<>();
        mLat = new MutableLiveData<>(); //TODO
        mLon = new MutableLiveData<>(); //TODO
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();
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
                                    JSONArray animals = res.optJSONArray("animals");
                                    List<ResidentAnimalDetail> animalList = new LinkedList<>();
                                    if (animals != null && animals.length() != 0) {
                                        for (int i= 0; i < animals.length(); i++) {
                                            JSONObject aniEntry = animals.getJSONObject(i);
                                            int aniID = aniEntry.optInt("id", -1);
                                            String aniName = aniEntry.optString("name");
                                            String aniWelfareNum = aniEntry.optString("welfare_number");
                                            animalList.add(new ResidentAnimalDetail(aniID, aniName, aniWelfareNum));
                                        }
                                    }
                                    mAnimalList.setValue(animalList);
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
        //TODO: Backend call.
        mEditMode.setValue(false);
    }
}
