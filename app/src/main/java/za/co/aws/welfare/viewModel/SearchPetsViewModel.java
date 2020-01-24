package za.co.aws.welfare.viewModel;

import android.app.Application;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import za.co.aws.welfare.R;
import za.co.aws.welfare.application.WelfareApplication;
import za.co.aws.welfare.dataObjects.PetSearchData;
import za.co.aws.welfare.model.AnimalType;
import za.co.aws.welfare.utils.NetworkUtils;
import za.co.aws.welfare.utils.RequestQueueManager;
import za.co.aws.welfare.utils.SingleLiveEvent;
import za.co.aws.welfare.utils.Utils;

/** Viewmodel for the pet search view. Takes care of the searching of pets from the backend.
 * TODO: REplace Animals fragment with a copy of this*/
public class SearchPetsViewModel extends AndroidViewModel {

    /** The network statuses. */
    public enum NetworkStatus {
        // No network action at the moment.
        IDLE,

        SEARCHING_PET,
    }

    public enum Event {
        SEARCH_PET_ERROR,
    }

    public static final String GENDER_FEMALE = Utils.GENDER_FEMALE;
    public static final String GENDER_MALE = Utils.GENDER_MALE;
    public static final String GENDER_ALL = Utils.GENDER_ALL;

    public static final String STERILISED_YES = "1";
    public static final String STERILISED_NO = "0";
    public static final String STERILISED_ALL = "";

    private MutableLiveData<NetworkStatus> mNetworkHandler;
    private SingleLiveEvent<Pair<Event, String>> mEventHandler;

    public MutableLiveData<String> mPetNameSearch;
    public MutableLiveData<String> mPetGenderSearch;
    public MutableLiveData<String> mPetSterilisedSearch;

    // List of species available.
    public MutableLiveData<List<AnimalType>> mSpeciesAvailable;

    // The selected species
    public MutableLiveData<AnimalType> mSelectedSpecies;

    // The result of the search.
    public MutableLiveData<LinkedList<PetSearchData>> mPetSearchResult;

    public SearchPetsViewModel(Application app) {
        super(app);
        mNetworkHandler = new MutableLiveData<>();
        mPetSearchResult = new MutableLiveData<>();
        mEventHandler = new SingleLiveEvent<>();

        mPetNameSearch = new MutableLiveData<>();
        mPetGenderSearch = new MutableLiveData<>();
        mPetSterilisedSearch = new MutableLiveData<>();
        mSpeciesAvailable = new MutableLiveData<>();
        mSpeciesAvailable.setValue(((WelfareApplication) getApplication()).getAnimalTypes(true));
        mSelectedSpecies = new MutableLiveData<>();
    }

    public LiveData<LinkedList<PetSearchData>> getSearchResults() {
        return mPetSearchResult;
    }

    public LiveData<List<AnimalType>> getSpeciesAvailable() {
        return mSpeciesAvailable;
    }

    /** Use this to respond to search result changes. */
    public LiveData<AnimalType> getSpeciesTypeSelected() {
        return mSelectedSpecies;
    }

    public MutableLiveData<NetworkStatus> getNetworkHandler() {
        return mNetworkHandler;
    }

    public MutableLiveData<Pair<Event, String>> getEventHandler() {
        return mEventHandler;
    }

    /** Search for pets on the given search parameters. */
    public void doAnimalSearch() {

        mNetworkHandler.setValue(NetworkStatus.SEARCHING_PET);

        AnimalType animalType = mSelectedSpecies.getValue();
        int animalTypeSelectedID = -1;
        if (animalType != null) {
            animalTypeSelectedID = animalType.getId();
        }

        String petName = mPetNameSearch.getValue();
        String steriStr = mPetSterilisedSearch.getValue();
        String gender = mPetGenderSearch.getValue();

        boolean hasPetName = !(petName == null || petName.isEmpty());
        boolean hasSpecies = (animalTypeSelectedID > 0);
        boolean hasSteri = steriStr != null && !steriStr.isEmpty();
        boolean hasGender = GENDER_FEMALE.equals(gender) || GENDER_MALE.equals(gender);

        Map<String, String> params = new HashMap<>();
        if (hasPetName) {
            params.put("name", petName);
        }

        if (hasSpecies) {
            params.put("animal_type_id", Integer.toString(animalTypeSelectedID));
        }

        if (hasGender) {
            params.put("gender", gender);
        }

        if (hasSteri) {
            params.put("sterilised", steriStr);
        }

        String baseURL = getApplication().getString(R.string.kBaseUrl) + "animals/list/";
        String url = NetworkUtils.createURL(baseURL, params);

        RequestQueueManager.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET,
                url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        LinkedList<PetSearchData> results = new LinkedList<>();
                        try {
                            JSONObject data = response.getJSONObject("data");
                            if (data != null) {
                                JSONArray resArr = data.getJSONArray("animals");
                                for (int i = 0; i < resArr.length(); i++) {
                                    JSONObject entry = resArr.getJSONObject(i);
                                    int id = entry.getInt("id");
                                    int animalType = entry.getInt("animal_type_id");
                                    String animalTypeDesc = entry.optString("description");
                                    String name = entry.optString("name");
                                    String dob = entry.optString("approximate_dob");
                                    String gender = entry.optString("gender");
                                    int isSterilised = entry.optInt("sterilised", -1);
                                    results.add(new PetSearchData(id, animalType, animalTypeDesc, name, dob, gender, isSterilised));
                                }
                            }
                        } catch (JSONException e) {
                            mEventHandler.setValue(new Pair<>(Event.SEARCH_PET_ERROR, getApplication().getString(R.string.internal_error_pet_search)));
                        }
                        mPetSearchResult.setValue(results);
                        mNetworkHandler.setValue(NetworkStatus.IDLE);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                mNetworkHandler.setValue(NetworkStatus.IDLE);
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    mEventHandler.setValue(new Pair<>(Event.SEARCH_PET_ERROR, getApplication().getString(R.string.conn_error_pet_search)));
                } else {
                    String errorMSG = Utils.generateErrorMessage(error, getApplication().getString(R.string.unknown_error_pet_search));
                    mEventHandler.setValue(new Pair<>(Event.SEARCH_PET_ERROR, errorMSG));
                }
                mPetSearchResult.setValue(null);
            }
        })
        {@Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + ((WelfareApplication)getApplication()).getToken());
            return headers;
        }
        }, getApplication());
    }

    /** used when user selects gender to search for. */
    public void setPetGender(String gender) {
        mPetGenderSearch.setValue(gender);
    }

    public void setPetSterilised (String isSterilised) {
        mPetSterilisedSearch.setValue(isSterilised);
    }

}
