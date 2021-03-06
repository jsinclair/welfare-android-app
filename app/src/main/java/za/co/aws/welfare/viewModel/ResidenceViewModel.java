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
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** Controls the resident view/edit interface.*/
public class ResidenceViewModel extends AndroidViewModel implements SearchPetsFragment.PetSearcher {

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

        DELETE_ERROR,
        DELETE_DONE,

        // If the user is adding a pet from the search res, we want to return correctly.
        SPECIAL_ADD_DONE,
    }

    /** Remember the user name. */
    private Integer residenceID;
    private boolean isNew;
    private boolean fromSearch;
    private boolean successfulEditOccurred;

    // This should only be true if a NEW pet has been added, which needs to be added to the parent search list.
    private boolean mAddToParent;
    private PetMinDetail mRemoveRequest;

    public final MutableLiveData<Boolean> mErrorState;

    public final MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public final MutableLiveData<String> mAddress;
    public final MutableLiveData<String> mShackID;
    public final MutableLiveData<String> mLat;
    public final MutableLiveData<String> mLon;
    public final MutableLiveData<String> mNotes;
    public final MutableLiveData<String> mResidentName;
    public final MutableLiveData<String> mResidentID;
    public final MutableLiveData<String> mResidentTel;
    public final MutableLiveData<List<PetMinDetail>> mAnimalList;

    private final MutableLiveData<NetworkStatus> mNetworkHandler;
    private final SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // These store the values to revert to if the user 'cancels' an edit.
    private String mAddressSave, mShackIDSave, mLatSave, mLongSave, mNotesSave, mSaveName, mSaveIDNumber, mSaveTelNumber;
    private List<PetMinDetail> mSavedAnimalList;

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

        mEventHandler = new SingleLiveEvent<>();
        successfulEditOccurred = false;
        mAddToParent = false;
        //todo: saved instance!
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int resID, boolean fromSearch) {
        this.isNew = isNew;
        this.fromSearch = fromSearch;
        mEditMode.setValue(isNew);
        if (!isNew) {
            loadData(resID);
        }
    }


    public boolean editOccurred() {
        return successfulEditOccurred;
    }

    public boolean shouldAddToParent() {
        return mAddToParent;
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
                                    List<PetMinDetail> animalList = new LinkedList<>();
                                    if (animals != null && animals.length() != 0) {
                                        for (int i= 0; i < animals.length(); i++) {
                                            JSONObject aniEntry = animals.getJSONObject(i);
                                            int aniID = aniEntry.optInt("id", -1);
                                            String aniName = aniEntry.optString("name");
                                            int sterilised = aniEntry.optInt("sterilised", -1);
                                            animalList.add(new PetMinDetail(aniID, aniName, sterilised));
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
                public Map<String, String> getHeaders() {
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
    public MutableLiveData<List<PetMinDetail>> getAnimalList() {
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
            mEventHandler.setValue(new Pair<>(Event.DATA_REQUIRED, getApplication().getString(R.string.address_shack_req)));
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

            List<PetMinDetail> pets = mAnimalList.getValue();
            if (!((mSavedAnimalList == null || mSavedAnimalList.isEmpty()) && (pets == null || pets.isEmpty()))) {
                if ((pets == null && !mSavedAnimalList.isEmpty()) || (pets.size() != mSavedAnimalList.size())) {
                    hasChanged = true;
                } else {
                    for (PetMinDetail pet : mSavedAnimalList) {
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

            List<PetMinDetail> animalList = mAnimalList.getValue();
            if (animalList != null) {
                JSONArray animalIDs = new JSONArray();
                for (PetMinDetail det: animalList) {
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

                            if (isNew) {
                                mAddToParent = true;
                            }
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.res_update_unknown_err)));
                            return;
                        }
                        mEditMode.setValue(false);
                        isNew = false;
                        if (fromSearch) {
                            mEventHandler.setValue(new Pair<>(Event.SPECIAL_ADD_DONE, ""));
                        }
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
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + ((WelfareApplication) getApplication()).getToken());
                        return headers;
                    }
                }, getApplication());
    }

    public void setRemoveRequest(PetMinDetail deleteRequest) {
        this.mRemoveRequest = deleteRequest;
    }

    public void removePet() {
        if (mRemoveRequest != null) {
           List<PetMinDetail> list = mAnimalList.getValue();
           if (list != null) {
               list.remove(mRemoveRequest);
           }
           mAnimalList.setValue(list);
        }
    }

    /** call this to add a pet to the residence. Will only be persisted on save. */
    public void onPetSelected(PetMinDetail petToAdd) {
        if (petToAdd != null) {
            List<PetMinDetail> list = mAnimalList.getValue();
            if (list == null) {
               list = new LinkedList<>();
            }
            boolean hasAni = false;
            for (PetMinDetail ani: list) {
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

    public void permanentlyDelete() {
        if(residenceID != null && residenceID >= 0) {
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
                        public Map<String, String> getHeaders() {
                            HashMap<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "Bearer " + ((WelfareApplication) getApplication()).getToken());
                            return headers;
                        }
                    }, getApplication());
        }
    }

    public Integer getResidenceID() {
        return residenceID == null ? -1 : residenceID;
    }

    public String getAddress() {
        return mAddress.getValue() == null? "": mAddress.getValue();
    }

    public boolean fromSearch() {
        return fromSearch;
    }
}
