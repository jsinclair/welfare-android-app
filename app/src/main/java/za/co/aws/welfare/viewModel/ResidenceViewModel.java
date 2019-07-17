package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class ResidenceViewModel extends AndroidViewModel {


    /** Remember the user name. */
    public MutableLiveData<String> mAddress;

    public ResidenceViewModel(Application app) {
        super(app);
        mAddress = new MutableLiveData<>();
        mAddress.setValue("TEST");
    }
}
