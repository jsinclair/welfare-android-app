package za.co.aws.welfare.viewModel;

import android.app.Application;
import android.text.format.DateUtils;
import android.widget.Toast;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.dataObjects.PetMinDetail;
import za.co.aws.welfare.fragment.SearchPetsFragment;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** Viewmodel for the add reminders activity. Handle backend calls and data changes. */
public class RemindersViewModel extends AndroidViewModel implements SearchPetsFragment.PetSearcher {

    // Place holder for the date.
    public static final String UNKNOWN_DATE = "Unknown";

    // Once off events.
    public enum Event {
        // Happens when the user has not provided a date.
        DATE_REQUIRED,

        // Happens when the user tries to edit a reminder that is today.
        EDIT_ATTEMPT_TODAY,

        // Error on update
        UPDATE_ERROR,
    }

    // Once off events.
    public enum NetworkAction {
        IDLE,
        UPDATING,
    }

    // Used to indicate an event has triggered.
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    // Stores date selected by the user.
    public MutableLiveData<String> mDateSelected;

    // Stores notes for the event.
    public MutableLiveData<String> mNotes;
    public MutableLiveData<NetworkAction> mNetworkHandler;

    // Stores a list of animals associated with the reminder.
    public MutableLiveData<List<PetMinDetail>> mAnimalList;

    private Integer reminderID;
    private boolean isNew;
    private boolean fromSearch;
    private boolean successfulEditOccurred;
    private PetMinDetail mRemoveRequest;

    // These store the values to revert to if the user 'cancels' an edit.
    private String mDateSave, mNotesSave;
    private List<PetMinDetail> mSavedAnimalList;

    public MutableLiveData<Boolean> mEditMode; //Use this to enable and disable input.

    public RemindersViewModel(Application application) {
        super(application);
        mNotes = new MutableLiveData<>();
        mAnimalList = new MutableLiveData<>();
        mDateSelected = new MutableLiveData<>();
        mEditMode = new MutableLiveData<>();
        mNetworkHandler = new MutableLiveData<>();
        successfulEditOccurred = false;
        mEventHandler = new SingleLiveEvent<>();
    }

    public MutableLiveData<List<PetMinDetail>> getAnimalList() {
        return mAnimalList;
    }

    public MutableLiveData<String> getSelectedDate() {
        return mDateSelected;
    }

    public MutableLiveData<NetworkAction> getmNetworkHandler() {
        return mNetworkHandler;
    }

    // Should set to TRUE if editable.
    public MutableLiveData<Boolean> getEditMode() {
        return mEditMode;
    }

    public MutableLiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }


    /** call this to add a pet to the residence. Will only be persisted on save. */
    public void onPetSelected(PetMinDetail petToAdd) {
        if (petToAdd != null) {
            List<PetMinDetail> list = mAnimalList.getValue();
            if (list == null) {
                list = new LinkedList<>();
            }
            boolean hasAni = false;
            for (PetMinDetail ani: list) {
                if (ani.getID() == petToAdd.getID()) {
                    hasAni = true;
                    break;
                }
            }
            if (!hasAni) {
                list.add(petToAdd);
            }
            mAnimalList.setValue(list);
        }
    }

    // Call this to modify the viewModel and activity for a NEW entry or an EDIT entry.
    public void setup(boolean isNew, int reminderID, boolean fromSearch) {
        this.isNew = isNew;
        this.fromSearch = fromSearch;
        mEditMode.setValue(isNew);
        if (!isNew) {
            //TODO: LOAD DATA FROM THE BACKEND loadData(reminderID);
        } else {
            // Start the date selector.
            mDateSelected.setValue(UNKNOWN_DATE);
        }
    }

    public boolean isNew () {
        return isNew;
    }

    /** Either enable edit, or if its enabled already start the saving process. */
    public void toggleSaveEdit() {
        Boolean currentEdit = mEditMode.getValue();
        if (currentEdit != null) {
            if (!currentEdit) {
                if (!isNew && isTodayReminder()) {
                    mEventHandler.setValue(new Pair<>(Event.EDIT_ATTEMPT_TODAY, getApplication().getString(R.string.cannot_edit_today)));
                    return;
                }
                mNotesSave = mNotes.getValue();
                mDateSave = mDateSelected.getValue();
                mSavedAnimalList = new LinkedList<>();
                if (mAnimalList != null && mAnimalList.getValue() != null) {
                    mSavedAnimalList.addAll(mAnimalList.getValue());
                }
                mEditMode.setValue(true);
            } else {
//                 Do save actions to backend.
//                 set editable back to false once done
                saveData();
            }
        }
    }

    /** Return true if the reminder's date is that of todays. */
    private boolean isTodayReminder() {
        String date = mDateSelected.getValue();
        if (!UNKNOWN_DATE.equals(date)) {
            try {
                DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                Date dateOff = formatter.parse(date);
                return DateUtils.isToday(dateOff.getTime());
            } catch (ParseException e) {}
        }
        return false;
    }

    public void setDate(String date) {
        mDateSelected.setValue(date);
    }

    /** Attempt to send the update / to the backend. */
    private void saveData() {
        String date = mDateSelected.getValue();
        if (date == null || date.isEmpty() || UNKNOWN_DATE.equals(mDateSelected.getValue())) {
            mEventHandler.setValue(new Pair<>(Event.DATE_REQUIRED, getApplication().getString(R.string.date_required)));
            return;
        }

        String note = mNotes.getValue();
        if (isNew) {
            doUpdate(-1, date, note, mAnimalList.getValue());
        } else {
            boolean hasChanged = ((note != null && !note.equals(mNotesSave))
                    || (date != null && !date.equals(mDateSave)));

            List<PetMinDetail> pets = mAnimalList.getValue();
            if (!((mSavedAnimalList == null || mSavedAnimalList.isEmpty()) && (pets == null || pets.isEmpty()))) {
                if ((pets == null && !mSavedAnimalList.isEmpty()) || (pets.size() != mSavedAnimalList.size())) { //todo; check logic here
                    hasChanged = true;
                } else {
                    for (PetMinDetail pet : mSavedAnimalList) {
                        if (!pets.contains(pet)) {
                            hasChanged = true;
                            break;
                        }
                    }
                }
            }

            if (hasChanged) {
                doUpdate(reminderID, date, note, pets);
            } else {
                Toast.makeText(getApplication(), getApplication().getString(R.string.no_change),
                        Toast.LENGTH_LONG).show();
                mEditMode.setValue(false);
            }
        }
    }

    /** Send the update to the backend and handle the result. */
    private void doUpdate(int id, String date, String notes, List<PetMinDetail> pets) {
        mNetworkHandler.setValue(NetworkAction.UPDATING);
        JSONObject params = new JSONObject();
        try {
            if (!isNew) {
                params.put("reminder_id", id);
            }
            params.put("note", notes == null ? "" : notes);
            params.put("date", date);

            if (pets != null) {
                JSONArray animalIDs = new JSONArray();
                for (PetMinDetail det: pets) {
                    animalIDs.put(det.getID());
                }
                params.put("animals", animalIDs);
            }

        } catch (JSONException e) {
            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.reminder_update_internal_err)));
            mNetworkHandler.setValue(NetworkAction.IDLE);
            return;
        }

        String URL = getApplication().getString(R.string.kBaseUrl) + "reminders/update/";

        RequestQueueManager.getInstance().addToRequestQueue(
                new JsonObjectRequest(Request.Method.POST, URL, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            String msg = data.getString("message");
                            reminderID = data.getInt("reminder_id");
                            Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show();

                            // If an edit managed to occur at all, we might need to reload the calling class.
                            successfulEditOccurred = true;
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.reminder_update_unknown_err)));
                            return;
                        }
                        mEditMode.setValue(false);
                        isNew = false;
                        mNetworkHandler.setValue(NetworkAction.IDLE);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, getApplication().getString(R.string.reminder_update_conn_err)));
                        } else {
                            String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.reminder_update_unknown_err));
                            mEventHandler.setValue(new Pair<>(Event.UPDATE_ERROR, errorMSG));
                        }
                        mNetworkHandler.setValue(NetworkAction.IDLE);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + ((WelfareApplication) getApplication()).getToken());
                        return headers;
                    }
                }, getApplication());
    }

    /* Cancel the current edit and reset the values. */
    public void cancelEdit() {
        mEditMode.setValue(false);
        mDateSelected.setValue(mDateSave);
        mNotes.setValue(mNotesSave);
        mAnimalList.setValue(mSavedAnimalList);
    }

    // Set the last pet that was selected to remove from the reminder.
    public void setRemoveRequest(PetMinDetail deleteRequest) {
        this.mRemoveRequest = deleteRequest;
    }

    // remove a pet from the pet list.
    public void removePet() {
        if (mRemoveRequest != null) {
            List<PetMinDetail> list = mAnimalList.getValue();
            if (list != null) {
                list.remove(mRemoveRequest);
            }
            mAnimalList.setValue(list);
        }
    }
}
