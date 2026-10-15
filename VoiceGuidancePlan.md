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

## Where things stand on 2026-10-08

- `StateMachine.md` is complete.
- Samuel had written the types and a half-finished `PLANNED` branch in `TripSessionContract.kt`.
- **On 2026-10-08 Claude wrote the whole function and its tests, against the agreement above.** That skipped steps 3 to 6 of Samuel's own work.
- Claude's version also departs from the agreed design in these places:
  - `Message` still carries English text, not one typed member per sentence.
  - It is a `tripSessionMachine` function, not a reducer with `reduce`.
  - It uses `kotlin.time.Instant`, not time-since-boot.
  - The field is still `timeStamp`, not `timestamp`.
