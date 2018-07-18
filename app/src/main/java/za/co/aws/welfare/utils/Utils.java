package za.co.aws.welfare.utils;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    /** Show the given dialog, if it doesn't exist already. */
    public static void showDialog(FragmentManager fm, DialogFragment dialog, String tag, boolean allowStateLoss) {
        if (allowStateLoss) {
            if (fm.findFragmentByTag(tag) == null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(dialog, tag);
                ft.commitAllowingStateLoss();
            } else {
                Log.i("SHOW DIALOG", "DIALOG ALREADY EXISTS");
            }
        } else {
            if (fm.findFragmentByTag(tag) == null) {
                dialog.show(fm, tag);
            } else {
                Log.i("SHOW DIALOG", "DIALOG ALREADY EXISTS");
            }
        }
    }

    /**
     * Using the volley error, retrieve the error message.
     * @param error the error that was returned.
     * @param defaultMessage The default message to use if an exception occurred while processing the data.
     * @return The resulting error message to display to the user.
     */
    public static String generateErrorMessage(VolleyError error, String defaultMessage) {
        NetworkResponse response = error.networkResponse;
        if(response != null && response.data != null) {
            String json = new String(response.data);
            try {
                JSONObject errorObject = new JSONObject(json);
                JSONArray errorArray = errorObject.getJSONArray("errors");
                StringBuilder returnMessage = new StringBuilder("");
                int end = errorArray.length();
                for (int i = 0; i < end; i++) {
                    returnMessage.append(errorArray.getJSONObject(i).getString("detail"));
                    if(i < end -1) {
                        returnMessage.append("\n");
                    }
                }
                return returnMessage.toString();
            } catch (JSONException e) {
                //Swallow exception and send default response.
            }
        }
        return defaultMessage;
    }
}
