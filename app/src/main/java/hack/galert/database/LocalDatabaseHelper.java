package hack.galert.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import hack.galert.log.L;
import hack.galert.models.FriendsModel;
import hack.galert.models.SMLReminderModel;
import hack.galert.models.SettingsModel;

/**
 * Created by Ankit on 10/19/2016.
 */
public class LocalDatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_SETTINGS_TABLE = "create table settings (" +
            "volume integer," +
            "mobiledata text," +
            "wifi text," +
            "vibration text," +
            "bluetooth text," +
            "latitude text," +
            "title text," +
            "longitude text" +
            ");";

    private static final String CREATE_FRIENDS_TABLE = "create table friends (" +
            "name text," +
            "allowed text," +
            "_id_friend text" +
            ");";


    private static final String CREATE_REMINDERS_TABLE = "create table reminders (" +
            "_id_reminder integer," +
            "title text," +
            "content text," +
            "latitude text," +
            "longitude text);";

    private Context mContent;
    private static final String DATABASE_NAME = "mydb";
    private static final int DATABASE_VERSION = 1;


    public LocalDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    private static Context context;
    private static LocalDatabaseHelper mInstance;

    public static LocalDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LocalDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_FRIENDS_TABLE);
        L.m("Database", "created friends table");
        sqLiteDatabase.execSQL(CREATE_REMINDERS_TABLE);
        L.m("Database", "created reminders table");
        sqLiteDatabase.execSQL(CREATE_SETTINGS_TABLE);
        L.m("Database", "created settings table");

    }

    public void writeSetting(SettingsModel settingsModel) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("volume", settingsModel.volumeLevel);
        values.put("title", settingsModel.title);
        values.put("mobiledata", settingsModel.mobileDataState);
        values.put("wifi", settingsModel.wifiState);
        values.put("vibration", settingsModel.vibrationMode);
        values.put("bluetooth", settingsModel.bluetoothState);
        values.put("latitude", settingsModel.latitude);
        values.put("longitude", settingsModel.longitude);

        long id = database.insert("settings", null, values);
        L.m("Database", "Settigs inserted " + id);

    }

    public ArrayList<SettingsModel> getSettings() {

        ArrayList<SettingsModel> settingsModelArrayList = new ArrayList<>();
        SQLiteDatabase databaseObj = getReadableDatabase();
        String cols[] = {"title", "volume", "mobiledata", "wifi", "vibration", "bluetooth", "latitude", "longitude"};
        Cursor settingsCursor = databaseObj.query("settings", cols, null, null, null, null, null);

        boolean hasNext = true;

        settingsCursor.moveToFirst();

        if (settingsCursor.getCount() > 0)
            while (hasNext) {
                settingsModelArrayList.add(new SettingsModel(settingsCursor.getString(settingsCursor.getColumnIndex("title")),
                        settingsCursor.getDouble(settingsCursor.getColumnIndex("volume")),
                        Boolean.parseBoolean(settingsCursor.getString(settingsCursor.getColumnIndex("bluetooth"))),
                        Boolean.parseBoolean(settingsCursor.getString(settingsCursor.getColumnIndex("wifi"))),
                        Boolean.parseBoolean(settingsCursor.getString(settingsCursor.getColumnIndex("mobiledata"))),
                        Boolean.parseBoolean(settingsCursor.getString(settingsCursor.getColumnIndex("vibration"))),
                        settingsCursor.getString(settingsCursor.getColumnIndex("latitude")),
                        settingsCursor.getString(settingsCursor.getColumnIndex("longitude"))
                ));

                hasNext = settingsCursor.moveToNext();
            }

        L.m("Database", " read " + settingsModelArrayList.size() + " settings entries");

        return settingsModelArrayList;
    }

    public void writeFriends(ArrayList<FriendsModel> friendsList) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = null;
        long id = -1;
        for (int i = 0; i < friendsList.size(); i++) {

            FriendsModel friend = friendsList.get(i);
            values = new ContentValues();

            values.put("name", friend.friendsName);
            values.put("allowed", String.valueOf(friend.isAllowed));
            values.put("_id_friend", friend.friendsID);

            id = database.insert("friends", null, values);

        }

        L.m("Database", "Friends inserted " + id);


    }

    public ArrayList<FriendsModel> getFriends() {

        ArrayList<FriendsModel> friends = new ArrayList<>();
        SQLiteDatabase databaseObj = getReadableDatabase();
        String cols[] = {"name", "allowed", "_id_friend"};
        Cursor friendsCursor = databaseObj.query("friends", cols, null, null, null, null, null);

        boolean hasNext = true;

        friendsCursor.moveToFirst();

        if (friendsCursor.getCount() > 0)
            while (hasNext) {
                friends.add(new FriendsModel(
                        friendsCursor.getString(friendsCursor.getColumnIndex("name")),
                        friendsCursor.getString(friendsCursor.getColumnIndex("_id_friend")),
                        Boolean.parseBoolean(friendsCursor.getString(friendsCursor.getColumnIndex("allowed")))
                ));

                hasNext = friendsCursor.moveToNext();
            }

        L.m("Database", " read " + friends.size() + " friends entries");

        return friends;
    }

    public void writeReminder(SMLReminderModel reminder) {

        SQLiteDatabase database = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("_id_reminder", reminder._id_reminder);
        values.put("title", reminder.title);
        values.put("content", reminder.content);
        values.put("latitude", reminder.latitude);
        values.put("longitude", reminder.longitude);

        long id = database.insert("reminders", null, values);
        L.m("Database", "Reminders inserted " + id);

    }

    public ArrayList<SMLReminderModel> getReminders() {

        ArrayList<SMLReminderModel> reminders = new ArrayList<>();
        SQLiteDatabase databaseObj = getReadableDatabase();

        String cols[] = {"_id_reminder", "title", "content", "latitude", "longitude"};

        Cursor remindersCursor = databaseObj.query("reminders", cols, null, null, null, null, null);

        boolean hasNext = true;

        remindersCursor.moveToFirst();

        if (remindersCursor.getCount() > 0)
            while (hasNext) {
                reminders.add(new SMLReminderModel(
                        Integer.parseInt(remindersCursor.getString(remindersCursor.getColumnIndex("_id_reminder"))),
                        remindersCursor.getString(remindersCursor.getColumnIndex("title")),
                        remindersCursor.getString(remindersCursor.getColumnIndex("content")),
                        remindersCursor.getString(remindersCursor.getColumnIndex("latitude")),
                        remindersCursor.getString(remindersCursor.getColumnIndex("longitude"))
                ));

                hasNext = remindersCursor.moveToNext();
            }

        L.m("Database", " read " + reminders.size() + " reminders entries");

        return reminders;

    }

    public void truncateSettingsTable(){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("drop table settings;");
        sqLiteDatabase.execSQL(CREATE_SETTINGS_TABLE);
        L.m("Database", "created settings table");

    }


    public void truncateRemindersTable(){

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        sqLiteDatabase.execSQL("drop table reminders;");
        sqLiteDatabase.execSQL(CREATE_REMINDERS_TABLE);
        L.m("Database", "created reminders table");
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        onCreate(sqLiteDatabase);

    }

}
