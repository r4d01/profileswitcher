package com.github.r4d01.profileswitcher.manager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.Settings;

import com.github.r4d01.profileswitcher.model.SoundProfile;

/**
 * Reads and sets the system sound profile (NORMAL, VIBRATION, DND).
 */
public class SoundProfileManager {

    private final AudioManager audioManager;
    private final NotificationManager notificationManager;

    public SoundProfileManager(Context context) {
        this.audioManager = context.getSystemService(AudioManager.class);
        this.notificationManager = context.getSystemService(NotificationManager.class);
    }

    /**
     * Switches to the next sound profile (NORMAL -> VIBRATION -> DND -> NORMAL).
     *
     * @return the new profile, or null if switching failed (DND requires Notification Policy Access).
     */
    public SoundProfile cycleProfile() {
        SoundProfile next = getCurrentProfile().next();
        return setSoundProfile(next) ? next : null;
    }

    /**
     * @return an Intent opening the system screen where Notification Policy Access (DND access) is granted.
     */
    public Intent getNotificationPolicyAccessSettingsIntent() {
        return new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
    }

    private SoundProfile getCurrentProfile() {
        int filter = notificationManager.getCurrentInterruptionFilter();
        if (filter != NotificationManager.INTERRUPTION_FILTER_ALL
                && filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN) {
            return SoundProfile.DND;
        }
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_VIBRATE:
                return SoundProfile.VIBRATION;
            case AudioManager.RINGER_MODE_SILENT:
                return SoundProfile.DND;
            default:
                return SoundProfile.NORMAL;
        }
    }

    private boolean setSoundProfile(SoundProfile profile) {
        switch (profile) {
            case NORMAL:
                return leaveDndAndSetRingerMode(AudioManager.RINGER_MODE_NORMAL);
            case VIBRATION:
                return leaveDndAndSetRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            case DND:
                return enterDnd();
            default:
                return false;
        }
    }

    private boolean enterDnd() {
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            return false;
        }
        try {
            notificationManager.setNotificationPolicy(createDndPolicy());
            // Calls let through by the policy vibrate instead of ringing
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * DND lets through only calls from starred contacts, alarms and media.
     * Messages, other calls, repeat callers and all other notifications are blocked.
     */
    private NotificationManager.Policy createDndPolicy() {
        int categories = NotificationManager.Policy.PRIORITY_CATEGORY_CALLS
                | NotificationManager.Policy.PRIORITY_CATEGORY_ALARMS
                | NotificationManager.Policy.PRIORITY_CATEGORY_MEDIA;
        // Keep the user's choice of how blocked notifications are hidden
        int suppressedVisualEffects = notificationManager.getNotificationPolicy().suppressedVisualEffects;
        return new NotificationManager.Policy(categories,
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED,
                NotificationManager.Policy.PRIORITY_SENDERS_STARRED,
                suppressedVisualEffects);
    }

    private boolean leaveDndAndSetRingerMode(int ringerMode) {
        try {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
            audioManager.setRingerMode(ringerMode);
            return true;
        } catch (SecurityException e) {
            // Changing the ringer mode while DND is active requires Notification Policy Access
            return false;
        }
    }
}
