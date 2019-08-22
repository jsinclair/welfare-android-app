package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.widget.Toast;

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
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.dataObjects.ResidentAnimalDetail;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** Controls the resident view/edit interface.*/
public class ResidenceViewModel extends AndroidViewModel {

    //TODO: on return from this activity set intent to say whether you edited something?? the calling view then knows to redo the data call.

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy retrieving data for this residence.
        RETRIEVING_DATA,

        // Busy updating residence.
        UPDATING_DATA,

        SEARCHING_PET, DELETE_RES,
    }

    public enum Event {
        // When we fail to retrieve the data from the backend.
        RETRIEVAL_ERROR,

        // If an error occurred while trying to update the residence.
        UPDATE_ERROR,

        // If the user has not provided enough data to update or create a residence.
        DATA_REQUIRED,

        SEARCH_PET_ERROR,
        DELETE_ERROR, DELETE_DONE,
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
    public MutableLiveData<String> mResidentName;
    public MutableLiveData<String> mResidentID;
    public MutableLiveData<String> mResidentTel;
    public MutableLiveData<List<ResidentAnimalDetail>> mAnimalList;

    public MutableLiveData<LinkedList<PetSearchData>> mPetSearchResult;

    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // These store the values to revert to if the user 'cancels' an edit.
    private String mAddressSave, mShackIDSave, mLatSave, mLongSave, mNotesSave, mSaveName, mSaveIDNumber, mSaveTelNumber;
    private List<ResidentAnimalDetail> mSavedAnimalList;

    public ResidenceViewModel(Application app) {
        super(app);
        mErrorState = new MutableLiveData<>();
        mAddress = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();
        mResidentName = new MutableLiveData<>();
        mResidentTel = new MutableLiveData<>();
        mResidentID = new MutableLiveData<>();

        mShackID = new MutableLiveData<>();
        mLat = new MutableLiveData<>();
        mLon = new MutableLiveData<>();
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();
        mPetSearchResult = new MutableLiveData<>();

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

    public LiveData<LinkedList<PetSearchData>> getSearchResults() {
        return mPetSearchResult;
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
                                    String resName = res.optString("resident_name");
                                    String resID = res.optString("id_no");
                                    String resTel = res.optString("tel_no");
                                    JSONArray animals = res.optJSONArray("animals");
                                    List<ResidentAnimalDetail> animalList = new LinkedList<>();
                                    if (animals != null && animals.length() != 0) {
                                        for (int i= 0; i < animals.length(); i++) {
                                            JSONObject aniEntry = animals.getJSONObject(i);
                                            int aniID = aniEntry.optInt("id", -1);
                                            String aniName = aniEntry.optString("name");
                                            animalList.add(new ResidentAnimalDetail(aniID, aniName));
                                        }
                                    }
                                    mAnimalList.setValue(animalList);
                                    residenceID = id;
                                    mShackID.setValue(shackID);
                                    mAddress.setValue(streetAddress);
                                    mLat.setValue(lat);
                                    mLon.setValue(lon);
                                    mNotes.setValue(notes);
                                    mResidentName.setValue(resName);
                                    mResidentID.setValue(resID);
                                    mResidentTel.setValue(resTel);
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
                mSaveName = mResidentName.getValue();
                mSaveIDNumber = mResidentID.getValue();
                mSaveTelNumber = mResidentTel.getValue();

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

        mResidentName.setValue(mSaveName);
        mResidentID.setValue(mSaveIDNumber);
        mResidentTel.setValue(mSaveTelNumber);

    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        String address = mAddress.getValue();
        String shackID = mShackID.getValue();
        String lat = mLat.getValue();
        String lon = mLon.getValue();
        String notes = mNotes.getValue();

        String resName = mResidentName.getValue();
        String resTel = mResidentTel.getValue();
        String resID = mResidentID.getValue();

        // Ensure the user provides some form of address.
        if ((address == null || address.isEmpty()) && (shackID == null || shackID.isEmpty())) {
            mEventHandler.setValue(new Pair<Event, String>(Event.DATA_REQUIRED, getApplication().getString(R.string.address_shack_req)));
            return;
        }

        if (isNew) {
            doUpdate(-1, address, shackID, resName, resID, resTel, lat, lon, notes);
        } else {
            boolean hasChanged = ((address != null && !address.equals(mAddressSave))
                    || (shackID != null && !shackID.equals(mShackIDSave))
                    || (notes !=null && !notes.equals(mNotesSave))
                    || (resName != null && !resName.equals(mSaveName))
                    || (resTel != null && !resTel.equals(mSaveTelNumber))
                    || (resID != null && !resID.equals(mSaveIDNumber))
                    || (lon != null && !lon.equals(mLongSave))
                    || (lat != null && !lat.equals(mLatSave)));

            List<ResidentAnimalDetail> pets = mAnimalList.getValue();
            if (!((mSavedAnimalList == null || mSavedAnimalList.isEmpty()) && (pets == null || pets.isEmpty()))) {
                if ((pets == null && !mSavedAnimalList.isEmpty()) || (pets.size() != mSavedAnimalList.size())) { //todo; check logic here
                    hasChanged = true;
                } else {
                    for (ResidentAnimalDetail pet : mSavedAnimalList) {
                        if (!pets.contains(pet)) {
                            hasChanged = true;
                            break;
                        }
                    }
                }
            }

            if (hasChanged) {
               doUpdate(residenceID, address, shackID, resName, resID, resTel, lat, lon, notes);
            } else {
                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
                        Toast.LENGTH_LONG).show();
                mEditMode.setValue(false);
            }
        }
    }

    /** Send the update to the backend and handle the result. */
    private void doUpdate(int id, String address, String shack, String resName, String resID, String resTel, String lat, String lon, String notes) {
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
            params.put("resident_name", resName == null ? "" : resName);
            params.put("id_no", resID == null ? "" : resID);
            params.put("tel_no", resTel == null ? "" : resTel);

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
                            residenceID = data.getInt("residence_id");
                            Toast.makeText(getApplication(), msg,
                                    Toast.LENGTH_LONG).show();

                            // If an edit managed to occur at all, we might need to reload the calling class.
                            successfulEditOccurred = true;
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.res_update_unknown_err)));
                        }
                        mEditMode.setValue(false);
                        isNew = false;
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

    /** call this to add a pet to the residence. Will only be persisted on save. */
    public void addPet(ResidentAnimalDetail petToAdd) {
        if (petToAdd != null) {
            List<ResidentAnimalDetail> list = mAnimalList.getValue();
            if (list == null) {
               list = new LinkedList<>();
            }
            boolean hasAni = false;
            for (ResidentAnimalDetail ani: list) {
                if (ani.getID() == petToAdd.getID()) {
                    hasAni = true;
                    break;
                }
            }
            if (!hasAni) {
                list.add(petToAdd);
            }
            mAnimalList.setValue(list);
        }
    }

    /** Search for pets on the given search parameters. */
    public void doAnimalSearch(int species, String petName, String gender, String sterilised) {

        boolean hasPetName = !(petName == null || petName.isEmpty());
        boolean hasSpecies = (species > 0);
        boolean hasGender = gender != null;
        boolean hasSterilised = sterilised != null;

        mNetworkHandler.setValue(NetworkStatus.SEARCHING_PET);

        Map<String, String> params = new HashMap<>();
        if (hasPetName) {
            params.put("name", petName);
        }

        if (hasSpecies) {
            params.put("animal_type_id", Integer.toString(species));
        }

        if (hasGender) {
            params.put("gender", gender);
        }

        if (hasSterilised) {
            params.put("sterilised", sterilised);
        }

        String baseURL = getApplication().getString(R.string.kBaseUrl) + "animals/list/";
        String url = NetworkUtils.createURL(baseURL, params);

        RequestQueueManager.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET,
                url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LinkedList<PetSearchData> results = new LinkedList<>();
                        try {
                            JSONObject data = response.getJSONObject("data");
                            if (data != null) {
                                JSONArray resArr = data.getJSONArray("animals");
                                for (int i = 0; i < resArr.length(); i++) {
                                    JSONObject entry = resArr.getJSONObject(i);
                                    int id = entry.getInt("id");
                                    int animalType = entry.getInt("animal_type_id");
                                    String animalTypeDesc = entry.optString("description");
                                    String name = entry.optString("name");
                                    String dob = entry.optString("approximate_dob");
                                    String gender = entry.optString("gender");
                                    int isSterilised = entry.optInt("sterilised", -1);
                                    results.add(new PetSearchData(id, animalType, animalTypeDesc, name, dob, gender, isSterilised));
                                }
                            }
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.SEARCH_PET_ERROR, getApplication().getString(R.string.internal_error_pet_search)));
                        }
                        mPetSearchResult.setValue(results);
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mNetworkHandler.setValue(NetworkStatus.IDLE);
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    mEventHandler.setValue(new Pair<>(Event.SEARCH_PET_ERROR, getApplication().getString(R.string.conn_error_pet_search)));
                } else {
                    mEventHandler.setValue(new Pair<>(Event.SEARCH_PET_ERROR, getApplication().getString(R.string.unknown_error_pet_search)));
                }
                mPetSearchResult.setValue(null);
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

    public void permanentlyDelete() {
        if(residenceID >= 0) {
            mNetworkHandler.setValue(NetworkStatus.DELETE_RES);

            JSONObject params = new JSONObject();
            try {
                params.put("residence_id", Integer.toString(residenceID));
            }  catch (JSONException e) {
                mEventHandler.setValue(new Pair<>(Event.DELETE_ERROR, getApplication().getString(R.string.delete_error_internal_msg)));
                mNetworkHandler.setValue(NetworkStatus.IDLE);
                return;
            }

            String baseURL = getApplication().getString(R.string.kBaseUrl) + "residences/delete/";
            RequestQueueManager.getInstance().addToRequestQueue(
                    new JsonObjectRequest(Request.Method.POST, baseURL, params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject data = response.getJSONObject("data");
                                String msg = data.optString("message");
                                Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show();
                                mNetworkHandler.setValue(NetworkStatus.IDLE);
                                mEventHandler.setValue(new Pair<>(Event.DELETE_DONE, msg));
                            } catch (JSONException e) {
                                mEventHandler.setValue(new Pair<>(Event.DELETE_ERROR, getApplication().getString(R.string.delete_error_msg)));
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                mEventHandler.setValue(new Pair<>(Event.DELETE_ERROR, getApplication().getString(R.string.delete_error_timeout_msg)));
                            } else {
                                String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.delete_error_msg));
                                mEventHandler.setValue(new Pair<>(Event.DELETE_ERROR, errorMSG));
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
    }
}
