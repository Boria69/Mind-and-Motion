# Mind & Motion — Arhitectură & Structură proiect

Aplicație Android de productivitate care reunește trei module: **Task Manager**, **Daily Journal** și **Pomodoro Timer**. Documentul descrie stack-ul tehnic, arhitectura, structura pe pachete și modelul de date. Este referința comună a echipei — orice decizie de implementare ar trebui să respecte ce e descris aici.

## Stack tehnic

| Strat | Tehnologie |
|---|---|
| Limbaj | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Navigație | Navigation Compose (single-Activity) |
| Arhitectură | MVVM (UI → ViewModel → Repository → Room) |
| Persistență | Room (SQLite) |
| Asincron | Kotlin Coroutines + `Flow` / `StateFlow` |
| Background (timer) | Foreground Service + Notifications (WorkManager pentru task-uri programate) |
| Preferințe | DataStore (Preferences) |
| Dependency Injection | Manual (un `AppContainer` ținut de clasa `Application`) |
| minSdk / targetSdk | 24 / cel mai recent stabil |

> **De ce DI manual și nu Hilt?** Pentru un proiect cu trei module, un container manual e mai ușor de înțeles, nu adaugă plugin-uri suplimentare și evită problemele de compatibilitate KSP/Kotlin care apar des la începători. Hilt rămâne un upgrade opțional dacă timpul permite.

> **De ce Foreground Service și nu doar WorkManager pentru cronometru?** Un Pomodoro trebuie să numere secundă cu secundă și să rămână activ când aplicația e în background sau ecranul e stins. Asta cere un *foreground service* cu notificare persistentă. WorkManager e potrivit pentru acțiuni programate / amânate (ex. un reminder), nu pentru un timer „live".

## Arhitectură pe straturi (MVVM)

```
┌─────────────────────────────────────────────┐
│  UI (Compose)  — Screens + componente         │  observă stare, trimite evenimente
├─────────────────────────────────────────────┤
│  ViewModel     — expune StateFlow<UiState>    │  logică de prezentare
├─────────────────────────────────────────────┤
│  Repository    — sursă unică de adevăr        │  combină DAO + alte surse
├─────────────────────────────────────────────┤
│  Room DAO / Database  +  DataStore  +  Service│  persistență & background
└─────────────────────────────────────────────┘
```

Reguli:
- UI-ul **nu** accesează niciodată direct Room sau Repository; doar ViewModel-ul.
- ViewModel-ul expune un singur `StateFlow<XxxUiState>` și funcții pentru evenimente (`onAddTask`, `onDelete` etc.).
- Repository-urile întorc `Flow` din DAO, fără a ști nimic despre UI.
- Operațiile de scriere rulează în coroutine (`viewModelScope`), niciodată pe main thread.

## Structura pe pachete (single-module, *package-by-feature*)

```
com.mindandmotion.app
├─ MindAndMotionApp.kt          // Application: creează AppContainer
├─ MainActivity.kt              // setContent { AppNavHost() }
│
├─ di/
│   └─ AppContainer.kt          // instanțiază Database + Repositories
│
├─ data/
│   ├─ AppDatabase.kt           // @Database, expune DAO-urile
│   ├─ Converters.kt            // TypeConverters (LocalDate, enum-uri)
│   ├─ task/
│   │   ├─ TaskEntity.kt
│   │   ├─ TaskDao.kt
│   │   └─ TaskRepository.kt
│   └─ journal/
│       ├─ JournalEntryEntity.kt
│       ├─ JournalDao.kt
│       └─ JournalRepository.kt
│
├─ ui/
│   ├─ navigation/
│   │   ├─ AppNavHost.kt        // NavHost + rute
│   │   ├─ Destinations.kt      // obiecte/rute type-safe
│   │   └─ BottomBar.kt         // BottomNavigation cu 3 (+1) tab-uri
│   ├─ theme/
│   │   ├─ Color.kt
│   │   ├─ Type.kt
│   │   └─ Theme.kt             // MindAndMotionTheme (light/dark)
│   ├─ components/              // composables reutilizabile
│   │   ├─ AppTopBar.kt
│   │   ├─ SectionCard.kt
│   │   ├─ EmptyState.kt
│   │   └─ ConfirmDialog.kt
│   ├─ tasks/
│   │   ├─ TaskListScreen.kt
│   │   ├─ TaskEditScreen.kt
│   │   └─ TaskViewModel.kt
│   ├─ journal/
│   │   ├─ JournalCalendarScreen.kt
│   │   ├─ JournalEntryScreen.kt
│   │   └─ JournalViewModel.kt
│   ├─ pomodoro/
│   │   ├─ PomodoroScreen.kt
│   │   └─ PomodoroViewModel.kt
│   └─ settings/
│       ├─ SettingsScreen.kt
│       └─ AboutScreen.kt
│
├─ pomodoro/                    // partea de background, separat de UI
│   ├─ TimerEngine.kt           // state machine: work/break, tick, pauză
│   ├─ PomodoroService.kt       // ForegroundService care rulează TimerEngine
│   └─ PomodoroNotifications.kt // canale + notificarea persistentă
│
└─ util/
    ├─ DateUtils.kt
    └─ Prefs.kt                 // wrapper DataStore (durate, temă)
```

## Navigație

Single-Activity. `BottomBar` cu patru destinații: **Tasks**, **Journal**, **Pomodoro**, **Settings**. Ecranele de editare (`TaskEditScreen`, `JournalEntryScreen`) sunt rute „push" peste tab-ul respectiv, primesc un argument opțional `id` (null = creare, non-null = editare).

```
Tasks  ──► TaskEdit?id={id}
Journal ─► JournalEntry?date={date}
Pomodoro
Settings ─► About
```

## Modelul de date (Room)

### `TaskEntity` (tabela `tasks`)
| Câmp | Tip | Note |
|---|---|---|
| `id` | `Long` (PK, autoGenerate) | |
| `title` | `String` | obligatoriu |
| `description` | `String?` | opțional |
| `priority` | `Priority` enum (HIGH/MEDIUM/LOW) | folosit la sortare |
| `dueDate` | `LocalDate?` | opțional |
| `isDone` | `Boolean` | default false |
| `createdAt` | `Long` (epoch millis) | |

Sortare implicită în DAO: `ORDER BY isDone ASC, priority ASC, dueDate ASC` (HIGH=0, MEDIUM=1, LOW=2).

### `JournalEntryEntity` (tabela `journal_entries`)
| Câmp | Tip | Note |
|---|---|---|
| `id` | `Long` (PK, autoGenerate) | |
| `date` | `LocalDate` | indexat |
| `mood` | `Mood?` enum (GREAT/GOOD/OKAY/BAD) | opțional |
| `content` | `String` | textul reflecției |
| `updatedAt` | `Long` | |

`AppDatabase` expune `taskDao()` și `journalDao()`. `Converters` mapează `LocalDate ↔ String (ISO)` și enum-urile ↔ `String`.

### Preferințe (DataStore, nu Room)
`workMinutes` (default 25), `breakMinutes` (default 5), `longBreakMinutes` (15), `theme` (system/light/dark).

## Funcționalități per modul (scop fix — nu mai mult)

**Task Manager**
- Listă sortată după prioritate + dată scadentă, cu bifă „done".
- Adăugare / editare / ștergere (CRUD complet).
- Selector de prioritate și date picker pentru deadline.
- Stare goală prietenoasă când nu există task-uri.

**Daily Journal**
- Vedere calendar lunar; zilele cu intrări sunt marcate.
- Tap pe o zi → vezi/editezi intrarea acelei zile.
- O intrare pe zi: text + mood opțional. CRUD complet.

**Pomodoro Timer**
- Cicluri work/break configurabile (din Settings).
- Start / pauză / reset; progres circular.
- Rulează în background prin foreground service + notificare persistentă.
- Notificare la finalul fiecărei faze (work terminat → break, etc.).

## Convenții de cod
- Un fișier = o responsabilitate; nume în engleză.
- ViewModel-urile nu importă nimic din `ui` (fără referințe la Compose/Context, exceptând `AndroidViewModel` unde e strict necesar).
- Fără logică în composables în afară de stare locală de UI; restul în ViewModel.
- String-urile vizibile mergem în `res/values/strings.xml` (ușurează un eventual bonus de localizare).
