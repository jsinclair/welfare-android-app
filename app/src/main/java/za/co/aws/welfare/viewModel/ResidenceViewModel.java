package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.widget.Toast;

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
import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;

/** Controls the resident view/edit interface.*/
public class ResidenceViewModel extends AndroidViewModel {

    //TODO: Use this for a new entry as wel!
    //TODO: Add a delete res option. Decide what should happen with animals
    //TODO: :On add animal, new fragment should be aware that we are not editing an animal but adding one!
    //TODO: On edit, have an add animal button available!!!
    //TODO: on return from this activity set intent to say whether you edited something?? the calling view then knows to redo the data call.
    //TODO: On back pressed, if in edit mode then just cancel edit?

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy retrieving data for this residence.
        RETRIEVING_DATA,
        UPDATING_DATA,
    }

    /** Remember the user name. */
    private Integer residenceID; //TODO: on new this must not be used.
    private boolean isNew; //TODO: USE!!

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
        this.isNew = isNew;
        //TODO: On edit if isnew, should we have special actions?
        // On edit if isnew, cancel button sould finish the activity.
        mEditMode.setValue(isNew);
        if (!isNew) {
            loadData(resID);
        }
    }

    /** If this is an edit and not a new, load the existing data from the backend. */
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

    // Should set to TRUE if editable.
    public MutableLiveData<List<ResidentAnimalDetail>> getAnimalList() {
        return mAnimalList;
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
        //TODO: if isnew, finish activity.
    }

    private void saveData() {

        String address = mAddress.getValue();
        String shackID = mShackID.getValue();
        String lat = mLat.getValue();
        String lon = mLon.getValue();
        String notes = mNotes.getValue();


        if (isNew) {
            //TODO: VALIDATE??
        } else {
            boolean hasChanged = ((address != null && !address.equals(mAddressSave))
                    || (shackID != null && !shackID.equals(mShackIDSave))
                    || (notes !=null && !notes.equals(mNotesSave))
                    || (lat != null && !lon.equals(mLongSave))
                    || (lat != null && !lat.equals(mLatSave)));
            if (hasChanged) {
               doUpdate(residenceID, address, shackID, lat, lon, notes);
            } else {
                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
                        Toast.LENGTH_LONG).show();
                mEditMode.setValue(false);
            }
        }
        //TODO IF isnew, set editble false and isnew false on success.

    }

    /** Send the update to the backend and handle the result. */
    private void doUpdate(int id, String address, String shack, String lat, String lon, String notes) {
        mNetworkHandler.setValue(NetworkStatus.UPDATING_DATA);

        JSONObject params = new JSONObject();
        try {
            if (!isNew) {
                params.put("residence_id", id);
            }
            params.put("shack_id", shack);
            params.put("street_address", address);
            params.put("latitude", lat);
            params.put("longitude", lon);
            params.put("notes", notes);
        } catch (JSONException e) {
            //TODO
//            mEventHandler.setValue(new Pair<>(LoginViewModel.Event.LOG_IN_ERROR, getApplication().getString(R.string.login_call_error)));
            mNetworkHandler.setValue(NetworkStatus.IDLE);
            return;
        }

        String URL = getApplication().getString(R.string.kBaseUrl) + "residences/update/";

        RequestQueueManager.getInstance().addToRequestQueue(
                new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       //TODO:
                        Toast.makeText(getApplication(), getApplication().getString(R.string.update_successful),
                                Toast.LENGTH_LONG).show();
                        mEditMode.setValue(false);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //TODO Check for TIMEOUT and check for feedback message.
//                        mEventHandler.setValue(new Pair<>(LoginViewModel.Event.LOG_IN_ERROR, getApplication().getString(R.string.invalid_server_response)));
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                        // On error, we stay on edit mode so that the user can decide what to do next.
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + ((WelfareApplication) getApplication()).getToken());
                        return headers;
                    }
                }, getApplication());
    }
}
