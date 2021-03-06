package za.co.aws.welfare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import za.co.aws.welfare.R;
import za.co.aws.welfare.databinding.ActivityLoginBinding;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.utils.Utils;
import za.co.aws.welfare.viewModel.LoginViewModel;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /** Tag used for the alert dialog. */
    final private static String LOGIN_ALERT_TAG  = "LOGIN_ALERT_TAG";

    /** Tag used for the Progress indicator dialog. */
    final private static String LOGIN_PROGRESS_TAG  = "LOGIN_PROGRESS_TAG";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private CheckBox mRememberMe;

    private LoginViewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mModel = ViewModelProviders.of(this).get(LoginViewModel.class);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);

//        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRememberMe = findViewById(R.id.remember_me);

        mModel.getNetworkHandler().observe(this, new Observer<LoginViewModel.NetworkStatus>() {
            @Override
            public void onChanged(LoginViewModel.NetworkStatus networkStatus) {
                handleNetworkStatus(networkStatus);
            }
        });

        mModel.getEventHandler().observe(this, new Observer<Pair<LoginViewModel.Event, String>>() {
            @Override
            public void onChanged(Pair<LoginViewModel.Event, String> eventData) {
                if (eventData != null) {
                    handleEvent(eventData.first, eventData.second);
                }
            }
        });
    }

    /** Handle once off events. */
    private void handleNetworkStatus(LoginViewModel.NetworkStatus status) {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialogFragment progressDialog = (ProgressDialogFragment) fm.findFragmentByTag(LOGIN_PROGRESS_TAG);
        switch (status) {
            case IDLE:
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                break;
            case LOGGING_IN:
                ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getString(R.string.signing_in));
                Utils.showDialog(fm, progress, LOGIN_PROGRESS_TAG, false);
                break;

        }
    }

    /** Handle once off events. */
    private void handleEvent(LoginViewModel.Event event, String message) {
        switch (event) {
            case LOG_IN_DONE:
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                break;
            case LOG_IN_ERROR:
                FragmentManager fm = getSupportFragmentManager();
                AlertDialogFragment alert = AlertDialogFragment.newInstance(getString(R.string.app_name), message);
                Utils.showDialog(fm, alert, LOGIN_ALERT_TAG, true);
                break;
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Begin the sign in attempt.
            mModel.startLogin();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    //TODO: Whats this?
        /*Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, getNotification("Test Notification"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + 10000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);*/


    @Override
    protected void onPause() {
        super.onPause();
        if (!mRememberMe.isChecked()) {
            mModel.updateSharedPreferences();
        }
    }
}
