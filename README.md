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
- **Designs / templates** — Classic, **Gold**, **Gold Plate** (premium
  thick gold rim), **Royal Purple**, **Diamond**, Neon, Candy and Forest.
  Each template recolours the rim, hub, pointer, background and all
  slices at once. Add more templates in `Models.kt` (`TEMPLATES`).

## v1.1 — premium update

- **Dark purple UI throughout** — dark purple backgrounds (never plain
  black), white text by default, gold accents, app name "FSD SPIN" and
  "by FSD ETC" shown clearly on the wheel screen. The wheel is centred
  vertically between the header and the bottom controls.
- **Customisation in Settings** — background colour, text colour, plate
  (rim) colour (or "Use template rim instead") and wheel design.
- **Haptics** — one vibration on every tap on the wheel.
- **Opening animation** — on every launch, "Made by FSD ETC" animates in
  with a vibration, then the app opens.
- **First-launch survey (once only)** — where you found the app
  (YouTube, Discord, AI, Instagram, Friend, Other) and birth year only.
  Continue → spinning loading animation → popup with a vibration:
  setup complete, app ready to use. Answers are stored on the device
  only (SharedPreferences) — this is an offline app, nothing is sent
  anywhere.
- **Settings page** — About FSD ETC section (from fsdetc.web.app) and
  "Made by FSD ETC" at the very bottom.
- **Unity Ads** — free app, with ads. Unity Ads SDK is integrated
  (`Ads.kt`) with the live Unity dashboard setup (project "FSD SPIN",
  Android Game ID 800410936, Interstitial placement
  BP_Interstitial_Android, general audience). See v1.2 below for how
  ads are shown.

## v1.2 — live Unity Ads on the SPIN button

- Tapping the **SPIN button** first shows an interstitial ad; the wheel
  spins as soon as the ad closes. If no ad is loaded, the spin starts
  at once — a spin is never blocked by an ad. Tapping the wheel itself
  spins directly, without an ad.
- The old "interstitial every 3rd spin after dismissing the result"
  behaviour was removed, so ads never show twice for one spin.
- Test mode is OFF, so real ads are served.

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

## App logo (official)

The official app logo is the gold wheel on a dark purple background,
supplied by FSD ETC. It is the launcher icon:

- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`

(512×512 PNGs converted from the supplied artwork. The old placeholder
vector wheel and adaptive-icon XMLs were removed, so the logo shows on
all supported Android versions.)

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
