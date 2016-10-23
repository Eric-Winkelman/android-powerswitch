package winkelman.powerswitch;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eric on 4/17/16.
 */
public class SwitchItem {
    //this will just store the name of the switch and frequency. We will store a json list of these in
    //preferences so we can retain all of their setup info
    //maybe even store the last known state? might be too much
    private String _name;
    public String GetName()
    {
        return  _name;
    }

    private int _rfCodeOn;
    public int GetRfCodeOn()
    {
        return _rfCodeOn;
    }

    private int _rfCodeOff;
    public int GetRfCodeOff()
    {
        return _rfCodeOff;
    }

    public int GetRfCode(boolean isActive)
    {
        if (isActive)
        {
            return _rfCodeOn;
        }
        else
        {
            return _rfCodeOff;
        }
    }

    private Types _type;
    public Types GetType()
    {
        return _type;
    }

    public enum Types
    {
        Toggle,
        OnOff
    }

    public SwitchItem(JSONObject json)
    {
        try
        {
            _name = json.get("name").toString();
            _rfCodeOn = Integer.parseInt(json.get("rfCodeOn").toString());
            _rfCodeOff = Integer.parseInt(json.get("rfCodeOff").toString());
        }
        catch (JSONException e)
        {

        }
    }

    public SwitchItem(String name, int rfCodeOn, int rfCodeOff, Types type)
    {
        _name = name;
        _rfCodeOn = rfCodeOn;
        _rfCodeOff = rfCodeOff;
        _type = type;
    }

    public JSONObject Serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", _name);

        jsonObject.put("rfCodeOn", _rfCodeOn);
        jsonObject.put("rfCodeOff", _rfCodeOff);

        jsonObject.put("type", _type);

        return jsonObject;
    }

    public void UpdateExisting(JSONObject jsonObject) throws JSONException {

        jsonObject.put("name", _name);

        jsonObject.put("rfCodeOn", _rfCodeOn);
        jsonObject.put("rfCodeOff", _rfCodeOff);

        jsonObject.put("type", _type);
    }

    public boolean Delete(Activity activity, int position)
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        String switchItemsString = settings.getString("preference_switch_items", "");
        String newString = null;

        JSONArray newArray = new JSONArray();
        try {
            JSONArray jsonArray;

            if (switchItemsString.isEmpty()) {
                jsonArray = new JSONArray();
            } else {
                jsonArray = new JSONArray(switchItemsString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (i != position)
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        newArray.put(jsonObject);
                    }
                }
            }
            newString = newArray.toString();
        }
        catch (Exception e)
        {

        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        editor.putString("preference_switch_items", newString);
        return editor.commit();
    }

    public static Types GetType(String abbr){
        for(Types v : Types.values()){
            if( v.name().equals(abbr)){
                return v;
            }
        }
        return null;
    }
}
