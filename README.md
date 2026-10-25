# FSD SPIN — Native Android Spinning Wheel App

A fully customisable spinning-wheel app, built in Kotlin with Jetpack Compose.
No web view, no hybrid wrapper — a real native Android app.

## What it does

- **Spin the wheel** — tap SPIN or tap the wheel itself. The wheel spins
  ~6 seconds with a slow-down finish and lands exactly on the winner slice.
- **Spin sound** — a tick sound as the pointer passes each slice, plus a
  win jingle when the result lands. Toggle it in Settings. The sounds are
  `app/src/main/res/raw/tick.wav` and `win.wav` — swap those files to
  change the sounds without touching code.
- **Colours** — every slice has its own colour. In Edit, tap a slice's
  colour dot to cycle it through the palette.
- **Fully customisable** — Edit screen: add, rename, recolour and delete
  entries (minimum 2 to spin). Settings screen: sound, hiding and modes.
- **Hide buttons** — "Hide buttons while spinning" hides all controls
  during a spin. "Hide all controls" shows only the wheel; tap the wheel
  to spin, and "Show controls" brings everything back.
- **Host control — force winner (optional)** — in Settings you can pick
  one entry that the wheel will always land on. It is labelled in the app
  as a host control. Leave it on "Random (no force)" for a fair spin.
- **Elimination mode** — after each spin the winner is removed, so the
  next spin picks someone else. With 10 names: spin 1 removes one name,
  spin 2 removes the next, and so on.
- **Designs / templates** — Classic, **Gold**, **Diamond**, Neon, Candy
  and Forest. Each template recolours the rim, hub, pointer, background
  and all slices at once. Add more templates in `Models.kt` (`TEMPLATES`).

## Build it

### Option A — Android Studio (recommended)
1. Open this `fsd-spin-app` folder in Android Studio.
2. Let Gradle sync (it downloads the Android Gradle Plugin, Kotlin and
   Compose on first run — internet required).
3. Run on an emulator or a real phone, or Build → Build App Bundle(s) /
   APK(s) → Build APK(s).

### Option B — GitHub Actions (no Android Studio)
Push this folder to a GitHub repo (branch `main`). The workflow in
`.github/workflows/android.yml` builds a debug APK automatically.
Download it from that workflow run's **Artifacts** section and install
it on an Android phone.

## Replace the logo (when your design is ready)

Your own logo goes here, keeping the same file names:

- `app/src/main/res/drawable/ic_launcher_foreground.xml` — the wheel
  artwork (currently a placeholder wheel).
- `app/src/main/res/values/colors.xml` — `ic_launcher_background`
  (currently purple) is the icon background colour.

For a PNG logo instead of a vector: in Android Studio, right-click
`res` → New → Image Asset → Launcher Icons, and pick your PNG.

## Project layout

- `app/src/main/java/com/fsdspin/app/MainActivity.kt` — screens, spin
  logic, result overlay, settings wiring.
- `Wheel.kt` — the Canvas wheel: slices, text, rim, hub, pointer.
- `Models.kt` — entries, templates, spin maths (winner landing angle).
- `Screens.kt` — entry editor, template picker, settings rows, forced
  winner picker.
- `Sound.kt` — SoundPool tick + win sounds.

## Notes

- Package / app id: `com.fsdspin.app`, app name: **FSD SPIN**.
- Min SDK 26 (Android 8.0+), target SDK 34.
- This project was written on a machine without an Android SDK, so the
  APK has not been compiled here yet — the first build happens in
  Android Studio or via the GitHub Actions workflow above.
