package za.co.aws.welfare.viewModel;

import android.app.Application;

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
import java.util.LinkedList;
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
    private Integer residenceID; //TODO: make a way to view this
    private boolean isNew;
    public MutableLiveData<Boolean> mErrorState;
    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public MutableLiveData<AnimalType> mAnimalType;
    public MutableLiveData<String> mPetName;
    public MutableLiveData<String> mApproxDOB;
    public MutableLiveData<String> mNotes;
    public MutableLiveData<String> mTreatments;
    public MutableLiveData<String> mWelfareNumber;

    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    private int mSaveResID;  //TODO: ANUMAL TYPE
    private String mSaveName, mSaveDOB, mSaveNotes, mSaveTreatements, mSaveWelfareNo;

    //TODO: NET WORK EVENT and save

    public PetViewModel(Application app) {
        super(app);

        mEditMode = new MutableLiveData<>();
        mErrorState = new MutableLiveData<>();
        mAnimalType = new MutableLiveData<>();
        mPetName = new MutableLiveData<>();
        mApproxDOB = new MutableLiveData<>();
        mNotes = new MutableLiveData<>();
        mTreatments = new MutableLiveData<>();
        mWelfareNumber = new MutableLiveData<>();

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

    public MutableLiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public MutableLiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }


    public MutableLiveData<Boolean> getHasDownloadError() {
        return mErrorState;
    }

    public boolean isNew () {
        return isNew;
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
                                    int residenceID = pet.getInt("residence_id");
                                    String name = pet.optString("name");
                                    String dob = pet.optString("approximate_dob");
                                    String notes = pet.optString("notes");
                                    String welfareID = pet.optString("welfare_number");
                                    String treatments = pet.optString("treatments");

                                    PetViewModel.this.petID = id;
                                    PetViewModel.this.residenceID = residenceID;
                                    //TODO: ANIMAL TYPE ID
                                    mPetName.setValue(name);
                                    mApproxDOB.setValue(dob);
                                    mNotes.setValue(notes);
                                    mWelfareNumber.setValue(welfareID);
                                    mTreatments.setValue(treatments);
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
    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        //TODO
    }

}
