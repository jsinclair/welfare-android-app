package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class HomeViewModel extends AndroidViewModel {

    /** Remember the user name. */
    public MutableLiveData<String> mUsername;

    public HomeViewModel(Application application) {
        super(application);
    }
}
