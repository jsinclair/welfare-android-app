package za.co.aws.welfare.customComponents;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Modified from android docs. A Dialog fragment displaying a date picker.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    /** Interface the MUST be implemented by the calling activity. */
    public interface DatePickerUser {
        /** The method that will be called when the date has been selected.*/
        void onDateChosen(String dateChosen);
    }

    /**
     * Get a new instance.
     * @param startDate The date that needs to be selected.
     * @param separator The separator used in the date .
     * @return A date picker
     */
    public static DatePickerFragment newInstance(String startDate, String separator) {
        DatePickerFragment f = new DatePickerFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("startDate", startDate);
        args.putString("separator", separator);
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String startDate = getArguments().getString("startDate");
        String separator = getArguments().getString("separator");
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        if (startDate != null && !startDate.isEmpty()) {
            if (separator == null || separator.isEmpty()) {
                separator = "-";
            }
            String [] parts = startDate.split(separator);
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]) - 1;
            day = Integer.parseInt(parts[2]);
        }

        DatePickerDialog dialog = new DatePickerDialog(getActivity(),
                AlertDialog.THEME_HOLO_DARK,this, year, month, day);

        Field mDatePickerField;
        try {
            mDatePickerField = dialog.getClass().getDeclaredField("mDatePicker");
            mDatePickerField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        return dialog;
    }

    /**
     * Called once the date has been selected
     *
     * @param view The date picker view.
     * @param year The year selected.
     * @param month The month selected.
     * @param day The day selected.
     */
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (getActivity() instanceof  DatePickerUser) {

            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(c.getTime());
            ((DatePickerUser) getActivity()).onDateChosen(formattedDate);
        } else {
            Log.w("ERROR", "caller does not implement interface. Investigate");
        }
    }
}