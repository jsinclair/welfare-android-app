package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.widget.Toast;

import androidx.core.util.Pair;
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
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** Controls the resident view/edit interface.*/
public class ResidenceViewModel extends AndroidViewModel {

    //TODO: Add a delete res option. Decide what should happen with animals
    //TODO: :On add animal, new fragment should be aware that we are not editing an animal but adding one!
    //TODO: On edit, have an add animal button available!!!
    //TODO: on return from this activity set intent to say whether you edited something?? the calling view then knows to redo the data call.
    //TODO: On back pressed, if in edit mode then just cancel edit?
    //TODO: SET TITLE

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy retrieving data for this residence.
        RETRIEVING_DATA,

        // Busy updating residence.
        UPDATING_DATA,
    }

    public enum Event {
        // When we fail to retrieve the data from the backend.
        RETRIEVAL_ERROR,

        // If an error occurred while trying to update the residence.
        UPDATE_ERROR,

        // If the user has not provided enough data to update or create a residence.
        DATA_REQUIRED,
    }

    /** Remember the user name. */
    private Integer residenceID;
    private boolean isNew;
    private boolean successfulEditOccurred;
    private ResidentAnimalDetail mRemoveRequest;


    public MutableLiveData<Boolean> mErrorState;

    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public MutableLiveData<String> mAddress;
    public MutableLiveData<String> mShackID;
    public MutableLiveData<String> mLat;
    public MutableLiveData<String> mLon;
    public MutableLiveData<String> mNotes;
    public MutableLiveData<List<ResidentAnimalDetail>> mAnimalList;

    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // These store the values to revert to if the user 'cancels' an edit.
    private String mAddressSave, mShackIDSave, mLatSave, mLongSave, mNotesSave;
    private List<ResidentAnimalDetail> mSavedAnimalList;

    public ResidenceViewModel(Application app) {
        super(app);
        mErrorState = new MutableLiveData<>();
        mAddress = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();

        mShackID = new MutableLiveData<>();
        mLat = new MutableLiveData<>();
        mLon = new MutableLiveData<>();
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();

        mEventHandler = new SingleLiveEvent<>();
        successfulEditOccurred = false;
        //todo: saved instance!
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int resID) {
        this.isNew = isNew;
        mEditMode.setValue(isNew);
        if (!isNew) {
            loadData(resID);
        }
    }

    public boolean editOccurred() {
        return successfulEditOccurred;
    }

    /**
     * Use this to reload the data if there was an error. Could also be used if there has been a
     * change (in pets for example).
     */
    public void reloadData() {
        loadData(residenceID);
    }

    /** If this is an edit and not a new, load the existing data from the backend. */
    private void loadData(int resID) {
        if(resID >= 0) {
            residenceID = resID;
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
                                    mErrorState.setValue(false);
                                }
                            } catch (JSONException e) {
                                mErrorState.setValue(false);
                                // there is still data available or
                                // there is a data issue. So cannot reload.
                                mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, getApplication().getString(R.string.internal_error_res_search)));
                            }
                            mNetworkHandler.setValue(NetworkStatus.IDLE);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, getApplication().getString(R.string.conn_error_res_search)));
                    } else {
                        String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.unknown_error_res_search));
                        mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, errorMSG));
                    }
                    mErrorState.setValue(true);
                    mNetworkHandler.setValue(NetworkStatus.IDLE);
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

    public MutableLiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public MutableLiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }

    public MutableLiveData<Boolean> getHasDownloadError() {
        return mErrorState;
    }

    // Should set to TRUE if editable.
    public MutableLiveData<List<ResidentAnimalDetail>> getAnimalList() {
        return mAnimalList;
    }

    public boolean isNew () {
        return isNew;
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
                mSavedAnimalList = new LinkedList<>();
                if (mAnimalList != null && mAnimalList.getValue() != null) {
                    mSavedAnimalList.addAll(mAnimalList.getValue());
                }
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
        mAnimalList.setValue(mSavedAnimalList);
    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        String address = mAddress.getValue();
        String shackID = mShackID.getValue();
        String lat = mLat.getValue();
        String lon = mLon.getValue();
        String notes = mNotes.getValue();

        // Ensure the user provides some form of address.
        if ((address == null || address.isEmpty()) && (shackID == null || shackID.isEmpty())) {
            mEventHandler.setValue(new Pair<Event, String>(Event.DATA_REQUIRED, getApplication().getString(R.string.address_shack_req)));
            return;
        }

        if (isNew) {
            doUpdate(-1, address, shackID, lat, lon, notes);
        } else {
            boolean hasChanged = ((address != null && !address.equals(mAddressSave))
                    || (shackID != null && !shackID.equals(mShackIDSave))
                    || (notes !=null && !notes.equals(mNotesSave))
                    || (lon != null && !lon.equals(mLongSave))
                    || (lat != null && !lat.equals(mLatSave)));

            List<ResidentAnimalDetail> pets = mAnimalList.getValue();
            if (!((mSavedAnimalList == null || mSavedAnimalList.isEmpty()) && (pets == null || pets.isEmpty()))) {
                if ((pets == null && !mSavedAnimalList.isEmpty()) || (pets.size() != mSavedAnimalList.size())) {
                    hasChanged = true;
                } else {
                    for (ResidentAnimalDetail pet : mSavedAnimalList) {
                        if (!pets.contains(pet)) {
                            hasChanged = true;
                            break;
                        }
                    }
                }
            }//TODO: TEST TEST TEST

            if (hasChanged) {
               doUpdate(residenceID, address, shackID, lat, lon, notes);
            } else {
                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
                        Toast.LENGTH_LONG).show();
                mEditMode.setValue(false);
            }
        }
    }

    /** Send the update to the backend and handle the result. */
    private void doUpdate(int id, String address, String shack, String lat, String lon, String notes) {
        mNetworkHandler.setValue(NetworkStatus.UPDATING_DATA);
        JSONObject params = new JSONObject();
        try {
            if (!isNew) {
                params.put("residence_id", id);
            }
            params.put("shack_id", shack == null ? "" : shack);
            params.put("street_address", address == null ? "" : address);
            params.put("latitude", lat == null ? "" : lat);
            params.put("longitude", lon == null ? "" : lon);
            params.put("notes", notes == null ? "" : notes);

            List<ResidentAnimalDetail> animalList = mAnimalList.getValue();
            if (animalList != null) {
                JSONArray animalIDs = new JSONArray();
                for (ResidentAnimalDetail det: animalList) {
                    animalIDs.put(det.getID());
                }
                params.put("animals", animalIDs);
            }

        } catch (JSONException e) {
            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.res_update_internal_err)));
            mNetworkHandler.setValue(NetworkStatus.IDLE);
            return;
        }

        String URL = getApplication().getString(R.string.kBaseUrl) + "residences/update/";

        RequestQueueManager.getInstance().addToRequestQueue(
                new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            String msg = data.getString("message");

                            Toast.makeText(getApplication(), msg,
                                    Toast.LENGTH_LONG).show();

                            // If an edit managed to occur at all, we might need to reload the calling class.
                            successfulEditOccurred = true;
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.res_update_unknown_err)));
                        }
                        mEditMode.setValue(false);
                        isNew = false; //Set to not new value.
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, getApplication().getString(R.string.res_update_conn_err)));
                        } else {
                            String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.res_update_unknown_err));
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, errorMSG));
                        }
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
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

    public void setRemoveRequest(ResidentAnimalDetail deleteRequest) {
        this.mRemoveRequest = deleteRequest;
    }

    public void removePet() {
        if (mRemoveRequest != null) {
           List<ResidentAnimalDetail> list = mAnimalList.getValue();
           if (list != null) {
               list.remove(mRemoveRequest);
           }
           mAnimalList.setValue(list);
        }
    }
}
