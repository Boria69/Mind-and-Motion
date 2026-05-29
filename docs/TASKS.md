# Mind & Motion — Plan de task-uri & împărțire pe echipă

Două persoane. **Tu (notat `[TU]`) ≈ 60%**, **colegul (notat `[COLEG]`) ≈ 40%**. Împărțirea e pe **felii verticale complete** (fiecare deține module întregi, end-to-end), nu pe „unul scrie logica, altul face UI". Asta înseamnă fișiere distincte, conflicte minime pe Git și un istoric de commit-uri credibil și echilibrat pentru ambii.

**Cine ce deține:**
- **`[TU]`** — fundația tehnică (proiect, DB, navigație), modulul **Tasks** și modulul **Pomodoro** (cel mai greu, cu service în background). + integrarea finală.
- **`[COLEG]`** — tema vizuală + componentele comune, modulul **Journal** (cu calendar), ecranele **Settings/About** și câteva teste unitare.

Fiecare ID = un „ticket". Le bifați pe măsură ce le faceți. `Depinde de` arată ordinea.

---

## Epic 0 — Fundație (sprintul 0, se face PRIMUL)

| ID | Owner | Titlu | Depinde de | Branch |
|---|---|---|---|---|
| MM-01 | `[TU]` | Creează proiectul Android (Compose), version catalog, leagă-l de repo, push scaffold | — | `chore/project-setup` |
| MM-02 | `[TU]` | `AppDatabase` (Room) gol + `AppContainer` + `MindAndMotionApp` | MM-01 | `feat/db-foundation` |
| MM-03 | `[TU]` | Scaffold navigație: `AppNavHost`, `Destinations`, `BottomBar`, `MainActivity` | MM-01 | `feat/nav-shell` |
| MM-04 | `[COLEG]` | Temă Material 3: `Color.kt`, `Type.kt`, `Theme.kt` (light + dark) | MM-01 | `feat/theme` |
| MM-05 | `[COLEG]` | Componente comune: `AppTopBar`, `SectionCard`, `EmptyState`, `ConfirmDialog` | MM-04 | `feat/shared-components` |

> După MM-01 (când scaffold-ul e pe `main`), MM-02/03 și MM-04/05 pot merge **în paralel** — voi lucrați simultan din prima zi.

## Epic 1 — Task Manager  *(`[TU]`)*

| ID | Owner | Titlu | Depinde de | Branch |
|---|---|---|---|---|
| MM-10 | `[TU]` | `TaskEntity` + `Priority` enum + `TaskDao` (CRUD + query sortat) | MM-02 | `feat/task-data` |
| MM-11 | `[TU]` | `TaskRepository` + `TaskViewModel` (`StateFlow`, sortare) | MM-10 | `feat/task-vm` |
| MM-12 | `[TU]` | `TaskListScreen` (listă sortată, bifă done, swipe-to-delete) | MM-11, MM-05 | `feat/task-list` |
| MM-13 | `[TU]` | `TaskEditScreen` (form, selector prioritate, date picker) | MM-11 | `feat/task-edit` |

## Epic 2 — Daily Journal  *(`[COLEG]`)*

| ID | Owner | Titlu | Depinde de | Branch |
|---|---|---|---|---|
| MM-20 | `[COLEG]` | `JournalEntryEntity` + `Mood` enum + `JournalDao` + `Converters` (LocalDate) | MM-02 | `feat/journal-data` |
| MM-21 | `[COLEG]` | `JournalRepository` + `JournalViewModel` (intrări pe dată) | MM-20 | `feat/journal-vm` |
| MM-22 | `[COLEG]` | `JournalCalendarScreen` (grilă lunară, marchează zilele cu intrări) | MM-21, MM-05 | `feat/journal-calendar` |
| MM-23 | `[COLEG]` | `JournalEntryScreen` (text + mood, CRUD pe ziua selectată) | MM-21 | `feat/journal-entry` |

## Epic 3 — Pomodoro Timer  *(`[TU]` — partea cea mai tehnică)*

| ID | Owner | Titlu | Depinde de | Branch |
|---|---|---|---|---|
| MM-30 | `[TU]` | `TimerEngine` (state machine: work/break, tick, pauză, reset) | MM-03 | `feat/timer-engine` |
| MM-31 | `[TU]` | `PomodoroService` (foreground service) + `PomodoroNotifications` | MM-30 | `feat/timer-service` |
| MM-32 | `[TU]` | `PomodoroScreen` + `PomodoroViewModel` (progres circular, start/pauză/reset) | MM-31, MM-05 | `feat/timer-ui` |

## Epic 4 — Preferințe, finisaje, QA

| ID | Owner | Titlu | Depinde de | Branch |
|---|---|---|---|---|
| MM-40 | `[COLEG]` | `Prefs` (DataStore) + `SettingsScreen` (durate Pomodoro, temă) | MM-04 | `feat/settings` |
| MM-41 | `[COLEG]` | `AboutScreen` + icon aplicație + README final + screenshot-uri | MM-03 | `feat/about-readme` |
| MM-42 | `[COLEG]` | Teste unitare: `TaskRepository`/`JournalViewModel` (2-3 teste) | MM-11, MM-21 | `test/unit` |
| MM-43 | `[TU]` | Integrare finală: cablare toate ecranele în nav, smoke test pe device | toate | `chore/integration` |
| MM-44 | `[TU]` | Build de release (debug semnat), note pentru demo/prezentare | MM-43 | `chore/release` |

---

## Ordine recomandată (3 „sprinturi" scurte)

1. **Sprint 0 — Fundație:** MM-01 → apoi în paralel MM-02/03 (`[TU]`) și MM-04/05 (`[COLEG]`).
2. **Sprint 1 — Funcționalități:** `[TU]` face Epic 1 (Tasks); `[COLEG]` face Epic 2 (Journal). Complet independente.
3. **Sprint 2 — Greu + finisaj:** `[TU]` face Epic 3 (Pomodoro) + MM-43/44; `[COLEG]` face Epic 4 (Settings, About, teste).

## Strategie Git — ca istoricul să arate ca muncă de echipă reală

Aici e esența cerinței „să nu pară pe GitHub că am lucrat doar eu":

1. **Fiecare commitează de pe contul lui.** Pe laptopul fiecăruia, configurați identitatea Git o singură dată:
   ```bash
   git config --global user.name "Numele Tău"
   git config --global user.email "email-ul-de-pe-github@example.com"
   ```
   Commit-urile lui Claude Code sunt atribuite automat persoanei care rulează Claude Code pe mașina ei. **Nu** commitați munca colegului de pe contul vostru — fiecare împinge propriile task-uri.
2. **Un branch per ticket** (vezi coloana `Branch`). Nimeni nu lucrează direct pe `main`.
3. **Pull Request pentru fiecare branch**, iar celălalt coleg îl revizuiește înainte de merge. Comentariile și approve-urile de la review apar și ele în activitatea GitHub a colegului — întăresc impresia de colaborare.
4. **Commit-uri mici și dese**, cu mesaje clare (`feat: add TaskDao with sorted query`). E mai bine decât un singur commit uriaș.
5. Pentru că fiecare deține fișiere diferite (felii verticale), **conflictele de merge sunt aproape inexistente**.

Rezultat: chiar dacă efortul e 60/40, colegul are module întregi pe numele lui (tot Journal-ul, tema, Settings, About, teste) → un grafic de contribuții vizibil și echilibrat pentru ambii.

## Definiția lui „gata" (pentru orice ticket)
- Compilează fără warning-uri noi.
- Funcționează pe emulator/device.
- Respectă arhitectura din `ARCHITECTURE.md` (UI → ViewModel → Repository → Room).
- E pe branch-ul lui, cu PR deschis și revizuit.
