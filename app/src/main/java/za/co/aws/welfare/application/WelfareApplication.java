package za.co.aws.welfare.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WelfareApplication extends Application {

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
}
