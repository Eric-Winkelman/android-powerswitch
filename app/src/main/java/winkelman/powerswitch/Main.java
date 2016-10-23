package winkelman.powerswitch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main extends AppCompatActivity {

    private static final int RESULT_SETTINGS = 1;
    private static final int RESULT_ADD = 2;
    private HashMap<Integer, SwitchItem> _switchItems = new HashMap<Integer, SwitchItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SetupListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_preferences:
                Intent i = new Intent(this, PrefActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                break;

            case R.id.menu_add_switch:
                Intent addIntent = new Intent(this, AddItem.class);
                startActivityForResult(addIntent, RESULT_ADD);
                break;

            case R.id.menu_remove_all_switches:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("preference_switch_items", "");
                editor.commit();
                RefreshListView();
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS:
                break;
            case RESULT_ADD:
                RefreshListView();
                break;
        }
    }


    private void RefreshListView()
    {
        ListView listView = (ListView) findViewById(R.id.listView);
        SetupListView();
        listView.invalidateViews();
    }
    private void SetupListView() {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String switchItemsString = settings.getString("preference_switch_items", "");

        JSONArray jsonArray = null;
        ArrayList switchList = new ArrayList();

        try {
            jsonArray = switchItemsString.isEmpty() ? new JSONArray() : new JSONArray(switchItemsString); //GetDefaultList();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                SwitchItem sw = new SwitchItem(jsonObject);
                _switchItems.put(i, sw);
                switchList.add("1");
            }
        }
        catch (JSONException ex)
        {
            Toast toast = Toast.makeText(this, "Unable to parse data", Toast.LENGTH_LONG);
            toast.show();
        }

        ListView listView = (ListView) findViewById(R.id.listView);

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, switchList);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        private final Context _context;
        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            _context = context;
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int rowPosition = position;
            LayoutInflater inflater = (LayoutInflater) _context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.switch_list_template, parent, false);

            rowView.setLongClickable(true);
            rowView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(Main.this, v);
                    MenuInflater inflater = popup.getMenuInflater();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId())
                            {
                                case R.id.row_delete:
                                    SwitchItem switchItemToDelete = _switchItems.get(rowPosition);
                                    switchItemToDelete.Delete(Main.this, rowPosition);
                                    RefreshListView();
                                    break;
                                case R.id.row_edit:
                                    Intent addIntent = new Intent(Main.this, AddItem.class);
                                    SwitchItem switchItem = _switchItems.get(rowPosition);

                                    try {
                                        addIntent.putExtra("switchItem", switchItem.Serialize().toString());
                                        addIntent.putExtra("switchItemPosition", rowPosition);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    startActivityForResult(addIntent, RESULT_ADD);
                                    break;
                            }
                            return false;
                        }
                    });
                    inflater.inflate(R.menu.row_options, popup.getMenu());
                    popup.show();
                    return true;
                }
            });

            SwitchItem switchItem = _switchItems.get(position);

            TextView textView = (TextView) rowView.findViewById(R.id.textView);
            textView.setText(switchItem.GetName());

            return rowView;
        }
    }

    public void OnButtonClick(View view) {
        ProcessButtonPress(view, true);
    }

    public void OffButtonClick(View view) {
        ProcessButtonPress(view, false);
    }

    private void ProcessButtonPress(View view, boolean isActive)
    {
        ListView listView = (ListView) findViewById(R.id.listView);
        int position = listView.getPositionForView(view);

        LinearLayout vwParentRow = (LinearLayout)view.getParent();

        Button clickedButton;
        Button siblingButton;
        if (isActive)
        {
            clickedButton = (Button) vwParentRow.getChildAt(0);
            siblingButton = (Button) vwParentRow.getChildAt(1);
        }
        else
        {
            clickedButton = (Button) vwParentRow.getChildAt(1);
            siblingButton = (Button) vwParentRow.getChildAt(0);
        }

        SwitchItem switchItem = _switchItems.get(position);
        PowerSwitch powerSwitch = new PowerSwitch(clickedButton, siblingButton, isActive, switchItem, this);
        powerSwitch.Trigger();
    }
}
