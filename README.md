# Profile Switcher

A tiny Android app that, with the help of
[Activity Launcher](https://play.google.com/store/apps/details?id=de.szalkowski.activitylauncher),
turns the **Motorola AI key** (or any other key) into a sound profile switch. Each press cycles the
phone's sound profile and quits:

**Normal → Vibration → Do Not Disturb → Normal → …**

There is no app screen. Every press switches to the next profile, confirms it with 1, 2 or 3 short
buzzes plus a small toast with the new profile icon (🔔 / 📳 / 🔕), and exits immediately. It only
switches while the phone is **unlocked** — presses on the lock screen or with the screen off are
ignored (see [known limitations](#known-limitations)).

See [setup](#binding-it-to-the-moto-ai-key-tested-on-motorola-edge-70-pro) for binding it to the AI
key. It also works from the app icon or from automation apps.

Pure Java, no third-party or AndroidX libraries; the APK is about 100 KB.

## Requirements

- Android 12 (API 31) or newer
- **Do Not Disturb access** (Notification Policy Access) — needed to turn DND on and off.
- [Activity Launcher](https://play.google.com/store/apps/details?id=de.szalkowski.activitylauncher)
  — for binding the app to the Motorola AI key

## Getting the app

This is a personal tool and is **not** published on Google Play or any other app store. Either
[build it yourself](#building) or download the APK from the
[Releases page](https://github.com/r4d01/profileswitcher/releases) — but only if you trust the
author, because it isn't reviewed by any store.

## Building

Open the project in Android Studio and click **Run**, or build from the command line:

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/profileswitcher-debug.apk`. Install it with:

```bash
adb install -r app/build/outputs/apk/debug/profileswitcher-debug.apk
```

Run the unit tests with:

```bash
./gradlew testDebugUnitTest
```

## Setup

1. Install the app and tap its icon. The first switches (Normal → Vibration) work right away.
2. When it first tries to switch to Do Not Disturb, the app opens the system
   **Do Not Disturb access** screen — allow it for *Profile Switcher*. From then on, every launch
   just switches the profile.
3. Bind it to a hardware key (below), or just keep using the app icon.

### Binding it to the Moto AI key (tested on Motorola Edge 70 Pro)

Motorola phones don't let you assign an arbitrary app to the AI key from the normal settings. The
key's hidden configuration screen can be opened with
[Activity Launcher](https://play.google.com/store/apps/details?id=de.szalkowski.activitylauncher):

1. In the AI key settings, set the AI key to **No action**, so Moto AI no longer takes it.
2. Install **Activity Launcher** from Google Play and open it.
3. Search for `RedKeyActivity` and open the **AI Key** activity (`.activity.RedKeyActivity`).
4. In the screen that opens, set the AI key to launch **Profile Switcher**.
5. Press the key: you should feel 1, 2 or 3 buzzes and see the new profile icon.

### Other phones and automation apps

Anything that can start an app or an activity can trigger a switch — a launcher gesture, a button
remapper, Tasker, etc. The activity to start is
`com.github.r4d01.profileswitcher/.ProfileSwitchActivity`. From a computer, for testing:

```bash
adb shell am start -n com.github.r4d01.profileswitcher/.ProfileSwitchActivity
```

## How it works

Every switch is confirmed by short buzzes (60 ms each, 80 ms apart), so you can tell the profile
without looking: **1 buzz** = Normal, **2 buzzes** = Vibration, **3 buzzes** = Do Not Disturb.

| Profile        | What the app sets                                                    |
|----------------|----------------------------------------------------------------------|
| Normal         | Interruption filter `ALL`, ringer mode `NORMAL`                      |
| Vibration      | Interruption filter `ALL`, ringer mode `VIBRATE`                     |
| Do Not Disturb | Ringer mode `VIBRATE`, interruption filter `PRIORITY` with the policy below. Requires Notification Policy Access |

## What gets through in Do Not Disturb

| Source                                         | In DND                                                     |
|------------------------------------------------|------------------------------------------------------------|
| Calls from **starred contacts**                | ✅ Vibrate (the ringer is on vibrate, so they never ring)   |
| Alarms and timers                              | ✅ Ring as usual                                            |
| Media (music, videos)                          | ✅ Plays                                                    |
| Apps you allowed to interrupt DND (see below)  | ✅ Vibrate                                                  |
| Profile Switcher's own 3-buzz confirmation     | ✅                                                          |
| Calls from anyone else, incl. repeat callers   | ❌ Silent                                                   |
| Messages — **even from starred contacts**      | ❌ Silent                                                   |
| All other notifications                        | ❌ Silent — still shown in the notification shade           |

### Letting critical apps through

The app can't choose which other apps may interrupt DND — that's an Android setting. Apps you allow
there keep working with Profile Switcher's DND and their notifications vibrate:

- **Android 14 and older:** Settings → Notifications → Do Not Disturb → Apps → add the app.
- **Android 15+:** Settings → Modes — Profile Switcher's DND may be listed as its own mode where you
  can add apps.

Menu names can differ slightly between phone makers.

### Changing the rules

The DND policy is defined in `SoundProfileManager.createDndPolicy()`. For example, to let messages
from starred contacts vibrate too, add `NotificationManager.Policy.PRIORITY_CATEGORY_MESSAGES` to the
allowed categories (message senders are already limited to starred contacts).

## Project structure

The whole app is three classes in `app/src/main/java/com/github/r4d01/profileswitcher/`:

- `ProfileSwitchActivity` — invisible launcher activity: switches, vibrates, shows a toast and finishes; ignores presses while the phone is locked
- `manager/SoundProfileManager` — reads and sets the profile via `AudioManager` and `NotificationManager`
- `model/SoundProfile` — profile enum and cycle order

## Known limitations

- **Only works while unlocked:** presses on the lock screen or with the screen off are ignored. This
  is deliberate: once the phone is asleep, Motorola holds AI key presses until the phone wakes up (for
  every app), so they would otherwise switch the profile later, at an unexpected moment.
- **DND settings on Android 12–14:** switching to DND replaces the system DND rules (what is allowed
  through) with the ones above. On Android 15+ the app's DND is separate and your own DND settings
  are left untouched.
- **DND enabled elsewhere (Android 15+):** apps targeting Android 15+ control DND through their own
  rule, so if DND was turned on from system Quick Settings or another app, Profile Switcher may not
  be able to turn it off. DND turned on by Profile Switcher itself switches off normally.

## Credits

Code written with [Claude Code](https://claude.com/claude-code) (Anthropic).

## License

[MIT](LICENSE)
