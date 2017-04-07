package com.xwray.groupie.example;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;

import com.xwray.groupie.example.R;
import com.xwray.groupie.example.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        final Prefs prefs = Prefs.get(this);

        binding.showBounds.theSwitch.setChecked(prefs.getShowBounds());
        binding.showOffsets.theSwitch.setChecked(prefs.getShowOffsets());

        binding.showBounds.theSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean showBounds) {
                prefs.setShowBounds(showBounds);
            }
        });

        binding.showOffsets.theSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean showOffsets) {
                prefs.setShowOffsets(showOffsets);
            }
        });
    }
}
