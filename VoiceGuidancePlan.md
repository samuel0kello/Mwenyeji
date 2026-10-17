# Voice guidance:

Written 2026-10-08 from the chat of 2026-10-03, so the plan is not lost. The trip table itself lives in `StateMachine.md`.

## How we work together

- **Samuel writes the code. Claude does not generate it.** Claude guides as a senior would.
- **Claude reviews only when Samuel says he is ready for a review.**
- **When Samuel is stuck, Claude explains in words** (questions to ask, hand traces, sentences), not code.
- **Work in small steps:** one phase at a time, tests green before the next phase.

The one exception so far: Samuel asked Claude to fix the table in `StateMachine.md`, and Claude rewrote it.

## What the feature is

Voice guidance during a commute, working the way maps apps do, with haptic feedback.

- The user says or types something like "From my location I want to go to Embakasi".
- The app suggests a trip: walk to Archives, look for Super Metro or Cozi buses, fare around 100 KES, confirm the fare with the conductor.
- Once the user starts walking, the app switches to guide mode. It keeps updating them during the walk and the ride, down to "you're almost at your stop".
- Every guidance event comes with a vibration, so it works from a pocket.

## Decisions made

| Decision | What we agreed |
|---|---|
| Offline | Offline-first. Network is an optional upgrade, never a gate. |
| Languages for v1 | English, Swahili and Sheng. |
| Sheng output | A third phrase set spoken by the Swahili voice. Sheng has no Android locale or voice. |
| Facts | The LLM never invents directions, saccos or fares. Facts come from the planner and the guides. |
| Guide mode | Must run with no LLM and no network. |
| Understanding a request offline | No LLM. Match place names and aliases ("tao", "Archives") plus a few trigger phrases per language. |
| Free-form chat | An online extra, not part of the offline core. |
| Activity Recognition | Left out of v1. Speed and distance from location fixes are enough. |
| Wake word | Dropped for v1. Use a button or headset button. |
| Interrupting speech | Tap only, so the mic does not hear the app's own voice. |
| Naming | Call it "voice guidance" in code and UI. "TalkBack" is Android's screen reader. |

**Still open:** Claude recommended that in v1, typed and tapped input works offline in all three languages, and voice input uses the device recognizer where available and the network otherwise. Samuel has not answered yet.

## Build order

Build the guidance engine first and the chat last.

1. **Trip planner (pure Kotlin).** Origin and destination in, a plan of legs out: walk to stage, ride route X, alight at stop Y. Direct routes first, transfers later.
2. **Trip session state machine (pure Kotlin).** Takes location fixes and taps, gives back speech and vibration effects. Tested with fake fixes.
3. **Announcer.** Turns a guidance message into speech in the right language plus a haptic pattern.
4. **Foreground service (location type).** Hosts the session so guidance survives screen-off, with a notification showing the next action.
5. **Chat and voice input.** Chat screen plus speech-to-text, with the planner called as a tool. Any cloud model goes behind Firebase; no API key in the APK.

**First slice to ride with:** a "Start guidance" button on the route details screen. Pick an alight stop, and the phone speaks and vibrates as you approach it. This needs steps 2 to 4 in their simplest form, and no planner or LLM. Ride a real matatu with it before building anything else.

## Step 2 in detail: the trip session machine

This is the step in progress.

1. Write the trip as a table: state, event, guard, next state, action. **Done**, in `StateMachine.md`.
2. Write the types with no Android imports: state, event, effect, the phase list, the haptic pattern list.
3. Write one function that takes the state and an event and returns the next state plus a list of effects. Each table row is one branch.
4. Write one test per row, feeding fake location fixes.

The order Samuel was following when he got stuck:

1. Fix the state: nullable fields where needed, a default for every field.
2. Add the permission flag to the start event.
3. Write `PLANNED` as plain sentences in comments, then turn each sentence into a line.
4. Write the two `PLANNED` tests and get them green.
5. Trace `WALKING_TO_STAGE` on paper for both of its rows, then repeat step 3 for it.
6. Leave the other eight phases until those two work. They repeat the same two patterns.

How to read a branch off the table:

1. Which rows start with this phase?
2. Which event does each row name? Any other event means same state, no effects.
3. What is the guard? That is the if/else.
4. Next state becomes the new phase. Action becomes the effects list.

Every location fix does three things: measure, update the streak (add one if the guard is true, reset if not), then decide.

### Design points agreed in review

- **The trip plan stays out of the state.** It never changes during a trip, so it is passed in separately.
- **One streak counter per guard.** `OnBoard` watches three guards at once.
- **The timestamp travels inside the event.** The function never reads the clock.
- **GPS lost is produced outside the machine**, by a timer in the service.
- **The spoken message is a typed value, not English text.** One member per sentence in the table, each holding only its values (stage name, distance, stops left). The announcer turns it into English, Swahili or Sheng.
- **Effects are a list**, since most rows speak and vibrate.
- **Cancel and GPS lost are handled once**, before the per-phase branches.
- **`ARRIVED` and `CANCELLED` return the state unchanged.** A late fix must not crash the app.
- **Name the speed unit.** Android reports metres per second; the thresholds are in km/h.
- **Timestamps:** time-since-boot in milliseconds is safer than wall-clock time, which can jump.
- **Follow the onboarding pattern:** a reducer with a `reduce` function, like `OnboardingReducer`.
- **Share the haversine function** from `ProximityEngine`.

## Traps to plan for

- **Detecting boarding:** nothing tells you the user got on. Use the "I'm on board" tap, with speed as a fallback.
- **CBD GPS noise:** never change state on a single fix.
- **Inbound stops:** inbound is the outbound list reversed. Around Archives and the one-way CBD streets this will be wrong, so check alight alerts on the ground.
- **"In 200m turn left":** needs the Mapbox Directions walking profile. Offline gives only distance, direction and guide landmarks.
- **Platform rules:** Android 14+ needs a declared service type and the location foreground-service permission. Android 13+ needs notification permission. The vibration API differs below API 26, and minSdk is 24.
- **TTS:** check the language is installed, test how it says stage names, and request audio focus. The Swahili voice may be missing, so fall back to English.
- **Offline voice input in Swahili and Sheng** is the weak point. Check what the device recognizer supports on target phones.
- **Safety:** announcing a destination aloud in a matatu is a theft risk. Default to earphones when connected and offer a haptics-only mode.
- **Outside interruptions:** a phone call or unplugged earphones must stop speech.
- **Thresholds:** all ten numbers are guesses. The wrong-bus row and the missed-stop exit are the likeliest to misfire.

## The second machine: the voice loop

Samuel's first table (listen, think, speak) is a separate machine from the trip session. It is parked until step 5. Fixes agreed for when it comes back:

- Remove the "Internet connected" guard.
- Let the trip machine speak at any time. Urgent guidance preempts everything; chat replies wait.
- Send Listening with silence and an empty buffer back to Idle.
- Give "didn't hear", "didn't understand" and "no route found" their own spoken replies, and add a confirm step ("Embakasi, sawa?").
- Add haptics to every row.

## Where things stand on 2026-10-17

The first rideable slice (steps 2 to 4 plus the button) is built, on `feat/talk-back`.

- **History.** On 2026-10-08 Claude wrote the trip machine against the agreement. Samuel chose to keep it and refactor it. He fixed the design gaps with Claude's help (typed `Message`, reducer with `reduce`, time since boot in millis, `timestamp`). On 2026-10-17 he asked Claude to build the announcer and then the rest of the slice.
- **Step 2, trip session:** `feature/guidance/session/`. Four moments now only vibrate (back on track, boarding noticed, I've alighted, cancel); see `StateMachine.md`.
- **Step 3, announcer:** `feature/guidance/announcer/`. Phrasebook (English only; Swahili and Sheng fall back to English text), speech priority, `TtsSpeaker`, `VibratorHaptics`, `Announcer`.
- **Step 4, service:** `feature/guidance/service/`. `TripGuidanceService` is a location foreground service. It turns GPS fixes into events, sends GPS lost after 60 seconds, shows the next step in a notification with "I'm on board", "I've alighted" and "Stop", and stops itself after the trip ends.
- **The button:** each guide on route details has **Start guide**. A guide stores no stops, so the trip follows the direction the user searched. The stop is the searched destination when it matches a stop name, otherwise the far end of the route. The stage is the searched origin when it matches a stop, otherwise the stop nearest the user when the trip starts. It asks for precise location (and notifications on Android 13+), then opens the guidance screen.
- **Guidance screen:** `feature/guidance/ui/GuidanceScreen.kt`. (Moved to MapLibre and our own map on 2026-10-18; see below.) Mapbox Standard style with landmarks, the route as straight lines between stops (no road shapes yet), stage and stop pins, the user's position followed by the camera, the next step at the top, and "I'm on board", "I've alighted" and "Stop" at the bottom. Leaving the screen does not stop guidance.

**Next:**
1. Ride a real matatu with it and tune `TripThresholds`.
2. Write the Swahili and Sheng phrases.
3. Add a language setting; today Swahili is used only when the phone's language is Swahili.
4. Optionally store a stage and stop on each guide, so a guide can start mid-route without a search.
5. Draw real road shapes (`outboundShapeId`, `inboundShapeId`) instead of straight lines.
6. Steps 1 and 5 (planner, chat and voice input) come after the ride.

## Where things stand on 2026-10-18: our own map, and learning shortcuts

Goal: own the map, and know when a driver takes a shortcut instead of calling it the wrong bus.

**1. Shortcut or wrong bus (app).** While riding, a detour is judged by whether it is still heading for
the stop. See "Shortcuts" in `StateMachine.md`. New `Message.Shortcut`, thresholds
`OFF_ROUTE_RIDE_DISTANCE_M`, `DETOUR_PROGRESS_M`, `DETOUR_LOSS_M`, guard `DETOUR`.

**2. Our own map (app + `seeder/map/`).**
- The guidance map is MapLibre (`feature/guidance/ui/GuidanceMap.kt`), not Mapbox.
- The style (`assets/map/style.json`, from OpenFreeMap Liberty), its icons and fonts ship inside the
  app. Only tiles come over the network. `core/map/MwenyejiMapStyle.kt` loads it.
- Tiles: `seeder/map/build_tiles.sh` builds `kenya.pmtiles` from OpenStreetMap with Planetiler
  (about 220 MB). `node map/upload_tiles.js` puts it on Firebase Storage and prints the URL for
  `MWENYEJI_MAP_TILES_URL` in `gradle.properties`. While that is empty, the app uses OpenFreeMap's
  tiles, which have the same schema. Rebuild monthly.
- The OpenStreetMap credit is shown above the guidance buttons, as the licence requires.
- Globe view: MapLibre Android only has it in a pre-release (13.7.0-pre-globe.0). When a stable
  release has it, add `"projection": {"type": "globe"}` to the style.
- Mapbox is gone from the app (2026-10-18). The contribute flow's maps use `MwenyejiPinMap`, with
  pins dragged by hand-written touch handling (MapLibre's annotation plugin was last built for
  MapLibre 11). Place search uses Photon over OpenStreetMap (`PhotonSearchRemoteDataSource`), on
  komoot's public server for now; Nominatim stays available as a named alternative.

**3. Ride traces (app).** Asked once, the first time a guide is started ("Help Mwenyeji learn matatu
shortcuts?"). With consent, `feature/guidance/trace/RideTraceRecorder` keeps only the ride (no walk),
one point per 20 m. When the trip reaches its stop, it goes to `/ride_traces` with the route, direction,
hour and weekday: no account, device or date. Cancelled trips are not sent.

**4. Learning variants (`seeder/route_learning/`).**
- `start_valhalla.sh` runs Valhalla in Docker on Kenya's OSM data.
- `node route_learning/learn_variants.js [--dry-run]` snaps each trace to roads, finds stretches
  that left the route, groups them by road, and writes roads taken on 3 or more rides to
  `/route_variants`. Traces older than 90 days are deleted. Run it weekly.
- The logic is in `variants.js`, tested by `node --test route_learning/*.test.js`.

**5. Using variants (app).** At trip start the service loads `/route_variants` for the route and
direction (5 s timeout) into `TripPlan.knownPaths`. The reducer counts them as on route, and the map
draws them dashed.

**Firestore rules to add** (not deployed; the project's rules are not in this repo):

```
match /ride_traces/{id} {
  allow create: if request.resource.data.keys().hasOnly(
      ['routeId', 'direction', 'boardingStopId', 'alightStopId', 'weekday', 'hour', 'points', 'createdAt'])
    && request.resource.data.points is list && request.resource.data.points.size() <= 5000
    && request.resource.data.createdAt == request.time;
  allow read, update, delete: if false;   // only the learning job, with the admin key
}
match /route_variants/{id} {
  allow read: if true;
  allow write: if false;                  // only the learning job
}
```

**Next for this part:**
1. Upload the tiles and set `MWENYEJI_MAP_TILES_URL`.
2. Add the rules above.
3. Run `learn_variants.js` on a schedule once traces come in.
4. Use matched traces to give each route its real road shape, replacing the straight lines.
5. Before launch, run our own Photon on Kenya's OSM data instead of the public server
   (`SearchConfig.photonUrl`).
