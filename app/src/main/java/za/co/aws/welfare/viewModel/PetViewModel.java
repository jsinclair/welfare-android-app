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
import za.co.aws.welfare.dataObjects.ResidenceSearchData;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** used for the Pet Activity, which allows the user to view or modify a pet. */
public class PetViewModel extends AndroidViewModel {

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy retrieving data for this pet.
        RETRIEVING_DATA,

        // Busy updating pet.
        UPDATING_DATA,

        SEARCHING_RESIDENCE,

        DELETE_PET,
    }

    public enum Event {
        // When we fail to retrieve the data from the backend.
        RETRIEVAL_ERROR,

        // If an error occurred while trying to update the pet.
        UPDATE_ERROR,

        // If the user has not provided enough data to update or create a pet.
        DATA_REQUIRED,

        SEARCH_RES_ERROR,

        DELETE_DONE,DELETE_ERROR
    }

    public static final String GENDER_UNKNOWN = Utils.GENDER_UNKNOWN;
    public static final String GENDER_MALE = Utils.GENDER_MALE;
    public static final String GENDER_FEMALE = Utils.GENDER_FEMALE;

    /** Remember the pet id as sent by the backend. */
    private Integer petID;
    private Integer residenceID;
    private boolean isNew;
    private boolean mSuccessfulUpdate;

    //If the display address and resID indicate that the pet is not assigned to an address, do not allow navigation.
    public MutableLiveData<Boolean> mAllowAddressNavigation;
    public MutableLiveData<Boolean> mErrorState;
    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public MutableLiveData<AnimalType> mAnimalType;
    public MutableLiveData<String> mPetName;
    public MutableLiveData<String> mApproxDOB;
    public MutableLiveData<String> mNotes;
    public MutableLiveData<String> mTreatments;
    public MutableLiveData<String> mDisplayAddress;
    public MutableLiveData<AnimalType> mSpecies;

    public MutableLiveData<String> mDescription;
    public MutableLiveData<String> mGender;
    public MutableLiveData<Integer> mSterilised;

    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // List of species available.
    public MutableLiveData<List<AnimalType>> mSpeciesAvailable;

    private int mSaveResID;
    private Integer mSaveSterilised;
    private String mSaveName, mSaveDOB, mSaveNotes, mSaveTreatements, mSaveAddressDesc, mSaveDescription, mSaveGender;
    private AnimalType mSavedAnimalType;

    ////////Residence search stuff here.
    public MutableLiveData<LinkedList<ResidenceSearchData>> mResidenceSearchResults;
    ////////

    public PetViewModel(Application app) {
        super(app);

        residenceID = -1;
        mSuccessfulUpdate = false;
        mSpeciesAvailable = new MutableLiveData<>();
        mSpeciesAvailable.setValue(((WelfareApplication) getApplication()).getAnimalTypes(false));

        mSpecies = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();
        mErrorState = new MutableLiveData<>();
        mAnimalType = new MutableLiveData<>();
        mPetName = new MutableLiveData<>();
        mApproxDOB = new MutableLiveData<>();
        mNotes = new MutableLiveData<>();
        mTreatments = new MutableLiveData<>();
        mDisplayAddress = new MutableLiveData<>();
        mAllowAddressNavigation = new MutableLiveData<>();

        mGender = new MutableLiveData<>();
        mSterilised = new MutableLiveData<>();
        mDescription = new MutableLiveData<>();

        mNetworkHandler = new MutableLiveData<>();
        mEventHandler = new SingleLiveEvent<>();

        mResidenceSearchResults = new MutableLiveData<>();
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int petID) {
        this.isNew = isNew;
        mEditMode.setValue(isNew);
        if (!isNew) {
            loadData(petID);
        }
    }

    public MutableLiveData<Boolean> getAllowAddressNavigation() {
        return mAllowAddressNavigation;
    }

    public int getResidenceID() {
        return residenceID;
    }

    // Should set to TRUE if editable.
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
    }

    public MutableLiveData<AnimalType> getSpecies() {
        return mSpecies;
    }

    public MutableLiveData<List<AnimalType>> getSpeciesAvailable() {
        return mSpeciesAvailable;
    }

    public MutableLiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public MutableLiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }

    public MutableLiveData<LinkedList<ResidenceSearchData>> getResidenceSearchResult() {
        return mResidenceSearchResults;
    }

    public void setResidence(int id, String description) {
        residenceID = id;
        mDisplayAddress.setValue(description);
        mAllowAddressNavigation.setValue(id >= 0);
    }

    public String getDateEntered() {
        return mApproxDOB.getValue();
    }

    public MutableLiveData<Boolean> getHasDownloadError() {
        return mErrorState;
    }

    public boolean isNew () {
        return isNew;
    }

    public boolean editOccurred() {
        return mSuccessfulUpdate;
    }

    public void setSpecies(AnimalType ani) {
        mSpecies.setValue(ani);
    }

    /**
     * Use this to reload the data if there was an error. Could also be used if there has been a
     * change (in pets for example).
     */
    public void reloadData() {
        loadData(petID);
    }

    /** If this is an edit and not a new, load the existing data from the backend. */
    private void loadData(int petID) {
        if(petID >= 0) {
            this.petID = petID;
            mNetworkHandler.setValue(NetworkStatus.RETRIEVING_DATA);

            Map<String, String> params = new HashMap<>();
            params.put("animal_id", Integer.toString(petID));

            String baseURL = getApplication().getString(R.string.kBaseUrl) + "animals/details";
            String url = NetworkUtils.createURL(baseURL, params);

            RequestQueueManager.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET,
                    url, new JSONObject(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject data = response.getJSONObject("data");
                                if (data != null) {
                                    JSONObject pet = data.getJSONObject("animal_details");
                                    int id = pet.getInt("id");
                                    int animalTypeID = pet.getInt("animal_type_id");
                                    int residenceID = pet.optInt("residence_id", -1);
                                    String name = pet.optString("name");
                                    String dob = pet.optString("approximate_dob");
                                    String notes = pet.optString("notes");
                                    String treatments = pet.optString("treatments");
                                    String displayAddresss = pet.optString("display_address");

                                    String gender = pet.optString("gender");
                                    int sterilised = pet.optInt("sterilised");
                                    String description = pet.optString("description");

                                    PetViewModel.this.petID = id;
                                    PetViewModel.this.residenceID = residenceID;
                                    if (mSpeciesAvailable.getValue() != null) {
                                        for (AnimalType ani : mSpeciesAvailable.getValue()) {
                                            if (ani.getId() == animalTypeID) {
                                                mSpecies.setValue(ani);
                                                break;
                                            }
                                        }
                                    }
                                    mPetName.setValue(name);
                                    mApproxDOB.setValue(dob);
                                    mNotes.setValue(notes);
                                    mTreatments.setValue(treatments);
                                    mDisplayAddress.setValue(displayAddresss);
                                    mAllowAddressNavigation.setValue(residenceID >= 0);
                                    if (!GENDER_MALE.equals(gender) && !GENDER_FEMALE.equals(gender)) {
                                        gender = GENDER_UNKNOWN;
                                    }
                                    mGender.setValue(gender);
                                    mSterilised.setValue(sterilised);
                                    mDescription.setValue(description);
                                    mErrorState.setValue(false);
                                }
                            } catch (JSONException e) {
                                mErrorState.setValue(false);
                                // there is still data available or
                                // there is a data issue. So cannot reload.
                                mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, getApplication().getString(R.string.internal_error_pet_search)));
                            }
                            mNetworkHandler.setValue(NetworkStatus.IDLE);
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, getApplication().getString(R.string.conn_error_pet_search)));
                    } else {
                        String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.unknown_error_pet_search));
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


    /** Either enable edit, or if its enabled already start the saving process. */
    public void toggleSaveEdit() {
        Boolean currentEdit = mEditMode.getValue();
        if (currentEdit != null) {

            if (!currentEdit) {
                mSaveResID = residenceID;
                mSaveSterilised = mSterilised.getValue();
                mSaveGender = mGender.getValue();
                mSaveDescription = mDescription.getValue();
                mSaveName = mPetName.getValue();
                mSaveDOB = mApproxDOB.getValue();
                mSaveNotes = mNotes.getValue();
                mSaveTreatements = mTreatments.getValue();
                mSaveAddressDesc = mDisplayAddress.getValue();
                mSavedAnimalType = mSpecies.getValue();
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
        residenceID = mSaveResID;
        mAllowAddressNavigation.setValue(residenceID >= 0);
        mDisplayAddress.setValue(mSaveAddressDesc);
        mPetName.setValue(mSaveName);
        mApproxDOB.setValue(mSaveDOB);
        mNotes.setValue(mSaveNotes);
        mTreatments.setValue(mSaveTreatements);
        mSpecies.setValue(mSavedAnimalType);

        mSterilised.setValue(mSaveSterilised);
        mGender.setValue(mSaveGender);
        mDescription.setValue(mSaveDescription);
        mSaveDescription = mGender.getValue();
    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        int animalType = mSpecies.getValue() == null ? -1 : mSpecies.getValue().getId();
        String name = mPetName.getValue();
        String dob = mApproxDOB.getValue();
        String notes = mNotes.getValue();
        String treatments = mTreatments.getValue();
        int resID = residenceID;

        // Ensure the user provides some form of address.
        if ((name == null || name.isEmpty()) || (animalType == -1)) {
            mEventHandler.setValue(new Pair<>(Event.DATA_REQUIRED, getApplication().getString(R.string.pet_det_req)));
            return;
        }

        if (isNew) {
            doUpdate(-1, resID, animalType, name, dob, notes, treatments);
        } else {
            boolean hasChanged = ((name != null && !name.equals(mSaveName))
                    || (dob != null && !dob.equals(mSaveDOB))
                    || (notes !=null && !notes.equals(mSaveNotes))
                    || (resID != mSaveResID))
                    || (mSavedAnimalType == null || mSavedAnimalType.getId() != animalType)
                    || (treatments != null && !treatments.equals(mSaveTreatements));

            if (hasChanged) {
                doUpdate(petID, resID, animalType, name, dob, notes, treatments);
            } else {
                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
                        Toast.LENGTH_LONG).show();
                mEditMode.setValue(false);
            }
        }
    }

    /** Send the update to the backend and handle the result. */
    private void doUpdate(int petID, int residenceID, int animalType, String petName, String dob,
                          String notes, String treatments) {

        mNetworkHandler.setValue(NetworkStatus.UPDATING_DATA);
        JSONObject params = new JSONObject();
        try {
            if (!isNew) {
                params.put("animal_id", petID);
            }
            params.put("animal_type_id", animalType);
            if (residenceID != -1) {
                params.put("residence_id", residenceID);
            }
            params.put("name", petName);
            if (dob != null && !dob.trim().isEmpty()) {
                params.put("approximate_dob", dob);
            }
            params.put("notes", notes == null ? "" : notes);
            params.put("treatments", treatments == null ? "" : treatments);

        } catch (JSONException e) {
            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.pet_update_internal_err)));
            mNetworkHandler.setValue(NetworkStatus.IDLE);
            return;
        }

        String URL = getApplication().getString(R.string.kBaseUrl) + "animals/update/";

        RequestQueueManager.getInstance().addToRequestQueue(
                new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            String msg = data.getString("message");
                            PetViewModel.this.petID = data.getInt("animal_id");
                            Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show();
                            mSuccessfulUpdate = true;
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.pet_update_unknown_err)));
                        }
                        mEditMode.setValue(false);
                        isNew = false; //Set to not new value.
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            mEventHandler.setValue(new Pair<>(Event.RETRIEVAL_ERROR, getApplication().getString(R.string.pet_update_conn_err)));
                        } else {
                            String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.pet_update_internal_err));
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

    public void setDate(String date) {
        mApproxDOB.setValue(date);
    }


    //TODO: REFACTOR maybe
    public void doResidenceSearch(String address, String shackID) {
        boolean hasShack = !(shackID == null || shackID.isEmpty());
        boolean hasStreet = !(address == null || address.isEmpty());

        mNetworkHandler.setValue(NetworkStatus.SEARCHING_RESIDENCE);

        Map<String, String> params = new HashMap<>();
        if (hasShack) {
            params.put("shack_id", shackID);
        }

        if (hasStreet) {
            params.put("street_address", address);
        }

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


    public void permanentlyDelete() {
        if(petID >= 0) {
            mNetworkHandler.setValue(NetworkStatus.DELETE_PET);

            JSONObject params = new JSONObject();
            try {
                params.put("animal_id", Integer.toString(petID));
            }  catch (JSONException e) {
                mEventHandler.setValue(new Pair<>(Event.DELETE_ERROR, getApplication().getString(R.string.delete_error_internal_msg)));
                mNetworkHandler.setValue(NetworkStatus.IDLE);
                return;
            }

            String baseURL = getApplication().getString(R.string.kBaseUrl) + "animals/delete/";
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

    public void setGender(String gender) {
        mGender.setValue(gender);
    }
}
