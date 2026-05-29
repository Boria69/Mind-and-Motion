# Mind & Motion — Ghid de setup pas cu pas

Ghid pentru a porni de la repo gol până la „aplicația rulează pe emulator". Scris pentru cineva care **nu a deschis niciodată Android Studio**. Comenzile de terminal sunt date și pentru Windows, și pentru macOS/Linux.

---

## Pasul 1 — Instalează Android Studio

1. Mergi pe **https://developer.android.com/studio** și descarcă **latest stable** (la momentul scrierii e seria **„Panda" / 2025.3.x**). E un download mare (~1 GB) și ocupă mulți GB după instalare.
2. Rulează installerul. La primul start alege **Standard** setup — instalează automat: Android SDK, un emulator și platform-tools.
3. Lasă să descarce componentele (poate dura). Când vezi ecranul „Welcome to Android Studio", ești gata.

> Cerințe rezonabile: 16 GB RAM ideal (8 GB merge, dar greu), ~10–15 GB liber pe disc. Pe Windows, dacă emulatorul e lent, activează virtualizarea (VT-x/Hyper-V) din BIOS.

## Pasul 2 — Instalează Git și configurează-ți identitatea

1. Dacă nu ai Git: **https://git-scm.com/downloads**. Pe Windows, „Git for Windows" include și „Git Bash" (un terminal bun).
2. Configurează **o singură dată** identitatea (foarte important pentru contribuțiile pe GitHub — vezi `TASKS.md`):
   ```bash
   git config --global user.name "Numele Tău"
   git config --global user.email "email-ul-tau-de-github@example.com"
   ```
   Colegul face același lucru pe laptopul lui, cu datele LUI.

## Pasul 3 — Instalează Claude Code

Metoda recomandată e installerul nativ (fără Node.js).

- **macOS / Linux** (în terminal):
  ```bash
  curl -fsSL https://claude.ai/install.sh | bash
  ```
- **Windows** (în **PowerShell**, nu CMD):
  ```powershell
  irm https://claude.ai/install.ps1 | iex
  ```
  Pe Windows, Claude Code merge cel mai bine prin **WSL** sau **Git Bash**; dacă PowerShell face figuri, folosește Git Bash.

Alternativă (dacă ai deja Node.js 18+): `npm install -g @anthropic-ai/claude-code` — **fără `sudo`**.

Verifică și autentifică-te:
```bash
claude --version      # trebuie să afișeze o versiune
claude                # prima rulare te duce printr-un login (contul tău Claude Pro/Max)
claude doctor         # verifică dacă totul e ok
```

## Pasul 4 — Clonează repo-ul gol

Alege un folder de lucru (ex. `Documents`) și clonează:
```bash
cd ~/Documents        # pe Windows: cd %USERPROFILE%\Documents
git clone https://github.com/USER/REPO.git mind-and-motion
cd mind-and-motion
```
Acum ai folderul cu `README.md` și `.git` în el.

## Pasul 5 — Creează proiectul Android **în** folderul repo-ului  *(ticket MM-01)*

1. Deschide Android Studio → **New Project**.
2. Alege template-ul **Empty Activity** (cel cu logo Compose — NU „Empty Views Activity").
3. Completează:
   - **Name:** `Mind and Motion`
   - **Package name:** `com.mindandmotion.app`
   - **Save location:** alege **exact folderul `mind-and-motion`** clonat la Pasul 4.
   - **Minimum SDK:** **API 24 (Android 7.0)**
   - **Build configuration language:** Kotlin DSL (`build.gradle.kts`) — implicit.
4. **Finish.** Android Studio generează proiectul peste folderul existent (păstrează `README.md` și `.git`). Va rula primul **Gradle Sync** automat — durează la prima rulare (descarcă Gradle + dependențe). Așteaptă să apară „BUILD SUCCESSFUL" jos.

> Dacă Android Studio refuză pentru că folderul „nu e gol": creează proiectul într-un folder nou alăturat, apoi mută folderul ascuns `.git` și `README.md` din clona ta în folderul proiectului. Sau cere-i lui Claude Code (Pasul 7) „leagă acest proiect de remote-ul GitHub existent".

## Pasul 6 — Adaugă dependențele (Room, Navigation, WorkManager, DataStore)

Deschide `gradle/libs.versions.toml` (în Android Studio: panoul din stânga → `Gradle Scripts`). Wizard-ul a pus deja `kotlin`, `agp`, `compose-bom`, `activity-compose`, `lifecycle`. **Adaugă** intrările noastre:

```toml
[versions]
# ... (lasă ce există deja)
roomVersion = "2.7.2"
navigationCompose = "2.9.5"
workManager = "2.10.1"
datastore = "1.1.1"
lifecycleVmCompose = "2.9.4"
# KSP TREBUIE să se potrivească cu versiunea de Kotlin de mai sus (format: <kotlin>-<ksp>)
ksp = "2.1.20-2.0.0"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "roomVersion" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "roomVersion" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "roomVersion" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
androidx-datastore-prefs = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleVmCompose" }

[plugins]
# ... (lasă ce există)
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

În `app/build.gradle.kts`, la `plugins { }` adaugă `alias(libs.plugins.ksp)`, iar la `dependencies { }`:
```kotlin
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
implementation(libs.androidx.navigation.compose)
implementation(libs.androidx.work.runtime)
implementation(libs.androidx.datastore.prefs)
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation("androidx.compose.material:material-icons-extended")
```
Apasă **Sync Now** (bara galbenă de sus).

> ⚠️ **Cel mai probabil punct de eroare = versiunea KSP.** KSP trebuie să corespundă EXACT versiunii de Kotlin pe care a pus-o wizard-ul (cheia `kotlin` din `libs.versions.toml`). Dacă sync-ul dă eroare la KSP sau Room, **nu pierde timp manual** — vezi Pasul 7, Claude Code o rezolvă în câteva secunde. La fel, dacă Android Studio subliniază o versiune cu galben („newer version available"), poți da click → update.

## Pasul 7 — Pornește Claude Code în proiect

În terminalul Android Studio (jos, tab-ul **Terminal**) sau în Git Bash, **din folderul proiectului**:
```bash
claude
```
Acum Claude Code „vede" tot proiectul. Comenzi utile de pornire:
- `„Citește docs/ARCHITECTURE.md și docs/TASKS.md ca să înțelegi proiectul."`
- `„Sync-ul eșuează cu următoarea eroare: <copiezi eroarea>. Repară versiunile în libs.versions.toml."`
- `„Implementează ticketul MM-02: AppDatabase gol + AppContainer + clasa Application, conform arhitecturii."`

> Lucrează **ticket cu ticket** (vezi `TASKS.md`), câte un branch per ticket. După fiecare ticket: rulezi aplicația, verifici, faci commit + push, deschizi PR.

## Pasul 8 — Pornește un emulator și rulează aplicația

1. Sus, lângă butonul verde ▶ **Run**, deschide **Device Manager** → **Create Virtual Device** → alege un telefon (ex. *Pixel 7*) → o imagine de sistem (descarcă un API recent, ex. **34/35**) → Finish.
2. Apasă ▶ **Run 'app'**. Prima pornire a emulatorului durează. Ar trebui să vezi aplicația (deocamdată ecranul implicit din template).

## Pasul 9 — Primul commit pe branch + push

```bash
git checkout -b chore/project-setup
git add .
git commit -m "chore: bootstrap Android Compose project + docs"
git push -u origin chore/project-setup
```
Apoi pe GitHub deschizi un Pull Request → colegul îl revizuiește → merge în `main`. De aici încolo, fiecare își ia ticketele din `TASKS.md`.

---

## Mini-checklist „suntem gata de lucru"
- [ ] Android Studio instalat, „BUILD SUCCESSFUL" la sync
- [ ] Git configurat cu identitatea proprie (fiecare coleg)
- [ ] `claude --version` și `claude doctor` ok
- [ ] Proiect creat în folderul repo, dependențe adăugate, sync ok
- [ ] Emulator pornește și aplicația rulează
- [ ] Primul branch împins + PR deschis

## Probleme frecvente
- **Sync eșuează la KSP/Room** → versiune KSP nepotrivită cu Kotlin. Dă eroarea lui Claude Code.
- **`claude: command not found`** (Windows) → folosește Git Bash sau WSL; sau adaugă Claude în PATH (vezi mesajul de la `claude doctor`).
- **Emulator foarte lent** → activează virtualizarea în BIOS, sau testează pe un telefon real cu „USB debugging" pornit.
- **Gradle „stuck"** → ai nevoie de internet stabil la primul sync; lasă-l să termine, nu-l opri.
