package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.pm.PackageManager;
import android.util.Pair;

import za.co.aws.welfare.application.WelfareApplication;

public class LoginViewModel extends AndroidViewModel {
    /** keeps track of the remember me option. */
    public MutableLiveData<Boolean> mRememberMe;

    /** Remember the user name. */
    public MutableLiveData<String> mUsername;

    /** Remember the user password. */
    public MutableLiveData<String> mPassword;

    /** Constructor. */
    public LoginViewModel(Application application) {
        super(application);

        mRememberMe = new MutableLiveData<>();
        mUsername = new MutableLiveData<>();
        mPassword = new MutableLiveData<>();

        boolean remember = ((WelfareApplication) getApplication()).getRememberMe();
        mRememberMe.setValue(remember);
        if (remember) {
            mUsername.setValue(((WelfareApplication) getApplication()).getUsername());
            mPassword.setValue(((WelfareApplication) getApplication()).getPassword());
//            mEventHandler.setValue(new Pair<Event, String>(Event.DISABLE_EYE, ""));
        } else {
            mUsername.setValue("");
            mPassword.setValue("");
        }
    }
}
