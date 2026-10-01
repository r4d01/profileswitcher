package com.github.r4d01.profileswitcher;

import android.app.Activity;
import android.app.KeyguardManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.widget.Toast;

import com.github.r4d01.profileswitcher.manager.SoundProfileManager;
import com.github.r4d01.profileswitcher.model.SoundProfile;

/**
 * Invisible launcher activity: switches to the next sound profile, confirms it with a vibration
 * and a toast with the profile icon, and finishes immediately. It only switches while the phone is
 * unlocked; presses on the lock screen or with the screen off are ignored.
 * <p>
 * Start it from the app icon, from Activity Launcher (e.g. bound to the Moto AI key), or via
 * {@code adb shell am start -n com.github.r4d01.profileswitcher/.ProfileSwitchActivity}.
 */
public class ProfileSwitchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isPhoneUnlocked()) {
            // Ignore presses on the lock screen or with the screen off (including presses Moto
            // held while the phone was asleep and delivers when it wakes up)
            finish();
            return;
        }

        SoundProfileManager manager = new SoundProfileManager(this);
        SoundProfile newProfile = manager.cycleProfile();

        if (newProfile == null) {
            // Switching to DND requires Notification Policy Access
            Toast.makeText(this, R.string.dnd_access_required, Toast.LENGTH_LONG).show();
            startActivity(manager.getNotificationPolicyAccessSettingsIntent());
        } else {
            vibrate(newProfile);
            Toast.makeText(this, newProfile.iconRes, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /** True if the screen is on and the lock screen is not showing. */
    private boolean isPhoneUnlocked() {
        PowerManager powerManager = getSystemService(PowerManager.class);
        KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
        return (powerManager == null || powerManager.isInteractive())
                && (keyguardManager == null || !keyguardManager.isKeyguardLocked());
    }

    /**
     * Confirms the switch with 1, 2 or 3 short buzzes identifying the profile. Uses the alarm
     * usage, which DND lets through.
     */
    @SuppressWarnings("deprecation")
    private void vibrate(SoundProfile profile) {
        VibratorManager vibratorManager = getSystemService(VibratorManager.class);
        if (vibratorManager == null) {
            return;
        }
        Vibrator vibrator = vibratorManager.getDefaultVibrator();
        VibrationEffect effect = VibrationEffect.createWaveform(profile.vibrationTimings(), -1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM));
        } else {
            vibrator.vibrate(effect, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
        }
    }
}
