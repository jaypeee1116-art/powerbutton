# Power Buttons (Android)

Two big buttons: **Power off** and **Restart**.

- No root: tapping a button opens Android's own power menu (via an accessibility service);
  you then tap Power off or Restart there.
- Rooted phone: the buttons power off / restart directly (after a confirmation).

## Build the app
1. Install Android Studio (developer.android.com/studio).
2. Open Android Studio > Open > choose this PowerButtonsApp folder. Wait for Gradle sync.
3. Turn on Developer options + USB debugging on your phone, plug it in, press Run.
   (Or Build > Build APK(s) and copy the APK to your phone to install it.)

## First use (no root)
Open the app > "Enable power menu access" > Installed apps > Power Buttons > turn on.
On Android 13+, if the switch is greyed out: phone Settings > Apps > Power Buttons >
three-dot menu > Allow restricted settings, then try again.
