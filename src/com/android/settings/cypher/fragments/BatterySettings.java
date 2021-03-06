/*
 * Copyright (C) 2016 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cypher.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.provider.Settings;
import android.provider.SearchIndexableResource;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.preference.SystemSettingSwitchPreference;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

public class BatterySettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, 
        Indexable {
			
    private static final String TAG = "BatterySettings";
	
    private static final String STATUSBAR_BATTERY_PERCENT = "statusbar_battery_percent";
    private static final String STATUSBAR_BATTERY_PERCENT_INSIDE = "statusbar_battery_percent_inside";
	
	private static final String KEY_BATTERY_LIGHT = "battery_light";

    private static final String CATEGORY_BLEDS = "bleds";
	
	private Preference mBattLedFrag;

    private ListPreference mBatteryPercent;
    private SwitchPreference mPercentInside;
    private int mShowPercent;
    private int mBatteryStyleValue;
    private int mShowBattery = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_battery_settings);  
    }
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();
         
        PreferenceScreen prefScreen = getPreferenceScreen();
		
	final PreferenceCategory bleds = (PreferenceCategory) findPreference(CATEGORY_BLEDS);

        mBattLedFrag = findPreference(KEY_BATTERY_LIGHT);
        //Remove battery led settings if device doesnt support it
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            bleds.removePreference(findPreference(KEY_BATTERY_LIGHT));
        }

        //Remove led category if device doesnt support notification or battery
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_intrusiveBatteryLed)) {
            prefScreen.removePreference(findPreference(CATEGORY_BLEDS));
        }
		
		mBatteryStyleValue = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_STYLE, 0);
				
		mShowBattery = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_ENABLE, 1);

        mBatteryPercent = (ListPreference) findPreference(STATUSBAR_BATTERY_PERCENT);
        mShowPercent = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_PERCENT, 2);

        mBatteryPercent.setValue(Integer.toString(mShowPercent));
        mBatteryPercent.setSummary(mBatteryPercent.getEntry());
        mBatteryPercent.setOnPreferenceChangeListener(this);

        mPercentInside = (SwitchPreference) findPreference(STATUSBAR_BATTERY_PERCENT_INSIDE);
        updateEnablement();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ADDITIONS;
    }
	
     public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryPercent) {
            mShowPercent = Integer.valueOf((String) newValue);
            int index = mBatteryPercent.findIndexOfValue((String) newValue);
            mBatteryPercent.setSummary(
                    mBatteryPercent.getEntries()[index]);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_PERCENT, mShowPercent);
            updateEnablement();
            return true;
        }
        return false;
    }

     private void updateEnablement() {
        mPercentInside.setEnabled(mShowBattery != 0 && mBatteryStyleValue < 3 && mShowPercent != 0);
        mBatteryPercent.setEnabled(mShowBattery != 0 && mBatteryStyleValue != 3);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        @Override
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList<SearchIndexableResource>();

            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.advanced_battery_settings;
            result.add(sir);

            return result;
        }

        @Override
        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList<String>();
            return result;
        }
    };
}