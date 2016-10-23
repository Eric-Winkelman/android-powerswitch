package winkelman.powerswitch;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class Connection extends AsyncTask {

    private String _urlString;
    private IConnectionEventListener _callback;
    private View _triggeringViewPart;

    public Connection(String urlString, IConnectionEventListener callback, View triggeringViewPart)
    {
        _urlString = urlString;
        _callback = callback;
        _triggeringViewPart = triggeringViewPart;
    }

    @Override
    protected Object doInBackground(Object... arg0) {

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        String result = Connect();
        JSONObject jsonObj = null;
        boolean success = false;
        try
        {
            if (result == null)
            {
                success = false;

            }
            else
            {
                jsonObj = new JSONObject(result);
                String successString = jsonObj.getString("success");
                success = successString.isEmpty() ? false : Boolean.parseBoolean(successString);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        if(_callback != null) {
            try {
                _callback.onEventCompleted(jsonObj, _triggeringViewPart);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    private String Connect()
    {
        HttpURLConnection urlConnection = null;
        String strFileContents = null;
        try
        {
            URL url = new URL(_urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000); //set timeout to 5 seconds

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            byte[] contents = new byte[1024];

            int bytesRead = 0;

            while( (bytesRead = in.read(contents)) != -1){
                strFileContents = new String(contents, 0, bytesRead);
            }

        }
        catch (Exception e)
        {
        }
        finally{
            urlConnection.disconnect();
        }

        return strFileContents;
    }

    public static String GetBaseAddress(Activity activity)
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        String piAddress = settings.getString("preference_ip_address", "");

        return "http://" + piAddress;
    }
}
