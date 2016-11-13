package hack.galert.models;

/**
 * Created by Ankit on 10/15/2016.
 */
public class FriendsModel {

    public String friendsName;
    public String friendsID;
    public boolean isAllowed;

    public FriendsModel(String friendsName, String friendsID, boolean isAllowed) {
        this.friendsName = friendsName;
        this.friendsID = friendsID;
        this.isAllowed = isAllowed;
    }

    public String getFriendsName() {
        return friendsName;
    }

    public void setFriendsName(String friendsName) {
        this.friendsName = friendsName;
    }

    public String getFriendsID() {
        return friendsID;
    }

    public void setFriendsID(String friendsID) {
        this.friendsID = friendsID;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public void setIsAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }
}
