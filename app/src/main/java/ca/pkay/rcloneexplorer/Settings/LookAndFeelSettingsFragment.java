package ca.pkay.rcloneexplorer.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import ca.pkay.rcloneexplorer.Dialogs.ColorPickerDialog;
import ca.pkay.rcloneexplorer.R;

public class LookAndFeelSettingsFragment extends Fragment {

    private Context context;
    private OnThemeHasChanged listener;
    private View primaryColorElement;
    private ImageView primaryColorPreview;
    private View accentColorElement;
    private ImageView accentColorPreview;
    private Switch darkThemeSwitch;
    private TextView darkThemeSwitchLabel;
    private Switch followSystemThemeSwitch;
    private View darkThemeElement;
    private Switch wrapFilenamesSwitch;
    private View wrapFilenamesElement;
    private boolean isDarkTheme;

    public interface OnThemeHasChanged {
        void onThemeChanged();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LookAndFeelSettingsFragment() {
    }

    public static LookAndFeelSettingsFragment newInstance() {
        return new LookAndFeelSettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.look_and_feel_settings_fragment, container, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);

        getViews(view);
        setDefaultStates();
        setClickListeners();

        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.look_and_feel));
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnThemeHasChanged) {
            listener = (OnThemeHasChanged) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement listener");
        }
    }

    private void getViews(View view) {
        primaryColorElement = view.findViewById(R.id.primary_color);
        primaryColorPreview = view.findViewById(R.id.primary_color_preview);
        accentColorElement = view.findViewById(R.id.accent_color);
        accentColorPreview = view.findViewById(R.id.accent_color_preview);
        darkThemeSwitch = view.findViewById(R.id.dark_theme_switch);
        darkThemeSwitchLabel = view.findViewById(R.id.dark_theme_switch_label);
        followSystemThemeSwitch = view.findViewById(R.id.follow_system_theme_switch);
        darkThemeElement = view.findViewById(R.id.dark_theme);
        wrapFilenamesSwitch = view.findViewById(R.id.wrap_filenames_switch);
        wrapFilenamesElement = view.findViewById(R.id.wrap_filenames);
    }

    private void setDefaultStates() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isDarkTheme = sharedPreferences.getBoolean(getString(R.string.pref_key_dark_theme), false);
        boolean themeFollowSystem = sharedPreferences.getBoolean(getString(R.string.pref_key_theme_follow_system), true);
        boolean isWrapFilenames = sharedPreferences.getBoolean(getString(R.string.pref_key_wrap_filenames), true);

        darkThemeSwitch.setChecked(isDarkTheme);
        followSystemThemeSwitch.setChecked(themeFollowSystem);
        wrapFilenamesSwitch.setChecked(isWrapFilenames);
        setDarkThemeSwitchState();
    }

    private void setClickListeners() {
        primaryColorElement.setOnClickListener(v -> showPrimaryColorPicker());
        accentColorElement.setOnClickListener(v -> showAccentColorPicker());
        darkThemeElement.setOnClickListener(v -> darkThemeSwitch.setChecked(!darkThemeSwitch.isChecked()));
        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onDarkThemeClicked(isChecked));
        followSystemThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onFollowSystemClicked(isChecked));
        wrapFilenamesElement.setOnClickListener(v -> wrapFilenamesSwitch.setChecked(!wrapFilenamesSwitch.isChecked()));
        wrapFilenamesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> onWrapFilenamesClicked(isChecked));
    }

    private void showPrimaryColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_primary), R.color.colorPrimary);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .setTitle(R.string.primary_color_picker_title)
                .setColorChoices(R.array.primary_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(isDarkTheme)
                .setListener(this::onPrimaryColorSelected);

        colorPickerDialog.show(getChildFragmentManager(), "primary color picker");
    }

    private void showAccentColorPicker() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultColor = sharedPreferences.getInt(getString(R.string.pref_key_color_accent), R.color.colorAccent);

        ColorPickerDialog colorPickerDialog = new ColorPickerDialog()
                .setTitle(R.string.accent_color_picker_title)
                .setColorChoices(R.array.accent_color_choices)
                .setDefaultColor(defaultColor)
                .setDarkTheme(isDarkTheme)
                .setListener(this::onAccentColorSelected);

        colorPickerDialog.show(getChildFragmentManager(), "accent color picker");
    }

    private void onPrimaryColorSelected(int color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_key_color_primary), color);
        editor.apply();

        primaryColorPreview.setColorFilter(color);

        listener.onThemeChanged();
    }

    private void onAccentColorSelected(int color) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_key_color_accent), color);
        editor.apply();

        accentColorPreview.setColorFilter(color);

        listener.onThemeChanged();
    }

    private void onDarkThemeClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_dark_theme), isChecked);
        editor.apply();

        listener.onThemeChanged();
    }

    private void onFollowSystemClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_theme_follow_system), isChecked);
        editor.apply();
        setDarkThemeSwitchState();

        listener.onThemeChanged();
    }

    private void onWrapFilenamesClicked(boolean isChecked) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_key_wrap_filenames), isChecked);
        editor.apply();
    }

    private void setDarkThemeSwitchState(){
        darkThemeSwitch.setEnabled(!followSystemThemeSwitch.isChecked());
        darkThemeElement.setEnabled(!followSystemThemeSwitch.isChecked());
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        if(followSystemThemeSwitch.isChecked()){
            theme.resolveAttribute(R.attr.textColorSecondary, typedValue, true);
        }else{
            theme.resolveAttribute(R.attr.textColorPrimary, typedValue, true);
        }
        darkThemeSwitchLabel.setTextColor(typedValue.data);
    }
}
