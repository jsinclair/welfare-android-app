package za.co.aws.welfare.dataObjects;

/**
 * Simple object containing an ID and description. Used in UI objects where we want to DISPLAY
 * the description but obtain the ID on selected. An example would be the spinner for animal type
 * selection.
 */
public class SimpleDescriptionObject {

    private final String mID;

    // This is the part that will be displayed (obviously) 
    private final String mDescription;

    public SimpleDescriptionObject(String mID, String mDescription) {
        this.mID = mID;
        this.mDescription = mDescription;
    }

    public String getID() {
        return mID;
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
        if(obj instanceof SimpleDescriptionObject){
            SimpleDescriptionObject c = (SimpleDescriptionObject )obj;
            if(c.getDescription().equals(mDescription) && c.getID() == mID) return true;
        }
        return false;
    }
}
