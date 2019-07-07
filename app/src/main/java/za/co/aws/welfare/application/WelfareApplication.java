package za.co.aws.welfare.application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.firebase.FirebaseApp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import za.co.aws.welfare.R;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.model.UserPermissions;

public class WelfareApplication extends MultiDexApplication {

    private final String PREF_FILE = "za.co.aws.welfare.PREFERENCE_FILE_KEY";

    /** The key to use for the username. */
    private final String PREF_FILE_USERNAME_EXT = ".username";

    /** The key to use for the password. */
    private final String PREF_FILE_PASSWORD_EXT = ".password";

    /** The key to use for remember me. */
    private final String PREF_FILE_REMEMBER_ME_EXT = ".remember";

    /** The key to use for the session token. */
    private final String PREF_FILE_TOKEN_EXT = ".token";

    /** The key to use for the user's full name. */
    private final String PREF_FILE_FULL_NAME_EXT = ".fullName";

    /** The key to use for the user's organisation name. */
    private final String PREF_FILE_ORGANISATION_NAME_EXT = ".organisationName";

    /** The key to use for the user's permissions. */
    private final String PREF_FILE_PERMISSIONS_EXT = ".permissions";

    // Session storage variables TODO: STORE THIS IN A DATABASE RATHER
    private List<AnimalType> mAnimalTypes;

    /**
     * Convenience method for getting the shared preference keys.
     *
     * @param extension PLEASE choose one of the shared preference constants.
     * @return A shared preference key.
     */
    private String getSharedPreferenceKey(String extension) {
        return PREF_FILE + extension;
    }

    /**
     * Set the login data.
     *
     * @param username The current username.
     * @param password The current password.
     * @param rememberMe Whether the username and password should be stored for the next session.
     */
    public void setLoginData (String username, String password, boolean rememberMe) {
        SharedPreferences sharedPrefs = getSharedPreferences(
                PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(getSharedPreferenceKey(PREF_FILE_USERNAME_EXT), username);
        editor.putString(getSharedPreferenceKey(PREF_FILE_PASSWORD_EXT), password);
        editor.putBoolean(getSharedPreferenceKey(PREF_FILE_REMEMBER_ME_EXT), rememberMe);

        editor.apply();
    }

    /**
     * Set the login data.
     *
     * @param permissions List containing keys for the user's permissions.
     */
    public void setPermissions(List<String> permissions) {
        SharedPreferences sharedPrefs = getSharedPreferences(
                PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.remove(PREF_FILE_PERMISSIONS_EXT);

        if (permissions != null) {
            editor.putStringSet(getSharedPreferenceKey(PREF_FILE_PERMISSIONS_EXT), new HashSet<>(permissions));
        }

        editor.apply();
    }

    /**
     * Returns whether or not the user has the requested permission.
     *
     * @param permission the UserPermission to check for.
     * @return boolean for whether the current user has the permission or not.
     */
    public boolean hasPermission(UserPermissions permission) {
        final SharedPreferences prefs = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        boolean hasPermission = false;
        if (prefs.contains(getSharedPreferenceKey(PREF_FILE_PERMISSIONS_EXT))) {
            final Set<String> permissions = prefs.getStringSet(getSharedPreferenceKey(PREF_FILE_PERMISSIONS_EXT),
                    new HashSet<String>());

            hasPermission = permissions.contains(permission.name());
        }
        return hasPermission;
    }

    /**
     * The user preference to remember username and password.
     * @return TRUE if remember me is active. FALSE otherwise.
     */
    public boolean getRememberMe() {
        return getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getBoolean(getSharedPreferenceKey(PREF_FILE_REMEMBER_ME_EXT), false);
    }

    /**
     * @return The last username used.
     */
    public String getUsername() {
        return getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getString(getSharedPreferenceKey(PREF_FILE_USERNAME_EXT), "");
    }

    /**
     * @return the last password used.
     */
    public String getPassword() {
        return getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getString(getSharedPreferenceKey(PREF_FILE_PASSWORD_EXT), "");
    }

    public void setLoginDetails(String token, String fullName, String organisationName) {
        SharedPreferences sharedPrefs = getSharedPreferences(
                PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(getSharedPreferenceKey(PREF_FILE_TOKEN_EXT), token);
        editor.putString(getSharedPreferenceKey(PREF_FILE_FULL_NAME_EXT), fullName);
        editor.putString(getSharedPreferenceKey(PREF_FILE_ORGANISATION_NAME_EXT), organisationName);
        editor.apply();
    }

    /**
     * @return the current user session.
     */
    public String getToken() {
        return getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getString(getSharedPreferenceKey(PREF_FILE_TOKEN_EXT), "");
    }

    /**
     * @return the current user's full name.
     */
    public String getFullName() {
        return getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getString(getSharedPreferenceKey(PREF_FILE_FULL_NAME_EXT), "");
    }

    /**
     * @return the current user's organisation name.
     */
    public String getOrganisationName() {
        return getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
                .getString(getSharedPreferenceKey(PREF_FILE_ORGANISATION_NAME_EXT), "");
    }

    // Accessors and mutators for utility lists
    public List<AnimalType> getAnimalTypes() {
        return mAnimalTypes;
    }

    public void setAnimalTypes(List<AnimalType> mAnimalTypes) {
        this.mAnimalTypes = mAnimalTypes;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.channel_id), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("WelfareApplication", "onCreate");

        // Initialise FireBase
        FirebaseApp.initializeApp(this);

        // Initialise the notification channel when the app starts up.
        createNotificationChannel();
    }
}
