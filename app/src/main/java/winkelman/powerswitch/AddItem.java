package winkelman.powerswitch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddItem extends AppCompatActivity implements IConnectionEventListener {

    private int existingItemPosition = -1;
    private String _onCode;
    private String _offCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Intent intent = getIntent();

        if (intent.hasExtra("switchItem"))
        {
            DefaultExistingItem(intent);
        }

        Button onButton = (Button) findViewById(R.id.getRemoteOnCode);

        onButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Button onCodeButton = (Button) findViewById(R.id.getRemoteOnCode);
                TriggerGetCode(onCodeButton);
            }
        });

        Button offButton = (Button) findViewById(R.id.getRemoteOffCode);
        offButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Button offCode = (Button) findViewById(R.id.getRemoteOffCode);
                TriggerGetCode(offCode);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_save_switch:
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                String switchItemsString = settings.getString("preference_switch_items", "");
                String newString = null;
                try
                {
                    JSONArray jsonArray;

                    if (switchItemsString.isEmpty())
                    {
                        jsonArray = new JSONArray();
                    }
                    else
                    {
                        jsonArray = new JSONArray(switchItemsString);
                    }

                    EditText switchName = (EditText) findViewById(R.id.switchName);


                    String name = switchName.getText().toString();
                    int onCode = Integer.parseInt(_onCode);
                    int offCode = Integer.parseInt(_offCode);

                    //todo, add radio set
                    SwitchItem.Types type = SwitchItem.Types.OnOff; // SwitchItem.GetType(offCodeText.getText().toString());

                    SwitchItem newItem = new SwitchItem(name, onCode, offCode, type);

                    if (existingItemPosition != -1)
                    {
                        JSONObject existingObject = jsonArray.getJSONObject(existingItemPosition);
                        newItem.UpdateExisting(existingObject);
                    }
                    else
                    {
                        jsonArray.put(newItem.Serialize());
                    }

                    newString = jsonArray.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("preference_switch_items", newString);
                editor.commit();
                finish();
                break;
        }

        return true;
    }

    private void DefaultExistingItem(Intent intent)
    {
        existingItemPosition = intent.getIntExtra("switchItemPosition", -1);

        JSONObject switchItemJson = null;
        try {
            switchItemJson = new JSONObject(intent.getStringExtra("switchItem"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SwitchItem switchItem = new SwitchItem(switchItemJson);
        EditText switchName = (EditText) findViewById(R.id.switchName);
        _onCode = Integer.toString(switchItem.GetRfCodeOn());
        _offCode = Integer.toString(switchItem.GetRfCodeOff());

        Button onButton = (Button) findViewById(R.id.getRemoteOnCode);
        Button offButton = (Button) findViewById(R.id.getRemoteOffCode);
        onButton.setBackgroundColor(PowerSwitch.GetSuccessColor());
        offButton.setBackgroundColor(PowerSwitch.GetSuccessColor());

        switchName.setText(String.valueOf(switchItem.GetName()));
    }

    private void TriggerGetCode(Button button)
    {
        Toast toast = Toast.makeText(this, "Aim remote at the pi and press desired button", Toast.LENGTH_LONG);
        toast.show();

        button.setBackgroundColor(PowerSwitch.GetDefaultColor());
        String urlString = Connection.GetBaseAddress(this) + "/getCode";

        Connection connection = new Connection(urlString, this, button);
        connection.execute("");
    }

    @Override
    public void onEventCompleted(JSONObject result, final View view) throws JSONException {
        if (result == null)
        {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    view.setBackgroundColor(PowerSwitch.GetErrorColor());
                }
            });
            return;
        }
        String code = result.getString("code");

        if (code == null || code.isEmpty() || code == "0")
        {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    view.setBackgroundColor(PowerSwitch.GetErrorColor());
                }
            });
            return;
        }

        if (view.getId() == R.id.getRemoteOnCode)
        {
            _onCode = code;
        }
        else if (view.getId() == R.id.getRemoteOffCode)
        {
            _offCode = code;
        }

        this.runOnUiThread(new Runnable() {
            public void run() {
                view.setBackgroundColor(PowerSwitch.GetSuccessColor());
            }
        });
    }

    @Override
    public void onEventFailed() {

    }
}
