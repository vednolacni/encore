# Encore: notes for AI assistants

## What Encore is

Encore is a music screen for the TV, for Home Assistant and Music Assistant, laid out as a player:
the album cover high on the left with the song under it, the song's words (or its title block)
beside it, a progress bar across the screen and, under the bar's right end, two buttons, one for
the lyrics and one for the settings. It began as a fork of Klangbild by hazymorning (MIT,
https://github.com/hazymorning/Klangbild), a calm now playing screen whose care for the picture
it keeps: type, inks, light, motion and centering measured on what is seen. Unlike Klangbild,
Encore is a player, so its buttons are part of the design and meet the same standard as
everything else. The next phase
gives it the look of a sing-along screen (larger, bolder words, the far lines blurred, the cover's
colors as the background by default).

## The project

Android TV app, sideload only, built for a 4K Android TV device with a weak GPU in front of an
OLED TV. The UI speaks English (default) and German (`values-de`); code, comments, the README,
the release notes, CI and every other document are English. The owner is vednolacni.

The README follows the layout of the owner's other projects (SeerrHA's README is the model) and
is written like everything else (see the writing rules). A centered header holds the wide logo
(`logo-wide.png`, or `logo-wide-dark.png` for a dark page), a tagline as `###`, reference-style
shields badges (Android TV 9+ linking to `#devices`, Home Assistant, Music Assistant, License), a
line on what it works with, the download button, a row of links to the sections (`&middot;`
between them) and the first picture. Then a rule (`---`), three short paragraphs on what Encore
does and where it comes from, `## Features` (one emoji opening each line), `## Requirements` (a
table, Requirement and Why), `## Install` (numbered steps, the note on the speaker listed twice as
GitHub's `> [!IMPORTANT]`, the optional automation), Day and night with its picture, Devices,
Remote, Sendspin, Privacy, Support, Credits (Klangbild by hazymorning, then the owner's own line
"Developed with the assistance of AI.") and License. No folds. The README's pictures are mockups
in `docs/screenshots/`: the app's own screens (`ReadmeShotsTest`, with the colors the lights behind
the TV would show) set in a room by `tools/make_readme_pictures.py`, made again when the look
changes, and the download button, which downloads the newest APK at once
(`releases/latest/download/encore.apk`, so every release names its APK `encore.apk`). The logo,
the first picture and the button come in a light and a dark version, switched by the reader's
theme (`<picture>` with `prefers-color-scheme`). No mockup carries text of its own. Issues are for
bugs: the issue form (`bug_report.yml`) says so kindly and sends ideas and questions to the
Discussions.

The logo is `tools/logo.svg`, the owner's own design, kept as given: an espresso brown TV
(#2B211A) with an antenna and five linen bars on its screen (#F7F2EA; three standing, two leaning
together), beside the name. The icons show the TV alone on a square of warm sand (#EDE5D9), the
banner the whole logo on that sand, and a dark page the TV and the name in linen with espresso bars.
`tools/make_launcher_assets.py` draws them from the SVG with CairoSVG (and through it
`tools/make_readme_pictures.py` the README's wide logo and the button), never scaled from a bitmap;
the notification icon (`ic_notification.xml`) is the same TV as a vector. Release notes are easy to scan and in everyday words: a small heading for each kind of
change there is (`### New`, `### Better`, `### Bug fixes`), under it a few short points a user
understands at once, nothing technical, never speaking to the reader and only claiming what is
sure to work. `docs/` holds only the notes of the releases on GitHub and the README's pictures:
effort goes into the app, not into documents.

## Writing rules (texts in both languages, the setup page, comments, docs, commits, PRs)

- Write the way a developer explains their project to a friend who isn't very technical:
  natural, flowing prose in everyday English (US spelling), with sentences of different lengths.
  Connect ideas that belong together instead of giving each one its own short sentence, and say
  why something is the way it is when that helps. These notes are terse because they are a
  reference; nothing written for people should sound like them. On the TV, where room is tight,
  lines stay short, but they are still whole, natural sentences.
- A word limit is kept by leaving things out, never by squeezing what stays. Every sentence is
  one a person would say out loud: no clipped or telegraphic lines, no row of short sentences
  that each tick off a fact, no sentence tacked on to deliver one ("You switch between them in
  the quick settings."), no parts of the screen that act like people ("the last line names the
  next song"), no stiff or odd wording that only machine-written text has. In the README that
  style tells a first-time visitor that nobody cared, and the whole project loses its first
  impression. Read every sentence aloud before it goes in.
- The README speaks in the first person only where it says where Encore comes from, in a
  sentence or two as the owner's other READMEs do; everywhere else it talks about Encore and to
  the reader. A text asked to be short says
  each point once in the fewest natural words, with no reasons, contrasts or examples nobody asked
  for; count the words before it goes in.
- No emoji (one opening each line of the README's Features is the exception), no em dashes, no dashes as punctuation, no exclamation marks, no bold for emphasis,
  no marketing words (seamless, effortless, elevate, delightful, immersive, robust, powerful,
  crafted), no filler, no poetic fragments. Commit subjects under 60 characters, PR descriptions
  under 300.
- No colons inside sentences in what people read (the README, release notes, pull requests,
  issues, discussions, replies there and new UI texts), since that style reads as
  machine-written. Two sentences, or a comma, instead. Older menu explanations still have some.
- No AI branding anywhere, Claude's or any other tool's: no footers, no Co-Authored-By, no session
  links, no "Generated with" or "Generated by" lines in commits, PRs, comments, issues or releases
  (`.claude/settings.json` turns Claude Code's own attribution off). A footer a tool adds by itself
  is edited out right away. The README's credits end with the owner's own line, "Developed with
  the assistance of AI.", which names no tool. Commits carry the owner's name (vednolacni). A cloud session's own
  git identity is Claude's, so the session start hook (`.claude/hooks/session-start.sh`) sets the
  owner's there.
- No generated names in public either. A cloud session's own branch (`ccr-…`, `claude/…`) is never
  pushed; the work goes to a branch with a short plain name that says what it is (`lyrics-look`,
  `seek-fix`).
- An English UI sentence is never longer than its German one (`LanguageTest` checks it).
- Never name a brand of light, speaker, media player or TV device in texts, names or comments.
- Whether a feature has been tried on a real device is internal: it stays in these notes and in
  what the assistant tells the owner, never in the README, release notes, pull requests or discussions,
  where it reads as if nothing had been tested. Public texts simply claim nothing unverified.
- Music metadata keeps its typesetting (`TrackText`); dashes inside strings and test data are
  data. Never search and replace them blindly.
- Before a release, grep for em dashes, dashes as punctuation and emoji.

## Build and test

```bash
./gradlew testDebugUnitTest                                    # the checks of every module (unit + Robolectric), not the picture tests
./gradlew testDebugUnitTest -Pencore.pictures -Proborazzi.test.record=true   # everything, every picture to its module's build/screenshots/
./gradlew :ui:testDebugUnitTest --tests '*StageTest*' -Pencore.pictures -Proborazzi.test.record=true   # one class, in its module
python3 ci/compare-pictures.py before/ layers/ui/build/screenshots/ changes/   # what looks different (Pillow)
./gradlew :androidApp:lintRelease                              # Android Lint over the app and every layer: every warning fails
./gradlew :androidApp:assembleRelease                          # R8-minified APK, signed with the debug key
python3 tools/make_launcher_assets.py                          # re-render banner + icons from the logo (Pillow, numpy, CairoSVG)
python3 tools/make_readme_pictures.py                          # README mockups, download button, wide logo (Pillow, numpy; names like wide_logos make only those)
./gradlew :speaker:testDebugUnitTest --tests '*EndToEndTest*' -Pencore.sendspin=$PWD/venv/bin/python   # the speaker against aiosendspin 10.2.0 (python3 -m venv venv && venv/bin/pip install 'aiosendspin[server]==10.2.0')
```

- Local SDK: `local.properties` → `sdk.dir=/opt/android-sdk` (not committed). compileSdk 37,
  targetSdk 36, minSdk 28 (Android 9: the release signature, scheme v3, needs it), in the
  version catalog.
- Modules: every layer is an Android library of its own in `layers/<package>/`, named as its
  package (`:domain`, `:app` for the controller, `:ui`); the app itself (MainActivity,
  EncoreApp, SpeakerService, the screensaver, the manifest and launcher resources) is
  `androidApp/`. What they share is set once in `build-logic/`: `encore.android.application`,
  `encore.android.library`, `encore.robolectric` and `encore.compose`, the Compose
  compiler, which also compiles the layers whose classes the screens take (color, domain, data,
  config, render, sound, setup, text, app), since it sees a class as stable only when
  it compiled it itself. A module names the layers it builds on as `implementation`, so it sees
  only those; a library whose types its own API shows is `api`. All words are `:text`'s
  resources (`text.R`), the fonts `:ui`'s. A library brings its R8 rules (`consumer-rules.pro`).
- Tests live in the module of their package. One that also needs a layer above its own moves
  up whole to the lowest module that sees it (`ProgressTest` in data, `DesignRulesTest` in ui);
  one that needs a layer beside its own takes it as a test dependency (`CoverPaletteTest`).
  Helpers shared by several layers' tests are modules of their own: `art-testing`
  (SyntheticCovers), `text-testing` (TestTexts), `domain-testing` (Catalog), `app-testing`
  (`AppFixtures`, which the controller's and the screens' `Fixtures` build on). The picture tests'
  mark, `Pictures`, stands in the root package in ui's tests, where all picture tests are. A test
  that reads a file outside its module gets its path from the build (`reads` in build-logic),
  which counts the file as the test's input.
- Kotlin warnings and lint warnings fail the build (`allWarningsAsErrors`, `warningsAsErrors`).
  A suppression always carries a one-line reason (the lint settings in build-logic, `@Suppress`
  with a comment).
- Maven Central rate-limits shared IPs (HTTP 429): Gradle and Robolectric use Google's
  mirror first (both `settings.gradle.kts`, `robolectric.dependency.repo.url` in build-logic's
  Robolectric plugin).
- Picture tests render how the screens look and check nothing themselves; they carry
  `@Category(Pictures::class)` and only run with `-Pencore.pictures`, so the everyday run
  stays quick. A new test that only renders pictures gets the category too. Pictures are not
  committed: CI renders them all, uploads them, and on a pull request also renders the branch it
  goes into and compares every picture (`ci/compare-pictures.py`; the job summary lists what
  changed, the artifact `picture-changes` marks each changed pixel in red). A refactor must leave
  every picture identical; locally, compare a copy of `layers/ui/build/screenshots/` (and the
  Chladni sheet in `layers/art/build/screenshots/`) from before the change with the same
  script. Renders must be the same on every run: an animated sequence whose content is made off
  the main thread wraps it like a still (`LocalStill`).
- Tests that need words use `TestTexts.german` or `TestTexts.english()` (Robolectric). Renders
  default to German through `Fixtures.texts` (the catalog's music and rooms are German);
  `Fixtures.Still(t) { }` renders in other words.
- The progress bar refreshes in a loop of its own, so a stage never goes idle: stills wrap
  their content in `Fixtures.Still { }` (provides `LocalStill`), animated sequences use
  `createComposeRule()` with `mainClock.autoAdvance = false`. The static stage for stills
  (`Stage`) lives in the test sources; the app only has its theater.
- Motion is judged as contact sheets: `TransitionTest`/`LyricsMotionTest` render frames to
  `layers/ui/build/screenshots/...`; tile them with Pillow and look at them with the Read tool.
- The emulator smoke test (CI) talks to a stand-in Home Assistant, `ci/fake_ha.py`, which
  speaks only the documented commands. With it running locally,
  `-Pencore.fakeHa=http://127.0.0.1:8123` lets `FirstLaunchTest` talk to it for a few
  minutes. The app logs its connection steps with the tag `Encore`, never a token.
- The speaker's end-to-end tests (`SendspinEndToEndTest`, `SpeakerEndToEndTest`) play from the
  server Music Assistant 2.10.7 runs, aiosendspin 10.2.0, started on this machine by
  `ci/sendspin_server.py`, and check every sample; they run only with
  `-Pencore.sendspin=<a Python with aiosendspin>`, as CI does. The smoke test plays a stream
  from it on the emulator, which has no sound.
- Run Gradle with a UTF-8 locale (`export LC_ALL=C.UTF-8`): test names and data hold umlauts.
- CI runs three jobs side by side: the release APK (the emulator smoke test starts from it), the
  tests of every module with their pictures, gathered into
  one tree (aiosendspin installed for the speaker's end-to-end tests), and lint. A release waits
  for all of them. A push or pull request that changes only the texts and
  pictures around the app (README, AGENTS.md, LICENSE, `docs/`, `tools/`, `.claude/`, the issue
  templates) runs nothing; tags and manual runs always run everything. The emulator jobs fetch
  their system image with `ci/install-system-image.sh`, which tries a broken download again.

## Signing and releases

- No key is in the repository. Release APKs are signed in CI (`ci/sign-release.sh`) with APK
  Signature Scheme v3 and the release key, which exists only as repository secrets
  (`RELEASE_KEYSTORE`: a PKCS12 keystore in base64 with the key under the alias `encore`;
  `RELEASE_KEYSTORE_PASSWORD`). The owner makes it once and keeps a copy safe: a new key would be
  refused by every installed copy, which could then only be uninstalled. Encore has its own
  application id (`io.github.vednolacni.encore`), so it installs beside Klangbild and never
  updates it.
- Only a version makes a release: a pushed tag (`v2026.11.0`) publishes one at once; a manual CI
  run on main with the version as `release` leaves it a draft only the owner sees, so the notes
  can be read once more before it is published by hand on GitHub. Its text is
  `docs/releases/<tag>.md`. A beta (`v2026.11.0-beta.1`) comes from a manual run on any branch (a
  pull request's too), as a draft the same way. Publishing a draft creates its tag, whose own run
  publishes nothing (it finds the release out already). This session's git cannot push tags;
  manual runs go through the GitHub tools. Klangbild's own tags are not Encore's and are never
  pushed.
- The assistant never publishes a release on its own: no version tag, and a draft is published by
  the owner, or on their word in so many words. Getting a release ready means its notes in
  `docs/releases/`, and when the owner asks for it, a draft from a manual run and its link.
- Versions follow the calendar, the way Home Assistant's do: the first release in a month is
  named after it (`v2026.11.0` for November 2026), and later releases that month count up the
  last number (`v2026.11.1`). A pre-release carries a hyphen (`v2026.11.0-beta.1`). Android
  decides about updates by `versionCode`, which only ever grows (the minutes from 2026 to the
  commit).
- The "Tidy up" workflow (started by hand) keeps only the newest releases, ten by default,
  deletes the tags of the releases that go (and those left behind by a release deleted on the
  website), and deletes branches merged into main. `docs/releases/` holds the notes of the
  releases kept.
- The manifest removes the permission AndroidX core adds for `ContextCompat.registerReceiver`,
  which the app does not use.

## Architecture (data source → state model → UI)

The packages build on each other in one direction, each a module of its own whose build file
names the layers below it, and `LayersTest` keeps them to this: math, json,
color, text, art and sound below everything; domain (the app's model and rules) on them; data (Home
Assistant and Music Assistant) on domain, sendspin and sound; config (the settings, their store and menus) on data;
render and lights; sendspin (the protocol, on json alone) and speaker (this TV as a
speaker, on config, data, domain, json, sendspin and sound); app (the controller); ui on top. MainActivity,
EncoreApp (the wiring) and SpeakerService stand in the root package, in `androidApp` with the
screensaver, and may use everything.

```
data/ha/        HaAuth (long-lived token, or a sign-in: refresh token + client id → 30 minute access
                tokens; a refused refresh is never tried again), HaRest (GET /api/states/<id>, all
                states and all actions (a script's fields) for the setup page), HaConnection (WebSocket: auth, subscribe_trigger with a
                state trigger for the followed entities, or subscribe_events state_changed for users
                who are not administrators; states read over REST when a subscription starts; a
                second swappable trigger for group members and lights via watch(); get_config;
                config/entity_registry/list_for_display (+ a sensor's display precision), read
                again on request; config/entity_registry/get_entries (unique ids, for every user);
                call_service (+ the call's context id);
                ping, backoff), EntityStore (state objects, never replaced by older ones, each
                state's context), PlayerParser (entity + MA get_queue; MediaKind from the MA URI,
                a book's description never an album; supported_features → PlayerAbilities),
                HaUrls, LocalNetwork (cleartext only to private addresses: the name before
                sending, the connected address on every hop), ArtworkUrls (CDN hi-res rewrites),
                HaSource (NowPlayingSource + LightBridge: player + sun.sun +
                "Everywhere" from the registry's MA players + a party of Music Assistant's Party
                plugin playing here (`party/player` every 60 s; `party/url`, the link its guests
                open), commands only where supported, seek in seconds, shuffle, the favorite
                button by its translation key, the entities of one's own followed (the buttons',
                their conditions', the values') with their precision and Home Assistant's
                temperature unit, a button run (a script with its variables and `player`), the
                lights' states and calls; this TV's own speaker shown instead of the one chosen
                while music plays on it (SpeakerSwitch), on the same connection, the commands,
                the queue and `player` going to the speaker shown), OwnSpeaker (this TV's own
                entity: its Sendspin id → Music Assistant's player around it (`players/all`) →
                the registry's unique id; asked again with a backoff, and from the start once the
                registry is there), QueueReader (the queue
                around the song that plays, retargeted when another speaker shows: MA
                queue (one read at a time, only for MA players with a queue) + the optional MA
                queue window; a Music Assistant song published once its queue has described it;
                the words of the song that plays, found by the URI Home Assistant names as what
                plays; with Music Assistant set up, its own queue and the songs to come asked
                every 15 s while a song plays, since Home Assistant hears nothing of a queue
                changed mid-song (and a song moved further down keeps the next one and the
                count): what comes next and the songs to come, published when they change; its
                word holds for that song only; the position from that same answer, to a fraction
                of a second, on Music Assistant's clock as its `time` command gives it, while it
                agrees with Home Assistant's), TrackMatch (one
                song in two descriptions), StationPictures (a station's song: the best picture on
                offer wins and holds)
data/ma/        MaClient (Music Assistant's own API, optional: POST /api with a long-lived token;
                `metadata/get_track_lyrics` on a client of its own with a long timeout;
                `player_queues/get`: the queue as it stands, its next item always up to date, and
                where it stands in it; `time`: its clock; `players/all`: the player around a
                Sendspin client),
                MaQueue (queue items around the current one → QueueWindow with the current item's
                speed, the current and next songs' URIs and the words they bring; books' authors,
                episodes' shows, chapters; pictures via imageproxy), MaLyrics (the words of the song
                that plays and the one up next: from the queue item, else asked, one song at a
                time, the one that plays first; kept by URI; a call for a song no longer playing
                or next canceled, its answer dropped; a version without the call: none),
                MilkdropSound (another speaker's sound from the MilkDrop plugin's relay, optional
                and off by default in Music Assistant: `/milkdrop_visualizer?player=<queue>` with
                the token as its first message, 1024-sample mono windows stamped on the server's own
                clock, through a TimeFilter of its own; the rate told by the windows' spacing;
                beats; close; only while read; none without the plugin)
data/           NowPlayingSource, LightBridge (what the lights need from Home Assistant), ArtworkLoader (size limits; the token only
                to Home Assistant's host, through a client without redirects; a larger rendition
                guessed from the address gets a short try and is not asked again for a while
                when it fails; two loads at a time; covers cached on disk), Backoff,
                Http (cancellable OkHttp fetch with a call timeout; HttpStatusException)
domain/         TrackInfo (+ pictureKey and sceneKey: a station's song by its picture too, kind,
                chapters), PlayerSnapshot (+ SpeakerGroup, QueueInfo, speed, PlayerAbilities),
                SpeakerLabel ("Living room + 5", "Everywhere", room names, "35%"), TrackText
                (typographic cleanup), Progress (runs at the item's speed) + SmoothedPosition,
                TimeText (track times, separator, "Speed 1.5×"),
                ScreenStateMachine, Spoken (MediaKind, Chapter/Chapters/ChapterPlace:
                chapters, labels, jumps), SleepTimer (minutes or an end reached by playing),
                FieldClock (the color field's own time, shared by screen and lights), SunClock (the
                sun by the local clock, without `sun.sun`), EntityId (what an entity id looks like; THIS_TV, the setup page's
                stand-in for this TV's own speaker), SpeakerLink + SpeakerStatus (how this TV stands
                as a speaker), SpeakerSwitch (when its own speaker takes the screen and gives it
                back), ValueText (a value of one's
                own in words: precision, unit, on and off, a time of day), SourceState (what a source
                knows: LinkState, MaLink, EntityState), QuickAction (+ ActionKind) and Condition (a
                button of one's own and when it shows), OwnValue, Lyrics (Lrc: LRC read tolerantly, tidied or raw, plain words none;
                lines with breaks; LyricsCursor: the line in the middle a little early, a small step
                back held)
text/           Texts (the words of one language: resources, plurals, numbers, percent, dates, the
                clock in 12 or 24 hours), AppLanguage + PlatformLanguageStore (LocaleManager from
                API 33, the app's own preference below), Localizer (StateFlow<Texts>,
                refreshed on configuration, locale and time changes), LocalTexts/texts()
color/          Oklab, CoverPalette (k-means in Oklab → ScenePalette, inks, both walls), Lamp (the
                warm daylight every color is seen in, its hue, near-neutral colors), Appearance
                (LIGHT / DARK, by the sun), Contrast (WCAG luminance and contrast ratio)
math/           smoothstep, Rnd (a random source with a fixed seed), hash64 / hashUnit
json/           string(): a JSON string, or null for anything else
sound/          The sound feed for the visualizer to come: SoundFeed (what is heard at a moment on the
                monotonic clock, read only while someone reads), SoundFrame (loudness, four bands,
                onset, the latest beat and downbeat, SoundTiming: exact, close or none),
                SoundAnalyzer + Fft (1024-sample Hann windows: A-weighted loudness and bands on
                Sendspin's -60 dB to full scale, spectral flux against its recent mean; nothing
                allocated per window), SoundTimeline (frames and beats ahead of time, read as they
                come due), PcmSound (this TV's own playback, measured on the audio thread every 512
                samples while read, exact)
render/         StageGrid (the player's grid, or with `centered` the setup page's and the menu's), ScreenSpec, RectDp, QrCode, PrintShape (the print's
                outline: rounded square or disc), CoverField (cover colors: ground + soft pools in
                the cover's colors, the veil with text shade) + FieldMotion (where each pool stands
                at a field time) + FieldModel (the field on the CPU, for the text shade), WallGlow
                (the artwork's light on the dark wall), LampLight (lamp tint for rest and menu),
                SceneBuilder (per-track precompute: Lanczos cover at exact px, wall, palette;
                relight), ImageOps (blur, dither, Lanczos; thumbnails keep transparency: what is clear
                gives no color)
art/            ChladniArt (missing-cover art), StableHash
lights/         LightSync (any light behind the TV via Home Assistant: a color per light, or a
                white, a brightness or just on, as the light can; saved with scene.create
                snapshot_entities and given back with scene.turn_on, our own note as a fallback;
                rate limits, takeover by others), LightColors (CoverLightColors: the cover's colors
                kept apart, placed as in the cover, moving on with motion), LightColor (xy of a color's hue at its purest, grays as white (neutral by day,
                warmer at night), a color temperature for white-only lights, brightness floor)
app/            NowPlayingController (single source of truth → StateFlow<UiState>, with the texts;
                settings from the menus, the setup page and the launch's extras taken at once,
                the newest written one at a time) and its parts: MenuControl (the groups, then a group's settings), QuickControl (+ the
                own buttons, the party code, the player's buttons),
                Seeking (jumps with the remote), SleepControl, LightTargets, HeldStage (the stage
                kept from before the clock and when it goes; the clock held while a song's first
                scene is made); ControllerHost (what the parts need), Scenes (scene cache + builds
                off the main thread; another frame lets go of the builds for the one left),
                SoundSelection (the one sound feed: this TV's own playback while the music shown
                plays here, the MilkDrop feed for Music Assistant's other speakers, none otherwise)
                (EncoreApp/AppGraph in the root package: http, tokenHttp without redirects, localizer,
                SpeakerHub; SpeakerService beside them)
config/         Settings (+ DisplayOptions, VolumeDisplay, Intensity, AppearanceMode, BackgroundMode,
                MotionLevel, RoomLights, RoomLightBrightness, tvSpeaker and speakerOn), SettingsStore (DataStore, written whole
                in one step; tokens sealed by SecretBox: AES-GCM with a key in the Android keystore;
                `migrate` at the start seals tokens stored plain, touching nothing else, since
                rewriting all would open the tokens and lose one the keystore cannot open at that
                moment), SettingsMenu (groups,
                entries, values, explanations, steps; what needs something set up first dimmed),
                QuickMenu (the quick settings' entries and values: Lyrics On / Off (a quiet On for a
                song without words), Party code On / Off, Favorite, Lights, Shuffle, the own buttons,
                Settings; PLAYER_BUTTONS, the two that stand on the stage),
                QuickActions (the own buttons as stored: a name, an entity, a script's variables, the
                condition: a state or attribute is, is not, is above or below a value; a list counts
                its entries), OwnValues (the values in the status line as stored),
                LaunchExtras (am start extras and encore:// links: English names and values, the
                German ones of earlier versions still taken; `tv_speaker` an extra, never a link)
sendspin/       Sendspin, Music Assistant's protocol for synchronized audio (the spec's main, as
                aiosendspin 10.2.0 in Music Assistant 2.10.7 speaks it): Noise (KKpsk2 over
                BouncyCastle's lightweight X25519, ChaCha20-Poly1305, SHA-256 and HMAC; no provider
                registered), Keys (client id, Sentinel and pairing PSKs, the SP:0 pairing token),
                Frames (transport messages, fragments), Messages (the JSON envelope, hello, state,
                commands), TimeFilter (the spec's two-dimensional Kalman filter, as the reference
                has it), Session (client/init to the stream: handshake, re-handshake, pairing,
                bursts of 8 time messages, available only once synced, leave, goodbye; an older
                or newer Music Assistant told apart: only a server that closes before it answers
                counts as older, a connection lost on the way is lost)
speaker/        SpeakerConnection (ws://<Music Assistant host>:8927/sendspin through the client
                without redirects; backoff, a try at once on wake), SpeakerStore (the key and the
                pairings sealed, in a store of their own; an identity that cannot be opened is
                never replaced; output delay, volume, mute), ChunkQueue (timed PCM, filled by the
                connection, emptied by the audio thread, nothing allocated), Playback (10 ms blocks
                at the moment the server meant: the output's timestamps trusted once they move on,
                put in place at once on a start, a clear, a gap or an underrun, a frame dropped or
                repeated against drift; volume ramps, output delay, silence while another app has
                the sound), AudioSink + AudioTrackSink (48 kHz 16-bit stereo, timestamps, routing),
                ArrivalDelays (the min_buffer_ms asked for), MonoClock (the monotonic clock in µs
                as a plain long: nothing boxed on the audio path, where "no time" is a sentinel,
                never null), Speaker (all of it, an audio thread at urgent priority while a stream
                plays and 30 s after; a key the keystore refuses asked for again, less and less
                often; playing only while its group plays; volume, mute and output delay kept one
                write at a time; its playback and Music Assistant's beats (visualizer@v1, beats
                only) as a PcmSound)
setup/          MiniHttp, SetupHandler (one-time code, the sign-in through Home Assistant's login
                page: state, callback, code exchange, revocation of what is not kept; API, tokens
                bound to their address, the own buttons and values, every entity and the scripts'
                fields; this TV's own speaker left out of the list, where "This TV" stands for it;
                Music Assistant's speakers first, and while there are any a line saying to choose
                one of those, since a speaker mostly shows up twice;
                the pairing code only through the API), SetupPage (HTML in the language shown;
                entities chosen by searching),
                SetupServer (+ discovery; Host, Origin and content type checked, a few
                connections at a time, slow ones closed, only while the setup screen shows and
                the TV has an address of its own, tried again until it has; a request that has
                arrived is still answered when it stops, checked against the address it came to;
                once a stop has returned, the port takes no connection: it waits for the thread
                blocked in accept(), which holds the socket open until it lets go)
ui/             EncoreRoot (screen switch, OLED drift, menu overlay, peek, the player's buttons;
                provides LocalTexts),
                stage/ (Stage layers, Typesetting, StageTheater transitions, ProgressBar (a range:
                the chapter; another song, a bar of its own), StageModel + StageTexts, FieldLayer:
                the pools as gradient quads, drawn small and scaled; Lyrics: the words in the
                column, set once per song off the main thread, one canvas, LyricsViews keeping
                their places through a change;
                PartyCode: the party's code where the cover stands, one picture per wall;
                PlayedLines: a song's own time, next and chapter once it no longer plays;
                PlayerButtons: Lyrics and Settings under the bar's right end, lit when chosen),
                Motion (+ StageTick: one frame tick for bar, field and words; rememberFrames: that
                tick, or a loop of its own for a part shown alone; animateGlide: a list's glide that
                carries on from its pace),
                OffMain (bitmaps off the main thread, at once in stills),
                theme/Type (Jost + DM Sans, `titleType()`/`textType()`, Figures, ink shift),
                MenuScreen (+ MenuGrid; two levels), QuickBar (+ QuickGrid; a long row glides),
                RestScreen, SetupScreen, Changes (`playChange`, `RisingText`: one change at a time, hurrying,
                held lines), DebugOverlay (frame timing, and the sound feed's meter: its timing in
                words, bars for loudness, bands and onset, a dot on each beat; read and drawn only
                while it shows)
dream/          EncoreDream (DreamService with ComposeView)
test only       SyntheticCovers + Catalog (generated test covers and tracks, test://cover/NAME, a
                book and an episode, invented words, the README's aurora), AppFixtures and TestTexts,
                each in a -testing module; Fixtures (the controller's and the screens' tests, built on
                AppFixtures), the static stage for stills (ui's tests)
```

## Design principles (do not regress)

They hold for every change, optional ones included. A setting that brings back what a principle
rules out breaks it just as much as a new default would, so a second layout or a look to choose
from stays out even when it is off by default.

UI words are given in English with the German in parentheses where it matters.

- **A well-made object, laid out as a player.** No icons, no harsh white. The only controls on
  the screen are the two buttons under the bar's right end, Lyrics (On / Off) and Settings, quiet
  chips in the stage's own inks that light up only while down on the remote chooses among them
  (`PlayerButtons`); without Music Assistant only Settings stands there. The artwork is the
  heaviest element and always the tallest: the text block beside it takes at most 62% of its
  height (`BLOCK_SHARE`, measured from the first cap height to the last baseline, never from text
  boxes).
- **Centered, measured on what is seen** (`CenteringTest` measures rendered
  pixels in both languages): the stage grid (960 × 540 dp, `StageGrid`) is artwork 290 dp at
  88 dp from the left and 44 dp from the top, as far as the buttons' lower edge from the bottom;
  64 dp gap; the text column from there to the 88 dp right margin; the progress bar across the
  screen from margin to margin, its times' baseline 14 dp above the buttons (36 dp tall); while
  the song's words take the column, the song's title (its cap height 22 dp under the cover) and
  artist stand under the cover. As much room left of the artwork as right of the column and the
  bar. Text block centered on
  the artwork; the column stands within the artwork's straight edge, a corner radius in from its
  top and bottom (the artwork stays the tallest on what is seen; its edges recede at the round
  corners and where a dark cover meets the dark wall; until 6.1 the descenders of "Up next" hung
  below it and the column read as taller): the status line ("9:47 PM · Living room + 5") hangs
  there with its cap height, the descenders of "Up next" (Als Nächstes) end there
  (`Fonts.DESCENT`); every line starts on the column's left edge. The rest clock's figures (top to baseline,
  ink-centered across from the outlines' exact bounds: side bearings differ) stand in the very
  middle, its lines under it; setup and menu on the centered grid (`StageGrid(centered = true)`,
  a 368 dp square in the very middle of the height): setup's code + longest text line centered as
  a group. No optical lifts, no re-hanging. The whole screen drifts only ±6 dp against
  burn-in. Square artwork has 8 dp corners.
- **Two faces, one voice:** Jost (Owen Earl, OFL) for titles ≥ 24 sp, the large clock,
  headings and all figures (`titleType()`); DM Sans (Colophon, OFL, optical size = size in
  sp) for everything ≤ 21 sp (`textType()`). Both shipped unmodified, both cap height 0.70.
  Sentence case only: no capitals, no letter-spaced labels, no italics, no third family
  (those read as "AI template"); the platform's own system face is out (it reads as the TV
  device's own UI). Title 44/40 sp (2 lines, weight 400, −1%), then 34/30 sp (3 lines, 420,
  −0.5%), 26 sp (3 lines, 430), 24 sp (4 lines); Japanese/Chinese/Korean titles at 0.88×. A
  version ("Radio Edit") is a quiet subtitle line (DM Sans). Artist 21 sp/500; album, work
  17 sp; labels, times, status, next 15 sp/440, nothing smaller. Large lines are pulled left by
  their extra side bearing (`opticalIndent`). Times are set in Jost with `Figures`: digits in
  equal cells (DM Sans has no tabular figures and a narrow 1), colon raised 0.123 em; the
  rest clock keeps natural figures, set by hand as outlines (`ClockFigures`): its colon raised
  to the figures' middle as measured at its weight (on the baseline it sagged 18 dp below them
  until 2026.10), each 1 drawn in by most of its wide side bearings (it stood apart), and it
  dissolves into each new minute. Neither face has
  U+202F, U+2009 or U+2002: `Texts` turns the platform's narrow spaces into U+00A0.
  Titles ≤ 3 lines, prefer breaks after section dashes, no mid-word breaks
  (`breaksInsideWord`), a word of one to three letters stays with the next ("Spiegel / im
  Spiegel"); artists on one line (smaller, then "and others" (u. a.)); the year leaves the
  album line before anything is cut; "Up next" drops the artist before it cuts; edge emoji
  dropped (`Emoji.trimEdges`), inner ones dimmed; one separator `TimeText.SEPARATOR`;
  `TrackText.typeset` for dashes and no-break spaces after abbreviations; RTL lines isolated;
  in UI prose a short word opening a sentence never ends a line, and articles and adjectives
  stay with their noun (no-break spaces in the string resources).
- **Text color:** three levels only: title; artist, clock and the bar's fill; everything
  else (like white at 100 / 80 / 62%; on paper Oklab L 0.24 / 0.37 / 0.49, the quietest
  still 4.5:1), near-neutral with a breath of the cover's hue (chroma ≤ 0.009), never #FFF,
  never ivory. Every level keeps 4.5:1 (`DesignRulesTest`; on paper `lightestInkOn` holds it).
- **No night mode:** the stage looks the same at every hour, seen in one warm daylight
  (`Lamp`); only the wall (appearance) and the lights' brightness follow the sun. A night mode
  that hid lines at dusk looked like a crash (v3.0.2) and was removed in 5.2; its dimmed, warmer
  light went in 6.1. Never hide anything by the time of day.
- **Light and dark wall (`Appearance`):** automatic by the sun (light from sunrise to sunset,
  elevation −0.833°, 0.5° hysteresis; the local clock without `sun.sun`) or fixed. The light
  wall is warm paper (Oklab L 0.915) with a breath of the cover's color pulled toward warm and
  kept within hues 50° to 90° (a cool cover gives less color, never mint), never white.
  **Nothing frames a cover**: no shadow, no hairline, no mat, on either wall; every soft shadow under the artwork read as an uneven, broken frame (v5.1;
  `NoFrameTest` checks rendered pixels). A wall change cross-fades the stage in 2.4 s, its
  lines and the status line with it (until 2026.10 the new inks stood at once, a pale ghost on
  the old paper; `WallChangeTest`); every ground reaches beyond the screen's edges
  (`BEYOND_EDGES_DP`), so no strip of the old wall ever shows at a side (`EdgesTest`).
- **Light (`WallGlow`):** a tight halo in the colors of the artwork's own edges, the same on
  every side the screen's edge leaves it (above the cover the black band is near, and the light
  fades sooner there): each wall point takes the nearest edge color, averaged over a stretch that
  grows with distance (diffuse, no streaks); at most L* ≈ 17 at the edge and never more than
  0.4 × the edge's own luminance (a dark cover gives little light), falling as a Gaussian
  with σ = 26 dp, gone before the text column (which stands on black). It fills 2 dp under
  the artwork's edge so the anti-aliased rim never shows black. The 36 dp next to the screen
  edges stay black (`EdgeBandTest` checks rendered pixels), faded in as a product of a
  horizontal and a vertical ramp (a min() of edge distances creases along the diagonals).
  One bitmap per scene, drawn as one rectangle, dimmed through a color matrix.
- **Progress:** a classic bar: 4 dp, round ends, no knob, track 16%, fill at the artist's
  level, elapsed left, the track's length right (only one number ticks). Refreshed about 30
  times a second aligned to frames, on its own layer; jumps > 1.5 s glide in 350 ms; paused,
  the fill dims. It stands across the screen, apart from the words, so it shows as set beside
  them too; hidden by setting, back on a key press. A Music
  Assistant player's position comes from Music Assistant's queue once it has been asked (every
  15 s while a song plays), to a fraction of a second where Home Assistant has whole seconds, as
  long as the two agree within 1.5 s; a jump Home Assistant has heard of wins until the queue
  tells it too.
- **Per frame only cheap work:** blur, palette, resampling and glow happen once per track in
  `SceneBuilder`; animations only change alpha, transform and color filters, read in draw
  lambdas.
- **Every cover fills its square:** a small one (a station's picture) is enlarged once with
  Lanczos. No passe-partout: a smaller print in a mat (v5.0) read as a half-transparent frame
  on paper and, optically centered, as a print that had slipped (`CoverFillTest`).
- **Calm motion** (`Motion`): a track change (1 s) never goes dark: the new artwork comes up
  over the old (0 to 600 ms), the old one goes once it is covered (600 to 750 ms), the light
  turns to the new colors (0 to 800 ms) while the old wall stays whole until the new one is
  up; old text leaves in 220 ms, new lines rise one after another from 260 ms (the dip through
  dark of 5.1 read as a glitch on every skip). Up to 4 s without music between two songs, the
  stage stays (`Timing.betweenSongsMs`) instead of flashing the rest screen. Same picture
  (album), or the song's words coming or going: only text; same track, new scene (a better
  cover): quiet cross-fade. Pause (600 ms) dims light, artwork and text; nothing moves, so the
  lines hung on the artwork's edges stay aligned.
  Mute is only a word; a lost link dims like a pause. Nothing slides, nothing bounces.
  Screens switch by fading through dark. A song that comes after the clock or the setup page
  shows its own stage once its scene is made, never the stage left from the song before, nor the
  same song's from a wall that has changed meanwhile. The clock stays until then (its words a
  moment longer), never the dark, also when no stage was kept; the keys, the quick settings and
  the lights act on what shows. A screen that says something
  is wrong, and the setup page, go through the dark, since what they said no longer holds
  (until 2026.10 the song before came up first and the lights took its colors; `HeldStageTest`).
  **One change at a time:** what arrives during a change waits until it has landed, then the
  newest goes next (`snapshotFlow … conflate()`).
  Only another *song* hurries the running change (`playChange`, ≤ 350 ms); a better cover just
  waits. Lines on their way up stop where they
  are when a change hurries and leave from there (`RisingText`): the skipped song's lines
  never come up; if the skip comes to nothing (back to the same song), they rise the rest of
  the way. A change that begins between composing a frame and drawing it is drawn as the stage
  stood until it is composed (until 2026.10 the lines blinked out for that frame;
  `SongChangeBlinkTest`). A stage still on screen after its song has ended (the next song's
  scene still being made, or its lines leaving) keeps its own time, its own "Up next" and its
  chapter, its time read for that song by name, since the player reports the next song a
  moment before the screen shows it (until 2026.10 a stage left waiting showed the new song's
  time, "Up next" and chapter, and lines leaving showed its "Up next" and lost their own time
  and chapter; `OwnLinesTest`).
- **States say it in words**, in the status line, place first and state last: "Living room
  · Paused", "Kitchen · Muted", "Volume 35%" (3.5 s), "Everywhere"; "Reconnecting…" in the
  progress bar's place.
- **Values of one's own** (`OwnValues`, the setup page's section 7, at most 3; setting
  "Values"): the state of any Home Assistant entity, or one of its attributes, after a name of
  one's own, between the time and the place ("21:47 · Outside 12.3 °C · Living room + 5").
  Numbers in the language's figures with Home Assistant's display precision and their unit
  (a weather entity's own units, a thermostat's in Home Assistant's), on and off in words, a
  time as a time of day (`ValueText`); what Home Assistant cannot tell shows nothing, so a
  template helper can hide a value. The place always has its room: values that do not fit
  beside it are left out from the last, never cut. They tick like the clock, no cross-fade.
- **The song's words** (`Lyrics`, `LyricsText`; Lyrics (Songtext) on the stage's button, in the
  quick settings and under Information, remembered; Music Assistant only, nothing else to set
  up): the text column set for them, not a screen. The line sung now stands centered on the artwork (first cap height to last
  baseline, `CenteringTest`) in the title's ink; the lines around it in the quiet ink, each
  fading as a whole with the distance of its nearer edge (whole within 40 dp, like the settings
  list), gone before its far edge comes within 8 dp of the status line or the bar.
  One size per song, no size or weight changes: Jost 30, 26 or 24 sp, the largest at which no
  line takes more than two rows (three at 24; two voices count apart), balanced breaks, a short
  word kept with the next, Japanese, Chinese and Korean at 0.88×. The list glides one step in
  500 ms, starting 300 ms before the line is sung, the light going with it (a glide that comes
  while one runs carries on from its pace); a break (an empty line, a lone note or
  "[Instrumental]", at least 4 s) only dims the line; before the first line it waits unlit in
  the middle; a jump lets the words go, then come at the new place; a step back of less than
  1.5 s (a corrected position) never moves them. The picture never moves. The status line keeps
  its place; the song's title (Jost 24) and artist (DM Sans 17) stand under the cover, each on
  one line, the title cut after a whole word; the bar stays across the screen below everything.
  Switching them on or off, and words that come late, use the stage's text change
  (`Change.COLUMN`). The words come from the queue item (`media_item.metadata`: `lrc_lyrics`, else
  LRC in `lyrics`), else from `metadata/get_track_lyrics` (Music Assistant 2.8 and later; raw
  LRC before 2.10) with a 75 s timeout of its own (Music Assistant asks its online source at
  most once in 30 s), never holding up the queue reads; the song up next is asked ahead. Songs
  only, synced only: plain words are none. Set once per song off the main thread; a frame only
  moves one canvas.
- **Remote:** OK plays and pauses, held it opens the settings; left and right change the song
  (spoken word: jump); down chooses among the player's buttons (left and right between them, OK
  presses), down again opens the whole row of the quick settings and, pressed once more, closes
  it; up closes them, and otherwise only shows hidden parts for a moment (down does too; with
  "Up and down: Volume" both turn the speaker's volume instead); back closes what lies on top (a
  settings group goes back to the groups first, then the settings, the quick settings), else the
  app. Commands go to Home Assistant only where the player's
  `supported_features` allow them.
- **Quick settings** (`QuickMenu`, `QuickBar`; down twice on the remote, or once without a
  song on stage): one row along the bottom edge, rising 22 dp as it fades in, where the key
  points: Lyrics On / Off (only with Music Assistant connected and a song on stage; while this song has no synced
  words it still says On, in the name's ink and chosen no brighter than another value at rest,
  since a word of its own, "None" (Keiner), read as switched off until 2026.10; OK turns them
  off), Party code On / Off (only while a party plays here), Favorite (with Music Assistant; a press
  that comes back with an error says Failed and logs Home Assistant's answer, and once Music
  Assistant counts the song as a favorite it says Added),
  Lights On / Off (off remembers the level), Shuffle On / Off (only where the player can
  shuffle; the sleep timer lives in the menu), then the own buttons, and last Settings: Open,
  the way into all settings. Left and right choose, OK changes, down, up or back close, 20 s
  alone close them. Each entry is its name (15 sp, quiet) over its value (20 sp), on the stage
  grid from 88 dp; its place is as wide as its widest value (only the last is as wide as what it
  says), so nothing moves when a value changes; the chosen one lit from below like the menu's
  line. A row longer than the room (in German the built-in entries alone are) glides to show the
  chosen entry whole, entries beyond its ends faded out; every entry fits the room on its own
  (`LanguageTest`). An opaque veil (black, paper) from 448 dp down, softened above: nothing
  behind shows through. The player's buttons on the stage are two of these entries
  (`QuickMenu.PLAYER_BUTTONS`): chosen among them, the row stays away and the chosen button
  lights on the stage; the screensaver shows no buttons (any key ends it).
- **Own buttons** (`QuickActions`, the setup page's section 6, at most 6): a name of one's own
  (at most 32 characters; without one, the entity's own words) and a Home Assistant entity,
  whose kind says what OK does: a script runs (`script.turn_on` with its variables (the fields
  it declares, asked for on the setup page, and any others as `name: value` lines) and the
  speaker followed as the variable `player` unless it names one, so one script can ungroup
  whatever plays), an automation runs its actions (`automation.trigger`), a scene is
  activated, a button is pressed, a switch, input boolean or light is toggled and shows On /
  Off. The value is the verb ("Run"), then "Done" or "Failed" for 4 s; a call still running
  after 4 s (a long automation) counts as taken. An entity Home Assistant does not know or has
  as unavailable is left out of the row, and so is a button whose condition does not hold
  (`Condition`: an entity's state or attribute is, is not, is above or below a value, as Home
  Assistant's own state conditions; a list counts its entries, so `group_members` above 1 means
  the speaker plays in a group; anything more is a template helper's state) unless it is just
  saying how its call went. Buttons are told apart by their place: one script can be two
  buttons with other variables.
- **Party code** (`PartyCode`; Music Assistant's Party plugin with guest access on): while a
  party plays to this speaker (`party/player` names its queue, asked every 60 s; two failed
  questions in a row end it; a server without the plugin is not asked again), the quick settings
  offer Party code Off / On, off at first. On: the QR code of the link guests open (`party/url`,
  asked again every 60 s, since a code expires) stands where the cover stands, a card like the
  setup's on whole pixels with the cover's corners and light, dimmed in a pause. The song's
  lines stay beside it, the wall's light and the lights
  stay the song's. It comes and goes in a quiet cross-fade once its picture is made, stays while
  songs change, and takes the new paper with the wall. While it shows, a pause never turns into
  the clock, so the guests can always scan it. Off, or when the party ends or plays elsewhere: the cover
  comes back, and the next party starts without it. `PartyCodeTest` reads the code from rendered
  pixels where the cover stands, on both walls, on the cover's colors and in a pause.
- **Settings menu** (OK long-press, Menu, or Settings in the quick settings), in two levels as
  on every TV: first the groups, then the settings of the one opened. It opens on the groups; OK
  or right opens a group on its first line, Back goes to the groups (on the group left) and from
  there out. The centered grid, heading (Settings, or the group's name) and the chosen line's
  explanation on the left (on the groups, what is in each), lines on the right; the chosen line
  sits at the screen's middle in a soft, low band of light (σ 120 × 12 dp, ≈ L* 10) that lights
  it alone; the list glides through it and fades with distance. Into a group or back out, the
  level left fades (140 ms), then the new one comes (220 ms), already resting on its line: two
  lists never lie over each other. A held key runs the list on evenly (`animateGlide`: a glide
  that comes while one runs carries on from its pace; until 6.3 it started over from a
  standstill at every repeat and stuttered), a step moves only the cursor, and the explanation
  goes while the key is held and comes back with the line it is let go on; the quick settings'
  row glides the same way. Values are words, changes apply at once (left and right clamp, OK
  wraps; right held on a group opens it once, and changes nothing in it until pressed again),
  closes itself after 60 s. Every setting is always there: one that cannot work until
  something is set up on the phone (Lyrics and Play here without Music Assistant: "Needs Music Assistant"; the
  lights and the values while none are chosen: "Set up") is dimmed, never brighter when chosen
  than another line at rest, its explanation says what it needs, and OK opens the setup. Options
  show or hide parts, choose the wall and the language, and dim the light; no
  fonts, colors or layout variants. Labels are the common words people know from TVs and phones,
  values plain words: groups Picture (Appearance, Background: Plain /
  Cover colors, Intensity, Motion: Off / Slow / Medium / Fast), Information (Clock, Progress
  bar, Next track, Album and year, Speaker group, Volume, Values, Lyrics), Lights (Brightness:
  Off / 10 to 100% with a slider, Lights that are off: Turn on / Leave off), Remote (Control music,
  Up and down: Buttons, menu / Volume, Audiobooks, podcasts: Jump / Change track), Timer
  (Sleep timer, Turn off after, When turning off: Close app / Screen saver), System (Home
  Assistant, Music Assistant, Play here: On / Off, Language: System language / English / Deutsch). Label + value fit
  the 352 dp column on one line in both languages (`LanguageTest` measures every value).
- **Language:** English is the default, German in `values-de`. The menu's Language line uses
  Android's per-app language (`LocaleManager` from API 33, where the choice also shows in the
  system's app settings; the app's own preference below API 33) and applies at once
  everywhere, the phone's setup page included, without restarting the activity. Every text
  comes from `Texts` (resources, plurals, numbers, percent, dates); the time of day follows
  the system's 12 or 24 hour setting. A recreated activity never applies its launch extras
  again.
- **Cover colors** (setting "Background", `BackgroundMode.COVER`): the cover's own colors as the
  wall, edge to edge and 18 dp beyond (`OVERSCAN_DP`: the ±6 dp drift never shows a border; the
  only wall without the black edge band, `EdgeBandTest` checks the edges are filled). A deep
  ground plus one mass per cover color (up to 5), placed where the color sits in the cover and
  pushed ≥ 0.42 screen widths apart; lit cores in the 3 most characteristic masses; 1 to 2
  soft pools of light in front, high on the picture's side only (never under the text beside it
  or below it). Far =
  large and soft, near = smaller, lighter, a little firmer (depth of field). Dark tones L 0.15
  to 0.56 (rich, chroma ≤ about 0.5 × L), paper L 0.86 to 0.975 (pale washes). Each pool is one
  quad with a radial gradient in a quarter-size layer (`FieldLayer`), saturation 1.2 after
  mixing; grain against banding; a veil with a shade under the text worked out per cover (the
  column, and the row under the cover across the screen with the song, the bar and the buttons;
  sampled over 20 min of motion, aiming at 4.8:1 so the quietest text keeps 4.5:1, tested on
  rendered pixels). No shadow under the artwork, no vignette. `FieldModel` computes the same
  field on the CPU (text shade, lights). On color the lower inks stand a little lighter
  (0.82 / 0.71); the progress bar takes the cover's leading hue.
  "Intensity" sets how strong the colors are. The next phase makes the cover colors the default
  background.
- **Motion** (Off / Slow / Medium / Fast = speed 0 / 0.3 / 0.6 / 1.0; the main layer turns
  once in about 470 / 230 / 140 s): only while music plays, on the field's own clock
  (`FieldClock`): a pause freezes it, nothing jumps; screen and lights read the same clock. One
  frame tick (about 30 a second) for the field and the progress bar, never two loops out of step,
  and none while nothing reads it (no bar shown, the colors still, the settings over the stage).
- **Lights** (any light Home Assistant controls, chosen on the setup page; Off or 10 to 100% in
  steps of 10, default 60%, a slider in the menu): brighter by day, the level as set while the
  sun is up, down to × 0.45 at night (sun elevation 6° to −8°), so they show in daylight and
  stay calm in the evening. Three lights, three colors (`LightColors`). From the cover
  (`CoverLightColors`): its most telling colors (color counts more than size), kept ≥ 0.09
  Oklab apart, placed as they sit in the cover (leftmost color left, rightmost right, the
  third above the TV); one color gives itself and its neighbors ±32° round the hue circle, a
  gray cover three whites. With motion the colors move on from light to light (a step per 90 s
  of field time, resting 70% of it). Each light shows
  what it can (`supported_color_modes`): a color goes as the xy of its hue at its purest, 42
  to 92% of the way from white (light on a wall looks far paler than a screen), and Home
  Assistant fits it to the light; only real grays are white (4300 K by day, 3000 K at night,
  never the amber of 2700 K, which read as plain orange in 5.1); a white-only light gets a
  color temperature of the color's warmth within its own range (Home Assistant's own
  conversion would turn red into cool white); a dimmer gets a brightness; an on/off light is
  only switched on. Dark colors softly; pause × 0.5 (after 2 s). `transition` only for lights
  that support it. Before taking a light over its state is saved with `scene.create`
  (`snapshot_entities`, one scene per light, our own note as a fallback); it is given back
  with `scene.turn_on` after the music has ended for 5 minutes, when the option goes off and
  when the app closes (lights that were off: dimmed, then their scene without a fade), and
  the scenes are deleted. At most one call per light a second, none for invisible changes, a new
  song after 1.5 s (quick skips cost nothing) in a 2.5 s fade, drift every 10 s in 8 s fades. A
  light someone else switches off, sets to white or changes a lot, during the music or after it,
  is theirs until the next session; what a person or an automation does counts at once, even
  while a fade settles. The lights' states arrive a moment after they are followed: taking over
  waits for them (≤ 10 s) and a light that appears later joins in; a light taken over stays
  followed until it is given back, so the dim and the fades reach it (until 2026.10 the lights
  were let go as the music ended, and went back without a fade); lights no longer chosen, or
  switched off, go back at once, over the clock too; a failed call is retried once, for that
  light only. "Lights that are off" (Lichter, die aus sind): Turn on (the default, as above) or
  Leave off: then a light that is off is never switched on and not saved; one switched on during
  the music is saved and joins in, one switched off is let go and joins in again when switched
  on, and after the music each is as it was before it first joined. Leave off chosen while they
  glow: the lights that were off go back off, the others keep their colors; one that Home
  Assistant still reports on (a slow light reports late) is not taken up again for 10 s.
- **Skip flicker** (`FlipGuard`): a return to an item left within 5 s is held 2 s and shown
  only if it is still what plays; new items pass at once, and so does the remote's "previous".
- **A song arrives whole** (`QueueReader.awaitsDetails`): Home Assistant reports a Music Assistant
  song first with a 512 px picture and without its year; the queue tells the rest about 0.4 s
  later. A song the queue announced as next is described at once; any other waits for its
  queue read (at most 2 s), while the song before it stays as it was, all of it. So the first
  picture is the song's own at full size, and nothing changes a moment after a song starts.
- **A station's pictures** (`StationPictures`): Music Assistant shows the station's picture
  first and the song's once it has looked it up, and may fall back for a while. The song's own
  picture (named by the queue window, or one with an address of its own) ranks above any
  other, the station's (named by the queue, or seen with another song) lowest; while a song
  plays its picture never steps down and never returns to one it has left. Home Assistant's
  proxy renews its token every few minutes: a picture's identity leaves the token out
  (`pictureId`), and a station's song gets a new scene when its picture changes
  (`TrackInfo.sceneKey`).
- **Audiobooks and podcasts** are told by Music Assistant's URI (`…://audiobook/…`,
  `…://podcast_episode/…`): a book's description (where an album would be) never shows, an
  episode's show stands where an artist would. With Music Assistant: the chapter in the album's
  place (a number-only name as "Chapter 7", a long one cut after a whole word), the bar spans
  the chapter, "Up next" the next chapter, the speed in words ("Speed 1.25×") and the position
  running at that speed. Left and right jump back 15 / ahead 30 s (presses add up, the bar
  shows it at once), held they go by chapters; setting "Audiobooks, podcasts": Jump / Change
  track. A held key never repeats a command.
- **Sleep timer** (menu, no setting): Off / 15 / 30 / 45 minutes / one hour / End of chapter /
  End of episode (track, audiobook); the status line says "Pauses at 11:10 PM". An end counts
  only when playing reaches it (a jump across moves the timer on); the item's end is paused
  1.5 s early so the queue never moves on first. It lives in the app: only while Encore runs.
- **Home Assistant** is used only through its documented interfaces (WebSocket API, auth API,
  REST, media player, light and scene entities). Sign-in from the phone through Home
  Assistant's own login page (authorization code; client id and redirect URI on the TV's setup
  server); a long-lived token still works. A refresh token Home Assistant refuses is never
  tried again (each try counts as a failed login there). Tokens are sealed at rest, only ever
  go to the address they were made for, never follow a redirect and are never logged; a new
  address without its credentials drops the old ones; a replaced sign-in is revoked.
- **This TV as a speaker** (`sendspin/`, `speaker/`, `SpeakerService`; System: Play here (Hier
  abspielen), off by default, dimmed with "Needs Music Assistant" without it; the setup page: a
  checkbox in Music Assistant's section, the pairing code folded beneath it while the speaker
  runs): the TV becomes one of Music Assistant's speakers over Sendspin, connecting to the
  address it already has on port 8927 (8095's /sendspin is the web player's), PCM 48 kHz 16-bit
  stereo only, encrypted (Noise KKpsk2) and offering unpaired access, so Music Assistant 2.10.7
  takes it without a pairing step; an older one is told apart and the menu's explanation says
  what it needs. It is named as the TV is (its device name), never like a web or app player
  (Music Assistant hides those). Its key is made once and kept: a new one would be a new speaker
  in Music Assistant and a new entity in Home Assistant. A foreground service of the media
  playback kind runs it while the setting is on, from the moment Encore is opened (Android
  starts none by itself after a reboot), with or without the screen; with the setting off
  nothing of it runs, no service, thread or connection (`SpeakerServiceTest`), and every
  picture stays as it was. `available` only once the clock filter has settled (3 bursts, error
  under 5 ms); each 10 ms block placed by the output's timestamps (none from before the start,
  stalls and frames never written refused), at once on a start, a clear, a gap or an underrun,
  else a frame at a time outside 100 µs (±100 ppm held within 1 ms, `PlaybackTest`); the volume
  is a gain ((v/100)^1.5, 20 ms ramp), since CEC pins the system volume (at 100% the status line
  leaves it out, since the TV's own volume, which the app cannot see, sets what is heard); the output delay (0 to
  5000 ms) is kept. The audio thread runs at urgent audio priority and allocates nothing per
  chunk. Another app taking the TV's sound for a moment silences it in time underneath, for good
  makes it leave its group; the TV waking or an HDMI device coming or going puts it in place
  anew. Music that starts on it shows instead of the chosen speaker, on the same connection
  (`OwnSpeaker` finds its entity for every user): paused it stays unless the chosen speaker
  plays, stopped it gives the screen back after 4 s (`SpeakerSwitch`); the keys, the volume, the
  queue and a button's `player` act on what shows, and another speaker's volume is no change to
  show. "This TV" at the top of the setup page's list follows it alone (`EntityId.THIS_TV`),
  and its own entity is left out of that list; while it does, Play here stays on (the menu's OK
  opens the setup, where another speaker is chosen first, and a launch extra cannot switch it
  off), and its explanation still says what keeps the TV from playing. Tested end to end against
  aiosendspin 10.2.0, and in daily use on Klangbild's owner's TV, where it plays well.
- **The sound feed** (`sound/`, `SoundSelection`; from Klangbild, for a visualizer it planned,
  shown nowhere yet but in the debug overlay's meter): for each short stretch, loudness, four bands and
  onset, the latest beat Music Assistant knows, when it is heard on the monotonic clock, and how
  exactly that is known. This TV's own playback is measured from the samples on their way to the
  output (exact); another Music Assistant speaker's from the MilkDrop plugin's relay (close: as
  close as Music Assistant knows that player's position; none without the plugin); anything
  else has none. One analysis for both, cheap enough for the box; nothing is measured, asked or
  connected while nobody reads it, and the meter draws frames only while the overlay shows.
- **Music Assistant** is optional: without it Encore works from Home Assistant alone; with it
  (address + long-lived token on the setup page, checked before storing) come the lyrics, the
  album line's year and the chapters.
- **Generated art:** Chladni plates are ring modes (two or three nodal circles inside 0.64 of
  the radius) made wavy by a second mode (3 to 6 lobes, rounded: amplitude ≤ 1.5 r/(n² − 1),
  share capped so circles never touch), mirror-symmetric, no sand beyond 0.74 to 0.84 of the
  radius. No nodal diameters (crosses, wheels, rosettes), no single circle or two lobes ("O").
- **Rest screen:** large light clock ("9:47" or "21:47" as the system is set, 132 sp, natural
  figures) in the very middle of a dim pool of light (centered; burn-in care
  is the ±6 dp drift, the changing figures and the screen ending after "Turn off after"); one
  plain sentence when something is wrong ("Home Assistant can't be reached.").
- **The phone's setup page** follows the same rules: one (system) sans face, sentence case, no
  italics, no gradients, no shadows, no pill buttons, no icons, light or dark as the phone is
  set, in the language the TV shows. An entity is chosen by searching its name, never from an
  endless list. The setup server runs only while the setup screen shows (saving ends it, and
  the answer to the save still reaches the phone), answers only requests for its own address,
  takes API calls only as JSON from its own page and with the six-digit code (a wrong one is
  answered only after a second), and closes slow connections.
- Never claim device behavior that was not verified on the device (frame rate, CEC wake,
  dream on the TV device): mark it as unverified.
