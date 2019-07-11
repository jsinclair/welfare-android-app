package za.co.aws.welfare.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import za.co.aws.welfare.R;

/**
 * Created by zynique on 2018/06/11.
 * Fragment to display an alert to the user. Should stay on orientation change.
 */
public class AlertDialogFragment extends DialogFragment {

    /** Create a new instance of the Dialog Fragment which will display an alert dialog. The alert
     * dialog is always cancelable.
     * @param title The title of the alert dialog.
     * @param message The message.
     * @return An alert dialog.
     */
    public static AlertDialogFragment newInstance(String title, String message) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", message);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        String title = getArguments().getString("title");
        String message = getArguments().getString("msg");

//        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
//        setCancelable(false);
//        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(R.layout.fragment_alert_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView titleArea = v.findViewById(R.id.title);
        titleArea.setText(title);

        TextView messageArea = v.findViewById(R.id.message);
        messageArea.setText(message);

        Button okButton = v.findViewById(R.id.button);
        okButton.setText(R.string.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;

    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}