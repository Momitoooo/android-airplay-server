# TV build (Momo's fork)

- `airplay.keystore`: fixed signing key for the TV build, so every new version installs over the last one.
  Never change or delete it. Password `android`, alias `airplay` (personal-use key, same rule as Momo's other TV apps).
- The workflow `.github/workflows/tv-build.yml` builds a release APK for the TCL TV (32-bit armeabi-v7a only),
  with the app id `com.homehub.airplay` and the name "AirPlay", so it installs next to the
  F-Droid AirPlay Server. It changes those lines on the build machine only; upstream files stay untouched,
  which keeps "Sync fork" simple.

## TV redesign (Momo), files changed from upstream

- 3a: `ui/TvReady.kt` (new: "Listo para AirPlay" screen, shared TV button/chip/colours), `ui/MainScreen.kt`
  (3 small hooks, marked "Momo's TV redesign"), `res/drawable/banner.xml` (Favoritos banner),
  `res/drawable/tv_ic_*.xml` (new glyphs). When syncing the fork, re-check those hooks.
