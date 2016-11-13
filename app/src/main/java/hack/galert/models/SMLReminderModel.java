package hack.galert.models;

/**
 * Created by Ankit on 10/15/2016.
 */
public class SMLReminderModel {

    public int _id_reminder;
    public String title;
    public String content;
    public String latitude;
    public String longitude;


    public SMLReminderModel(int _id_reminder, String title, String content, String latitude, String longitude) {
        this._id_reminder = _id_reminder;
        this.title = title;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int get_id_reminder() {
        return _id_reminder;
    }

    public void set_id_reminder(int _id_reminder) {
        this._id_reminder = _id_reminder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public double getLatitude() {
        return Double.parseDouble(latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return Double.parseDouble(longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
