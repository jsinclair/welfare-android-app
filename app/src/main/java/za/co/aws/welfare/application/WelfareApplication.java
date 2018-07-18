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

    private String mToken;

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

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }
}
