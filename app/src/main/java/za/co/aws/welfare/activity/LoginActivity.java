package za.co.aws.welfare.activity;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.List;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.fragment.AlertDialogFragment;
import za.co.aws.welfare.fragment.LoginTask;
import za.co.aws.welfare.fragment.ProgressDialogFragment;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.FirebaseTokenUpdater;
import za.co.aws.welfare.utils.Utils;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoginTask.LoginTaskCallbacks {

    /** Tag used for the alert dialog. */
    final private static String LOGIN_ALERT_TAG  = "LOGIN_ALERT_TAG";

    /** Tag used for the Login Task, used to do the API call to log the user into the app.*/
    final private static String LOGIN_TASK_TAG  = "LOGIN_TASK_TAG";

    /** Tag used for the Progress indicator dialog. */
    final private static String LOGIN_PROGRESS_TAG  = "LOGIN_PROGRESS_TAG";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private CheckBox mRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
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

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRememberMe = (CheckBox) findViewById(R.id.remember_me);

        boolean remember = ((WelfareApplication) getApplicationContext()).getRememberMe();
        if (remember) {
            mEmailView.setText(((WelfareApplication) getApplicationContext()).getUsername());
            mPasswordView.setText(((WelfareApplication) getApplicationContext()).getPassword());
        }
        mRememberMe.setChecked(remember);

        checkGooglePlayServicesAvailable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkGooglePlayServicesAvailable();
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
            startLogin();
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

    /** Start the fragment to log the user in. */
    private void startLogin() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment loginTask = fm.findFragmentByTag(LOGIN_TASK_TAG);
        if (loginTask == null) {
            //Show progress dialog.
            ProgressDialogFragment progress = ProgressDialogFragment.newInstance(getString(R.string.signing_in));
            Utils.showDialog(fm, progress, LOGIN_PROGRESS_TAG, false);

            //Start task.
            String uuid = Settings.Secure.getString(LoginActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
            loginTask = LoginTask.newInstance(mEmailView.getText().toString().trim(), mPasswordView.getText().toString().trim(), uuid);
            fm.beginTransaction().add(loginTask, LOGIN_TASK_TAG).commit();
        }
    }

    @Override
    public void onLoginComplete(boolean error, String errorMessage, final String sessionToken, String fullName,
                                String organisationName, List<AnimalType> animalTypes, List<String> permissions) {

        if (mRememberMe.isChecked()) {
            updateSharedPreferences();
        }

        FragmentManager fm = getSupportFragmentManager();

        if (error) {
            AlertDialogFragment alert = AlertDialogFragment.newInstance(getString(R.string.app_name), errorMessage);
            Utils.showDialog(fm, alert, LOGIN_ALERT_TAG, true);
        } else {
            // Set login details
            ((WelfareApplication)getApplication()).setLoginDetails(sessionToken, fullName, organisationName);
            ((WelfareApplication)getApplication()).setPermissions(permissions);

            // Set utility lists
            ((WelfareApplication)getApplication()).setAnimalTypes(animalTypes);

            // Update the firebase token for the user
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w("LoginActivity", "getInstanceId failed", task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            String token = task.getResult().getToken();

                            // Initialise and run a FirebaseTokenUpdater
                            new FirebaseTokenUpdater(LoginActivity.this.getApplicationContext(), token, sessionToken).start();

                            // Log and toast
                            Log.d("LoginActivity", token);
                            Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
                        }
                    });

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }

        Fragment loginTask = fm.findFragmentByTag(LOGIN_TASK_TAG);
        if (loginTask != null) {
            fm.beginTransaction().remove(loginTask).commit();
        }

        DialogFragment progress = (DialogFragment) fm.findFragmentByTag(LOGIN_PROGRESS_TAG);
        if (progress != null) {
            progress.dismiss();
        }

        /*Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, getNotification("Test Notification"));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + 10000;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mRememberMe.isChecked()) {
            updateSharedPreferences();
        }
    }

    /**
     * Updates the shared preferences to remember username, password and remember state.
     *
     * Please note: this method does not check for login success! Only saves based on checkbox.
     */
    private void updateSharedPreferences() {
        ((WelfareApplication) getApplicationContext()).setLoginData(
                mEmailView.getText().toString(),
                mPasswordView.getText().toString(),
                mRememberMe.isChecked());
    }

    private Notification getNotification(String content) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.channel_id))
                .setSmallIcon(R.drawable.dog)
                .setContentTitle("Scheduled Notification")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return mBuilder.build();
    }

    private void checkGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.makeGooglePlayServicesAvailable(this);
        }
    }
}
