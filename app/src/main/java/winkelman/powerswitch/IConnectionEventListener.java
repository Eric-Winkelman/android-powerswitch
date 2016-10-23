package winkelman.powerswitch;

import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eric on 8/21/16.
 */
public interface IConnectionEventListener {
    public void onEventCompleted(JSONObject result, View triggeringView) throws JSONException;
    public void onEventFailed();
}
