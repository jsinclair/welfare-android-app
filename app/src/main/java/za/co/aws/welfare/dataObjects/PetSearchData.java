package za.co.aws.welfare.dataObjects;

/** Used during a search for pets. */
public class PetSearchData {

    private int mID;
    private int mAnimalTypeID;
    private String mAnimalTypeDesc;
    private String mPetName;
    private String mPetDOB;
    private String mGender;
    private boolean mSterilised;

    public PetSearchData(int mID, int mAnimalTypeID, String mAnimalTypeDesc, String mPetName, String mPetDOB, String gender, boolean sterilised) {
        this.mID = mID;
        this.mAnimalTypeID = mAnimalTypeID;
        this.mAnimalTypeDesc = mAnimalTypeDesc;
        this.mPetName = mPetName;
        this.mPetDOB = mPetDOB;
        this.mGender = gender;
        this.mSterilised = sterilised;
    }

    public int getID() {
        return mID;
    }

    public int getAnimalTypeID() {
        return mAnimalTypeID;
    }

    public String getPetName() {
        return mPetName;
    }

    public String getPetDOB() {
        return mPetDOB;
    }

    public String getmAnimalTypeDesc() {
        return mAnimalTypeDesc;
    }

    public String getGender() {
        return mGender;
    }

    public boolean isSterilised() {
        return mSterilised;
    }
}
