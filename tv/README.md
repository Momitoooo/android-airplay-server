# TV build (Momo's fork)

- `airplay.keystore`: fixed signing key for the TV build, so every new version installs over the last one.
  Never change or delete it. Password `android`, alias `airplay` (personal-use key, same rule as Momo's other TV apps).
- The workflow `.github/workflows/tv-build.yml` builds a release APK for the TCL TV (32-bit armeabi-v7a only),
  with the app id `com.homehub.airplay` and the temporary name "AirPlay (nuevo)", so it installs next to the
  F-Droid AirPlay Server. It changes those lines on the build machine only; upstream files stay untouched,
  which keeps "Sync fork" simple.
