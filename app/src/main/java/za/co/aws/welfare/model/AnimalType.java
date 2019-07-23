package za.co.aws.welfare.model;

/**
 *
 * Basic data holder class for animal types. Retrieved on login and used on data screens.
 *
 */
public class AnimalType {
    private final int mId;
    private final String mDescription;

    public AnimalType(int id, String description) {
        this.mId = id;
        this.mDescription = description;
    }

    public int getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    //to display object as a string in spinner
    @Override
    public String toString() {
        return mDescription;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof AnimalType){
            AnimalType c = (AnimalType )obj;
            if(c.getDescription().equals(mDescription) && c.getId() == mId) return true;
        }
        return false;
    }
}
