
## Trip session state machine

Example trip used in the wording: walk to Archives stage, ride Super Metro, alight at Embakasi.

**States:** Planned, WalkingToStage, OffRoute, AtStage, OnBoard, ApproachingStop, AlightNow, MissedStop, Arrived, Cancelled.
Arrived and Cancelled are final: the trip ends, GPS and the foreground service stop.

**Events:** User tap, Location fix, GPS lost. Nothing else goes in the Event column.
If an event arrives and no guard matches, stay in the current state and do nothing.

| Current state                                   | Event                     | Guard                                                                    | Next state      | Action                                                                                                           |
|-------------------------------------------------|---------------------------|--------------------------------------------------------------------------|-----------------|------------------------------------------------------------------------------------------------------------------|
| Planned                                         | User taps Start           | Location permission granted                                              | WalkingToStage  | Say "Walk 200 metres to Archives stage"; Tick                                                                          |
| Planned                                         | User taps Start           | Location permission not granted                                          | Planned         | Ask for location permission; no buzz                                                                             |
| WalkingToStage                                  | Location fix              | Within 30m of the stage, 3 fixes in a row                                | AtStage         | Say "You're at the stage. Look for Super Metro to Embakasi, fare around 100"; Milestone                          |
| WalkingToStage                                  | Location fix              | Distance to the stage has grown for 30 seconds                           | OffRoute        | Say "You're walking away from the stage"; Warning                                                                |
| OffRoute                                        | Location fix              | Distance to the stage has shrunk, 3 fixes in a row                       | WalkingToStage  | Tick (no speech)                                                                                                 |
| AtStage                                         | User taps "I'm on board"  | Always                                                                   | OnBoard         | Say "Sawa, 9 stops to Embakasi. I'll tell you when to get off"; Tick                                             |
| AtStage                                         | Location fix              | Faster than 15 km/h and more than 100m from the stage, 3 fixes in a row  | OnBoard         | Tick (no speech)                                                                                                 |
| OnBoard                                         | Location fix              | Within 800m of the stop, or past the stop before it while on route, 3 fixes in a row | ApproachingStop | Say "Get ready, Embakasi is coming up"; GetReady                                                                 |
| OnBoard                                         | Location fix              | Off the route and every learned road, 3 fixes in a row, then 200m closer to the stop than where the detour began; not yet said| OnBoard         | Say "The driver is taking another road. I'll still tell you when to get off at Embakasi"; Tick; once per trip             |
| OnBoard                                         | Location fix              | Off the route and every learned road, 3 fixes in a row, then 500m further from the stop than where the detour began; not yet warned| OnBoard         | Say "This bus may not be going to Embakasi, check with the conductor" (urgent); Warning; once per trip                    |
| ApproachingStop                                 | Location fix              | Within 300m of the stop, 2 fixes in a row                                | AlightNow       | Say "Alight at Embakasi in 300 metres" (urgent); Alight                                                                   |
| AlightNow                                       | Location fix              | Within 50m of the stop and slower than 7 km/h, 3 fixes in a row          | Arrived         | Say "You've arrived at Embakasi"; Milestone; end trip                                                            |
| OnBoard, ApproachingStop, AlightNow             | Location fix              | On route, more than 150m past the stop and faster than 15 km/h, 3 fixes in a row | MissedStop      | Say "You've passed your stop. Ask to be dropped now if it's safe, otherwise at the next stage" (urgent); Alight           |
| MissedStop                                      | Location fix              | Slower than 7 km/h for 60 seconds                                        | Arrived         | Say "You're off the bus. Embakasi stage is X metres behind you"; Milestone; end trip                                   |
| OnBoard, ApproachingStop, AlightNow, MissedStop | User taps "I've alighted" | Always                                                                   | Arrived         | Milestone; end trip (no speech)                                                                                  |
| Any except Arrived, Cancelled                   | User taps Cancel          | Always                                                                   | Cancelled       | Tick; end trip (no speech)                                                                                       |
| Any except Arrived, Cancelled                   | GPS lost                  | No fix for 60 seconds, not yet warned since the last fix                 | Same state      | Say "I've lost your location, watch for your stop" (urgent); Warning                                                      |

"Past the stop" means the nearest stop on the route comes after the alight stop in the stop sequence.

At the stage for a long time: no row on purpose. The user may be in a queue, so we stay quiet.

## Speech

The buzz always comes just before the words, so attention arrives first.

**Say less.** Moments the user already knows about (they tapped the button) or that need no words
(back on track, boarding noticed) only vibrate. Speech is for boarding, the approach to the stop,
getting off, and the moments when the app can no longer help.

**Priority.** Sentences marked (urgent) ask the user to act now, or warn that the app can no longer help.
- An urgent sentence cuts off a normal one and clears the queue.
- An urgent sentence never cuts off another urgent one: it waits its turn.
- Normal sentences wait their turn.

**Starting up.** Sentences that arrive before the speech engine is ready are held, then spoken.
An urgent one replaces anything held. If there is no engine at all, the trip carries on with haptics only.

**Language.** Sheng is spoken by the Swahili voice. If the voice for the user's language is missing,
the whole sentence is spoken in English, not the Swahili words in an English voice.

**Music** lowers its volume while a sentence plays, and comes back when the queue is empty.

**End of trip.** The last sentence ("You've arrived…") finishes before the speech engine is released.

## Haptic patterns

| Pattern   | Vibration                                              | Meaning                   |
|-----------|--------------------------------------------------------|---------------------------|
| Tick      | One short buzz                                         | Acknowledged, information |
| Milestone | Two short buzzes                                       | You reached something     |
| Warning   | One long buzz                                          | Something is wrong        |
| GetReady  | Two long buzzes                                        | Your stop is coming       |
| Alight    | Three long buzzes, repeated until tapped or 10 seconds | Get off now               |

## Thresholds

All of these are starting guesses. Tune them on real trips.

| Name                 | Value      | Used for                                    |
|----------------------|------------|---------------------------------------------|
| Stage radius         | 30m        | Reaching the boarding stage                 |
| Off-route time       | 30 seconds | Walking away from the stage                 |
| Vehicle speed        | 15 km/h    | Detecting boarding, detecting a missed stop |
| Walking speed        | 7 km/h     | Detecting that the user is off the bus      |
| Get-ready distance   | 800m       | First alert before the stop                 |
| Alight distance      | 300m       | Strong alert before the stop                |
| Arrival radius       | 50m        | Reaching the alight stop                    |
| Missed-stop distance | 150m       | Passed the stop while still riding          |
| Off-route distance   | 300m       | Bus has left the route and its learned roads |
| Detour progress      | 200m       | A detour still heading for the stop is a shortcut |
| Detour loss          | 500m       | A detour heading away from the stop may be the wrong bus |
| GPS lost timeout     | 60 seconds | No location fixes                           |


## Shortcuts

A matatu that leaves the route to beat traffic is not on the wrong bus. While riding, the machine judges
a detour by where it is heading, not where it is. When the bus has been off the route for 3 fixes, the
distance to the stop at that moment is remembered. Getting 200m closer than that is a shortcut, said
once. Getting 500m further is a likely wrong bus, warned once. Standing in traffic changes neither, so
it stays quiet. Coming back to the route forgets the detour.

"The route" is the straight line through the stops plus every road learned from riders' traces
(`TripPlan.knownPaths`, loaded from `/route_variants`). A bus on a learned road is on route, so common
shortcuts say nothing at all. On a detour the nearest stop says little, so "past the stop before it"
and "past the stop" only count while on route.
