package io.yxy.mobgroupchat;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yxy on 15/7/1.
 */


public class Utils {
    private Context context;
    private SharedPreferences sharedPref;

    private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
    private static final int KEY_MODE_PRIVATE = 0;
    private static final String KEY_SESSION_ID = "sessionId", FLAG_MESSAGE = "message";

    public Utils(Context context) {
        this.context = context;
        sharedPref = this.context.getSharedPreferences(KEY_SHARED_PREF, KEY_MODE_PRIVATE);
    }

    public void storeSessionId(final String sessionId) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.apply();
    }

    public String getSessionId() {
        return sharedPref.getString(KEY_SESSION_ID, null);
    }

    public String getSendMessageJSON(String message) throws JSONException {
        String json = null;

        JSONObject jObj = new JSONObject();
        try {
            jObj.put("flag", FLAG_MESSAGE);
            jObj.put(KEY_SESSION_ID, getSessionId());
            jObj.put("message", message);
            json = jObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;

    }
}
