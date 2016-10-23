package winkelman.powerswitch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.widget.Button;

/**
 * Created by eric on 1/4/16.
 */
public class PowerSwitch {

    private Activity _mainActivity;
    private Button _button;
    private Button _siblingButton;
    private boolean _isActive;
    private SwitchItem _switchItem;

    public PowerSwitch(Button button, Button siblingButton, boolean isActive, SwitchItem switchItem, Activity mainActivity)
    {
        _button = button;
        _siblingButton = siblingButton;
        _switchItem = switchItem;
        _mainActivity = mainActivity;
        _isActive = isActive;
    }

    public boolean Trigger()
    {
        String url = GetURL();
        String response = GetPostData(url);

        boolean success = response != null && !response.isEmpty();

        if (success) {
            _button.setTextColor(GetSuccessColor());
            _siblingButton.setTextColor(GetDefaultColor());
        } else
        {
            _button.setTextColor(GetErrorColor());
            _siblingButton.setTextColor(GetDefaultColor());
        }
        return success;
    }

    private String GetURL()
    {
        return Connection.GetBaseAddress(_mainActivity) + "/toggleSwitch/" + _switchItem.GetRfCode(_isActive);
    }

    public static int GetSuccessColor()
    {
        return Color.rgb(0,255,0);
    }

    public static int GetErrorColor()
    {
        return Color.rgb(255,0,0);
    }

    public static int GetDefaultColor()
    {
        return Color.rgb(0,0,0);
    }

    private String GetPostData(String urlString)
    {
        Object result = new Connection(urlString, null, null).execute("");
        return result.toString();
    }
}
