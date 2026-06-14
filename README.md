# Time Awareness App

A Kotlin + Jetpack Compose Android app that trains your internal sense of time by challenging you to press a button on each hour.

## How It Works

- Your daily accuracy score starts at **100**.
- Press **[ MARK HOUR ]** when you think the hour has just ticked.
- The app calculates the deviation in minutes between your press and the nearest hour.
- Each minute of deviation is deducted from your score.
- A perfect day = pressing exactly on the hour every time = **100/100**.
- Export your session log as a structured XML file.

## Architecture

Strict **MVI (Model-View-Intent)** with Jetpack Compose and `StateFlow`:

```
app/src/main/java/com/kamenkarchev/timeawareness/
├── intent/
│   └── TimeIntent.kt         # Sealed class: LogCurrentTime | ExportData
├── model/
│   ├── TimeState.kt          # Single source of truth for UI
│   └── TimeLog.kt            # Per-press data: expected, actual, deviation
├── data/
│   └── XmlRepository.kt      # Serializes state to XML via android.util.Xml
├── viewmodel/
│   └── TimeViewModel.kt      # Processes intents, updates StateFlow<TimeState>
├── ui/
│   ├── theme/
│   │   └── Theme.kt          # Terminal aesthetic: dark bg, neon green/amber/red
│   └── screens/
│       └── TimeAwarenessScreen.kt  # Stateless Composables, state-hoisted
└── MainActivity.kt
```

## Tech Stack

- Kotlin + Coroutines + `StateFlow`
- Jetpack Compose (no XML layouts)
- `android.util.Xml` serializer (no external XML libraries)
- `ViewModelProvider.Factory` (no Hilt)

## Exported XML Format

```xml
<?xml version="1.0" encoding="UTF-8"?>
<TimeAwarenessData>
  <Score>87</Score>
  <Log expected="2026-06-13T11:00" actual="2026-06-13T11:02:14" deviationMinutes="2"/>
  <Log expected="2026-06-13T12:00" actual="2026-06-13T12:00:05" deviationMinutes="0"/>
</TimeAwarenessData>
```

Exported to: `Android/data/com.kamenkarchev.timeawareness/files/time_accuracy_data.xml`
