package za.co.aws.welfare.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import za.co.aws.welfare.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


/**
 * Created by zynique on 2017/09/11.
 *
 * Dialog fragment for YES, NO options. Activity using this needs to implement the interface.
 */
public class YesNoDialogFragment extends DialogFragment {

    /** Should be implemented by all users of this dialog. */
    public interface YesNoDialogUser {
        /**
         * Will be called when the YES option was selected.
         * @param tag The Dialog Tag, for when there is more than one dialog in the activity.
         **/
        void onDialogYesSelected(String tag);

        /**
         * Will be called when the NO option was selected.
         * @param tag The Dialog Tag, for when there is more than one dialog in the activity.
         **/
        void onDialogNoSelected(String tag);
    }

    /** The tag, used to reply to the activity so that it knows which dialog is responding. */
    private String mTag;


    /** Create a new instance of the Dialog Fragment which will display an alert dialog. The alert
     * dialog is always cancelable.
     * @param title The title of the alert dialog.
     * @param message The message.
     * @param yesText Text to display on the positive button.
     * @param noText Text to display on the negative button.
     * @return An alert dialog.
     */
    public static YesNoDialogFragment newInstance(String title, String message, String yesText, String noText, String tag) {
        YesNoDialogFragment frag = new YesNoDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("msg", message);
        args.putString("yes_button", yesText);
        args.putString("no_button", noText);
        args.putString("tag", tag);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mTag = getArguments().getString("tag");
        String message = getArguments().getString("msg");
        String title = getArguments().getString("title");

        String yesText = getArguments().getString("yes_button");
        String noText = getArguments().getString("no_button");

        View v = inflater.inflate(R.layout.fragment_yes_no_dialog, container, false);
        setCancelable(false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView titleArea = v.findViewById(R.id.title);
        titleArea.setText(title);

        TextView messageArea = v.findViewById(R.id.message);
        messageArea.setText(message);

        Button yesButton = v.findViewById(R.id.yes_button);
        yesButton.setText(yesText);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentFragment() != null){
                    if (getParentFragment() instanceof  YesNoDialogUser) {
                        ((YesNoDialogUser) getParentFragment()).onDialogYesSelected(mTag);
                    } else {
                        Log.w("YES-NO DIALOG", "User fragment does not implement interface");
                    }
                } else if (getActivity() instanceof YesNoDialogUser) {
                    ((YesNoDialogUser) getActivity()).onDialogYesSelected(mTag);
                } else {
                    Log.w("YES-NO DIALOG", "User does not implement interface");
                }
                dismiss();
            }
        });

        Button noButton = v.findViewById(R.id.no_button);
        noButton.setText(noText);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentFragment() != null){
                    if (getParentFragment() instanceof  YesNoDialogUser) {
                        ((YesNoDialogUser) getParentFragment()).onDialogNoSelected(mTag);
                    } else {
                        Log.w("YES-NO DIALOG", "User fragment does not implement interface");
                    }
                } else if (getActivity() instanceof YesNoDialogUser) {
                    ((YesNoDialogUser) getActivity()).onDialogNoSelected(mTag);
                } else {
                    Log.w("YES-NO DIALOG", "User does not implement interface");
                }
                dismiss();
            }
        });
        return v;
    }


}
