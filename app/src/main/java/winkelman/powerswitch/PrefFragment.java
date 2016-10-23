package winkelman.powerswitch;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class PrefFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        EditTextPreference ipAddress = (EditTextPreference) findPreference("preference_ip_address");
        EditTextPreference piText = (EditTextPreference) findPreference("preference_pi_text");

        SetSummaryValue(ipAddress);
        SetSummaryValue(piText);

        SetOnChangeEvent(ipAddress);
        SetSummaryValue(piText);
    }

    private void SetSummaryValue(EditTextPreference preference) {
        preference.setSummary(preference.getText().toString());
    }

    private void SetOnChangeEvent(Preference preference)
    {
        preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal) {
                final String value = (String) newVal;
                preference.setSummary(value);
                return true;
            }
        });
    }
}