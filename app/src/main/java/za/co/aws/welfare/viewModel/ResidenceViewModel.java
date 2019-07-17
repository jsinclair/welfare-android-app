package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class ResidenceViewModel extends AndroidViewModel {

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

        //todo: saved instance, then
        //TODO: DOwnload data!
    }
}
//
//	"residence_id":5, to be null or missing on ADD
//            "shack_id":"56",
//            "street_address":"1 test street",
//            "latitude":55.5555,
//            "longitude":55.5555,
//            "notes":"test notes"
