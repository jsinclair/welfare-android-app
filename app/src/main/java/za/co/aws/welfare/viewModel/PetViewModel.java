package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import za.co.aws.welfare.model.AnimalType;

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

        // If an error occurred while trying to update the residence.
        UPDATE_ERROR,

        // If the user has not provided enough data to update or create a residence.
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

    //TODO: NET WORK EVENT and save

    public PetViewModel(Application app) {
        super(app);

        mEditMode = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();
        mAnimalType = new MutableLiveData<>();
        mPetName = new MutableLiveData<>();
        mApproxDOB = new MutableLiveData<>();
        mNotes = new MutableLiveData<>();
        mTreatments = new MutableLiveData<>();
        mWelfareNumber = new MutableLiveData<>();
    }


}
