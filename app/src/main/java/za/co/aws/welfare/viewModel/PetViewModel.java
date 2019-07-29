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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** used for the Pet Activity, which allows the user to view or modify a pet. */
public class PetViewModel extends AndroidViewModel {

    //TODO: REsidence navigation and change.

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        // Busy retrieving data for this pet.
        RETRIEVING_DATA,

        // Busy updating pet.
        UPDATING_DATA,
    }

    public enum Event {
        // When we fail to retrieve the data from the backend.
        RETRIEVAL_ERROR,

        // If an error occurred while trying to update the pet.
        UPDATE_ERROR,

        // If the user has not provided enough data to update or create a pet.
        DATA_REQUIRED,
    }

    /** Remember the pet id as sent by the backend. */
    private Integer petID;
    private Integer residenceID;
    private boolean isNew;
    public MutableLiveData<Boolean> mErrorState;
    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public MutableLiveData<AnimalType> mAnimalType;
    public MutableLiveData<String> mPetName;
    public MutableLiveData<String> mApproxDOB;
    public MutableLiveData<String> mNotes;
    public MutableLiveData<String> mTreatments;
    public MutableLiveData<String> mWelfareNumber;
    public MutableLiveData<String> mDisplayAddress;
    public MutableLiveData<AnimalType> mSpecies;

    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // List of species available.
    public MutableLiveData<List<AnimalType>> mSpeciesAvailable;

    private int mSaveResID;  //TODO:
    private String mSaveName, mSaveDOB, mSaveNotes, mSaveTreatements, mSaveWelfareNo;
    private AnimalType mSavedAnimalType;

    public PetViewModel(Application app) {
        super(app);

        residenceID = -1;
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
        mWelfareNumber = new MutableLiveData<>();
        mDisplayAddress = new MutableLiveData<>();

        mNetworkHandler = new MutableLiveData<>();
        mEventHandler = new SingleLiveEvent<>();
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int petID) {
        this.isNew = isNew;
        mEditMode.setValue(isNew);
        if (!isNew) {
            loadData(petID);
        }
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

    public String getDateEntered() {
        return mApproxDOB.getValue();
    }


    public MutableLiveData<Boolean> getHasDownloadError() {
        return mErrorState;
    }

    public boolean isNew () {
        return isNew;
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
                                    String welfareID = pet.optString("welfare_number");
                                    String treatments = pet.optString("treatments");
                                    String displayAddresss = pet.optString("display_address");

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
                                    mWelfareNumber.setValue(welfareID);
                                    mTreatments.setValue(treatments);
                                    mDisplayAddress.setValue(displayAddresss);
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
                mSaveName = mPetName.getValue();
                mSaveDOB = mApproxDOB.getValue();
                mSaveNotes = mNotes.getValue();
                mSaveTreatements = mTreatments.getValue();
                mSaveWelfareNo = mWelfareNumber.getValue();
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
        mPetName.setValue(mSaveName);
        mApproxDOB.setValue(mSaveDOB);
        mNotes.setValue(mSaveNotes);
        mTreatments.setValue(mSaveTreatements);
        mWelfareNumber.setValue(mSaveWelfareNo);
        mSpecies.setValue(mSavedAnimalType);
    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        int animalType = mSpecies.getValue() == null ? -1 : mSpecies.getValue().getId();
        String name = mPetName.getValue();
        String dob = mApproxDOB.getValue();
        String notes = mNotes.getValue();
        String welfareID = mWelfareNumber.getValue();
        String treatments = mTreatments.getValue();
        int resID = residenceID;

        // Ensure the user provides some form of address.
        if ((name == null || name.isEmpty()) || (welfareID == null || welfareID.isEmpty()) || (animalType == -1)) {
            mEventHandler.setValue(new Pair<>(Event.DATA_REQUIRED, getApplication().getString(R.string.pet_det_req)));
            return;
        }

        if (isNew) {
            doUpdate(-1, resID, animalType, name, dob, welfareID, notes, treatments);
        } else {
            boolean hasChanged = ((name != null && !name.equals(mSaveName))
                    || (dob != null && !dob.equals(mSaveDOB))
                    || (notes !=null && !notes.equals(mSaveNotes))
                    || (welfareID != null && !welfareID.equals(mSaveWelfareNo))
                    || (resID != mSaveResID))
                    || (mSavedAnimalType == null || mSavedAnimalType.getId() != animalType)
                    || (treatments != null && !treatments.equals(mSaveTreatements));

            if (hasChanged) {
                doUpdate(petID, resID, animalType, name, dob, welfareID, notes, treatments);
            } else {
                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
                        Toast.LENGTH_LONG).show();
                mEditMode.setValue(false);
            }
        }
    }

    /** Send the update to the backend and handle the result. */
    private void doUpdate(int petID, int residenceID, int animalType, String petName, String dob,
                          String welfareNo, String notes, String treatments) {

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
            params.put("approximate_dob", dob == null ? "" : dob);
            params.put("notes", notes == null ? "" : notes);
            params.put("welfare_number", welfareNo == null ? "" : welfareNo);
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

                            Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show();

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


}
