package za.co.aws.welfare.utils;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import za.co.aws.welfare.fragment.ProgressDialogFragment;

public class Utils {

    private static final String PROGRESS_TAG = "PROGRESS_TAG";
    public static final String INTENT_UPDATE_REQUIRED = "INTENT_UPDATE_REQUIRED";


    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
    public static final String GENDER_ALL = "ALL";
    public static final String GENDER_UNKNOWN = "UNKNOWN";

    public static final int STERILISED_NO = 0;
    public static final int STERILISED_YES = 1;
    public static final int STERILISED_UNKNOWN = -1;

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

    public static ProgressDialogFragment getProgressDialog(FragmentManager fm) {
        return (ProgressDialogFragment) fm.findFragmentByTag(PROGRESS_TAG);
    }

    /** Convenience method to create or update the progress dialog. */
    public static void updateProgress(FragmentManager fm, ProgressDialogFragment progressDialog, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance(message);
            Utils.showDialogAllowingStateLoss(fm, progressDialog, PROGRESS_TAG);
        } else {
            progressDialog.updateText(message);
        }
    }

    /** Used to display dialogs when async, helps prevent crashes. */
    public static void showDialogAllowingStateLoss(FragmentManager fragmentManager, DialogFragment dialogFragment, String tag) {
        try {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(dialogFragment, tag);
            ft.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        } catch (IllegalStateException e) {
            Log.w("ILLEGAL STATE", "DIALOG MIGHT NOT SHOW");
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
                    returnMessage.append(errorArray.getJSONObject(i).getString("title"));
                    returnMessage.append(":");
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


    /**
     * Convenience method to resize the icons.
     * @param image The icon
     * @param maxWidth The size
     * @param maxHeight The size.
     * @return resized bitmap.
     */
    public static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}
