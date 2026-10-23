# ShuttlePulse 🏸⚡

<p align="center">
  <img src="app/src/main/res/drawable/img_hero_court.xml" alt="ShuttlePulse Court Banner" width="100%" />
</p>

<p align="center">
  <strong>Production-Grade Android Plyometrics & Agility Conditioning App Engineered for Elite Badminton Athletes</strong>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=for-the-badge&logo=android&logoColor=white" alt="Compose" /></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Target%20SDK-35%20%2F%20Android%2015-3DDC84.svg?style=for-the-badge&logo=android&logoColor=white" alt="Android 15" /></a>
  <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Room%20DB-Persistence-FF6F00.svg?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room" /></a>
  <a href="#zero-latency-audio-engine"><img src="https://img.shields.io/badge/Audio%20Engine-880Hz%20Fast%20Mixer-D4FF00.svg?style=for-the-badge&logo=soundcharts&logoColor=black" alt="Audio Engine" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-00F0FF.svg?style=for-the-badge" alt="License" /></a>
</p>

---

## ⚡ Executive Summary

**ShuttlePulse** is a specialized, sports-science driven Android training system designed specifically for the unique physical demands of competitive badminton. Unlike generic interval timers or running metronomes, ShuttlePulse targets **Achilles tendon elasticity**, **Stretch-Shortening Cycle (SSC) ankle stiffness**, and **Chinese National Team fast-feet ready stance mechanics** (240+ BPM cadence).

It combines:
- **Zero-latency programmatic PCM audio synthesis** bypassing Android's normal audio mixer queues.
- **Micro-phase monotonic timeline scheduling** preventing timer drift during explosive drills.
- **Dual real-time biomechanical canvas visualizers** rendering both front-on athlete kinematics and top-down BWF tactical court footwork in synchronization with the cadence tempo.
- **Asymmetric lead-foot conditioning** dynamically adapting to the athlete's dominant racket hand (`RIGHT` / `LEFT`).

---

## 🔬 The 4 Badminton Drill Archetypes

Every drill in ShuttlePulse is modeled on one of four distinct neuromuscular mechanics:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SHUTTLEPULSE ARCHETYPES                         │
├──────────────────┬──────────────────┬──────────────────┬───────────────┤
│ 1. RHYTHMIC      │ 2. COMBO         │ 3. REACTIVE      │ 4. UNILATERAL │
│    CADENCE       │    CYCLE         │    BURST         │    POWER      │
├──────────────────┼──────────────────┼──────────────────┼───────────────┤
│ Continuous tempo │ N-base bounces + │ Fast-feet base + │ Asymmetric    │
│ (150-240 BPM)    │ 1 maximal power  │ randomized cues  │ racket-foot   │
│ for SSC tendon   │ explosion on the │ (split-step, net │ lead with 50% │
│ elasticity       │ (N+1) downbeat   │ lunge, sprawl)   │ auto-switch   │
└──────────────────┴──────────────────┴──────────────────┴───────────────┘
```

### 1. `RHYTHMIC_CADENCE` (Continuous SSC Tempo)
* **Neuromuscular Objective:** Trains reactive ground contact times (<150ms) and short-latency stretch reflexes through double-leg and single-leg pogo hops at 150–240 BPM.
* **Engine Execution:** Dispatches a crisp 880 Hz tick every `60_000L / effectiveBpm` ms. Every 4th beat triggers a higher-pitched 1320 Hz accented downbeat.

### 2. `COMBO_CYCLE` (Deterministic Micro-Phase Rhythm)
* **Neuromuscular Objective:** Combines reactive sub-maximal bounces with immediate maximal rate of force development (RFD) (e.g., *"4 Pogo Hops + 1 Max Tuck Jump"*, *"2 Bounce Steps + 1 Rear-Court Scissor Kick"*).
* **Engine Execution:** Tracks rep counts synchronously with the BPM beat (`"1" -> "2" -> "3" -> "4" -> "EXPLODE!"`). On the final beat, triggers a high-frequency dual-harmonic whistle (1760 Hz + 2640 Hz) and crimson HUD burst flash.

### 3. `BASE_PLUS_REACTIVE_BURST` (Fast-Feet Base + Random Action Trigger)
* **Neuromuscular Objective:** Simulates live match play where an athlete sustains rapid toe-tapping while reading and reacting instantaneously to unpredictable shuttle trajectories.
* **Engine Execution:** Drives rapid 240 BPM ticks while scheduling pseudo-random explosive triggers every `2500ms–4500ms`. Supported triggers:
  - `"SPLIT STEP!"` · `"RACKET LUNGE!"` · `"SCISSOR SWITCH!"` · `"HIGH JUMP!"` · `"SPRAWL / DIVE RECOVERY!"`

### 4. `UNILATERAL_POWER` (Asymmetric Lead-Foot Conditioning)
* **Neuromuscular Objective:** Balances badminton's extreme structural asymmetry (dominant lunging limb vs. non-dominant push-off limb).
* **Engine Execution:** Queries `DominantHand` (`RIGHT` / `LEFT`) from Jetpack DataStore to cue `"RACKET FOOT FORWARD (RIGHT)"` vs. `"NON-RACKET FOOT FORWARD"`. At the exact 50% midpoint of the set, an audio chime and visual banner cue `"SWITCH SIDES!"`.

---

## 🏃 Pre-Built Pro Conditioning Programs

ShuttlePulse includes 5 pre-built, science-backed workout circuits calibrated for match preparation, recovery, and explosive power development:

| Program | Duration | Sets | Primary Training Focus | Key Drills Included |
|---|---|---|---|---|
| **Chinese Fast-Feet Primer** | **4 min** (240s) | 8 sets | High-frequency CNS activation & pre-match footwork wakeup | 240 BPM Wide Tapping, Fast-Feet Split Step, Scissor Taps |
| **Ankle Stiffness SSC Express** | **8 min** (480s) | 12 sets | Achilles tendon reactive elasticity & ground contact stiffness | Double-Leg Pogos, Single-Leg Line Hops, Split-Stance Pogos |
| **Pre-Court CNS Activation** | **15 min** (900s) | 18 sets | Complete pre-tournament neuromuscular preparation | Reactive Burst Taps, Lunge Recovery Hops, Scissor Jumps |
| **Chinese Footwork & Tapping Endurance** | **20 min** (1200s) | 22 sets | Lactate buffering & sustained fast-twitch foot speed | High-Volume Wide Tapping, Lateral China Bounds, In-Out Steps |
| **Tendon Stiffness & Plyometric Explosiveness** | **25 min** (1500s) | 26 sets | Maximal vertical impulse, decelerative braking, and hip rotation | Tuck Jump Combos, Deep Racket Lunges, Skater Deceleration Sticks |

---

## 🎨 Dual Real-Time Biomechanical Canvases

The Active Player renders two synchronized Jetpack Compose `Canvas` animations running in lockstep with the cadence phase:

```
                  ┌───────────────────────────────┐
                  │       ACTIVE PLAYER HUD       │
                  ├───────────────┬───────────────┤
                  │  STICK FIGURE │  TACTICAL BWF │
                  │  KINEMATICS   │  COURT VIEW   │
                  │               │               │
                  │     (o)       │     [===]     │
                  │    / | \      │   (L)   (R)   │
                  │     / \       │     \   /     │
                  │    /   \      │     COURT     │
                  └───────────────┴───────────────┘
```

1. **`StickFigureStanceCanvas` (Front-On Biomechanics):**
   - Renders anatomical stick figures against a badminton net backdrop with realistic joint kinematics for the **6 Core Biomechanical Stance Types**:
     1. `PARALLEL_POGO_BOUNCE`: Symmetric stiff-ankle bounce on balls of feet; heels elevated.
     2. `WIDE_FAST_TAPPING`: Low center-of-gravity wide badminton ready stance with rapid alternating toe taps.
     3. `STAGGERED_SCISSOR_STANCE`: Asymmetric fore-aft split stance priming for rear-court hip rotation.
     4. `DEEP_RACKET_LUNGE`: Extended forward brake on lead racket heel/midfoot with erect torso.
     5. `LATERAL_SKATER_BOUND`: China jump lateral interception with single-leg deceleration stick.
     6. `HIGH_TUCK_EXPLOSION`: Maximal vertical jump explosion with knees driven to chest.
   - Highlights the dominant racket hand with a glowing racket indicator.
2. **`FootStanceCanvas` (Tactical BWF Court Footwork):**
   - Top-down badminton court diagram showing left (`L`) and right (`R`) foot plates, ball-of-foot pressure vectors, and stance width dynamics.

---

## 🔊 Zero-Latency Audio Synthesis Engine

Standard Android audio frameworks introduce variable mixer latency that can ruin high-speed cadence drills (up to 40ms of jitter). ShuttlePulse solves this using a custom hybrid audio engine:

- **Programmatic PCM Tone Synthesis:** Generates audio waveforms in memory at 44.1 kHz, 16-bit PCM:
  - `880 Hz` Sine Wave (32ms, Hann window envelope) · Standard Metronome Tick
  - `1320 Hz` Sine Wave (38ms, Hann window envelope) · Downbeat Accent Beat
  - `1760 Hz + 2640 Hz` Dual-Harmonic Burst (140ms) · Combo Explosion & Whistle
  - `660 Hz` Countdown Tone (90ms) · 3-2-1 Prep Beeps
- **Fast Mixer Routing:** Static buffers pre-loaded into `AudioTrack` and `SoundPool` tagged with `AudioAttributes.USAGE_GAME` to guarantee direct hardware mixing (<10ms).
- **Dedicated Dispatcher Thread:** Runs on an isolated background thread (`ShuttlePulseAudioThread`) decoupling sound triggers from Jetpack Compose UI recomposition.
- **Audio Ducking:** Automatically ducks background Spotify / music tracks (`AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`) during active intervals.
- **Text-To-Speech (TTS):** Configured at 1.35x speech rate and 1.05x pitch for concise, urgent athletic callouts without stutter.

---

## 🏛️ Architecture & Package Blueprint

ShuttlePulse strictly follows MVVM and Clean Architecture:

```text
com.shuttlepulse.agility
├── ShuttlePulseApp.kt                  # Application entry point & Room pre-seeding
├── MainActivity.kt                     # Single Activity, Compose entry & edge-to-edge
├── di/
│   └── AppModule.kt                    # Dependency injection (Database, Audio, Repositories)
├── domain/
│   ├── model/
│   │   ├── DrillArchetype.kt           # Archetypes, exercise categories, DominantHand
│   │   ├── Exercise.kt                 # Exercise domain model & custom drill flags
│   │   ├── WorkoutCircuit.kt           # Pre-built & user circuit domain models
│   │   ├── WorkoutSegment.kt           # Micro-phase segments, cues & urgency
│   │   ├── StancePoseType.kt           # 6 Biomechanical stance poses
│   │   └── UserPreferences.kt          # DominantHand, ducking, TTS, BPM offset
│   ├── repository/
│   │   └── ShuttlePulseRepository.kt   # Repository contracts
│   └── engine/
│       └── WorkoutTimelineGenerator.kt # Deterministic micro-phase timeline compiler
├── data/
│   ├── local/
│   │   ├── ShuttlePulseDatabase.kt     # Room Database
│   │   ├── dao/
│   │   │   ├── ExerciseDao.kt          # Pre-seeded & custom drill queries
│   │   │   ├── CircuitDao.kt           # Circuit persistence
│   │   │   └── WorkoutLogDao.kt        # Completed workout telemetry logs
│   │   ├── entity/
│   │   │   ├── ExerciseEntity.kt
│   │   │   ├── CircuitEntity.kt
│   │   │   └── WorkoutLogEntity.kt
│   │   └── seed/
│   │       └── BadmintonSeedData.kt    # 16 authentic drills + 5 pre-built circuits
│   ├── preferences/
│   │   └── UserPreferencesRepository.kt# Jetpack DataStore preferences implementation
│   └── repository/
│       └── ShuttlePulseRepositoryImpl.kt
└── presentation/
    ├── theme/
    │   ├── Color.kt                    # High-contrast OLED sports color palette
    │   ├── Type.kt                     # Athletic typography (Teko, Space Grotesk, JetBrains Mono)
    │   └── Theme.kt                    # Material 3 dark OLED theme
    ├── navigation/
    │   └── ShuttlePulseNavHost.kt      # Type-safe Jetpack Navigation (5 tabs)
    ├── dashboard/
    │   ├── DashboardScreen.kt          # Hero card, duration filter chips, circuit list
    │   └── DashboardViewModel.kt
    ├── library/
    │   ├── DrillLibraryScreen.kt       # Drill catalog & custom drill creation
    │   └── LibraryViewModel.kt
    ├── builder/
    │   ├── CircuitBuilderScreen.kt     # Custom circuit composer & set configuration
    │   └── BuilderViewModel.kt
    ├── player/
    │   ├── ActivePlayerScreen.kt       # Real-time player HUD, transports, dual canvases
    │   ├── PlayerViewModel.kt
    │   ├── CadenceSoundEngine.kt       # Zero-latency PCM sound synthesizer
    │   ├── StickFigureStanceCanvas.kt  # Front-on biomechanical kinematics
    │   └── FootStanceCanvas.kt         # Top-down court footwork diagrams
    └── settings/
        ├── SettingsScreen.kt           # Dominant hand, BPM calibration, audio test bench
        └── SettingsViewModel.kt
```

---

## 🎨 Design System & Sports Science Palette

ShuttlePulse utilizes an ultra-high-contrast OLED sports palette optimized for bright gym and court lighting:

| Color Name | Hex Code | Purpose |
|---|---|---|
| **Court Obsidian** | `#0B0E14` | Deep, battery-saving true OLED background |
| **Volt Plyo Lime** | `#D4FF00` | High-energy primary accents, pogo rhythms, and downbeats |
| **Fast-Feet Cyan** | `#00F0FF` | Rapid cadence indicators, support leg bones, and timing rings |
| **Burst Crimson** | `#FF2A54` | Maximal power explosions, tuck jump callouts, and warning cues |
| **Slate Muted** | `#8E9AA8` | Secondary labels, coaching tips, and inactive tabs |

---

## 🛠️ Technology Stack

* **Language:** [Kotlin 2.0+](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
* **Concurrency:** Kotlin Coroutines & `StateFlow`
* **Local Persistence:** [Room](https://developer.android.com/training/data-storage/room) & [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
* **Audio Synthesis:** Hardware-accelerated `AudioTrack` + `SoundPool` (`USAGE_GAME`) + Android `TextToSpeech`
* **Graphics:** Hardware-accelerated Jetpack Compose `Canvas` with spring kinematics
* **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) with KSP

---

## 🚀 Getting Started & Build Instructions

### Prerequisites
* **Java Development Kit:** JDK 17 or JDK 21
* **Android Studio:** Ladybug (2024.2+), Koala, or Jellyfish
* **Android SDK:** Platform 35 / 36, Build-Tools 35.0.0+

### Cloning & Building Locally

```bash
# 1. Clone repository
git clone https://github.com/vishalvishal3736-sys/agility-app.git
cd agility-app

# 2. Build Debug APK
./gradlew assembleDebug

# 3. Run Unit & Biomechanics Tests
./gradlew test

# 4. Install onto connected Android device
./gradlew installDebug
```

---

## 🧪 Unit & Biomechanics Verification

The test suite validates timeline determinism, BPM intervals, and sports-science rules:

```bash
./gradlew testDebugUnitTest
```

Tested scenarios include:
- Verification of all 16 drills and 5 pre-built programs across the 4 archetypes.
- Verification of the 6 core biomechanical stance poses.
- Exact millisecond validation of 150 BPM (400ms) and 240 BPM (250ms) beat intervals.
- Unilateral 50% midpoint side-switching based on `DominantHand` (`RIGHT` vs `LEFT`).
- Deterministic phase-locking of combo cycle cues to BPM intervals.
- DataStore BPM calibration scaling and additive offset calculations (-20 to +20 BPM).

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
