
## Trip session state machine

Example trip used in the wording: walk to Archives stage, ride Super Metro, alight at Embakasi.

**States:** Planned, WalkingToStage, OffRoute, AtStage, OnBoard, ApproachingStop, AlightNow, MissedStop, Arrived, Cancelled.
Arrived and Cancelled are final: the trip ends, GPS and the foreground service stop.

**Events:** User tap, Location fix, GPS lost. Nothing else goes in the Event column.
If an event arrives and no guard matches, stay in the current state and do nothing.

| Current state                                   | Event                     | Guard                                                                    | Next state      | Action                                                                                                           |
|-------------------------------------------------|---------------------------|--------------------------------------------------------------------------|-----------------|------------------------------------------------------------------------------------------------------------------|
| Planned                                         | User taps Start           | Location permission granted                                              | WalkingToStage  | Say "Walk 200m to Archives stage"; Tick                                                                          |
| Planned                                         | User taps Start           | Location permission not granted                                          | Planned         | Ask for location permission; no buzz                                                                             |
| WalkingToStage                                  | Location fix              | Within 30m of the stage, 3 fixes in a row                                | AtStage         | Say "You're at the stage. Look for Super Metro to Embakasi, fare around 100"; Milestone                          |
| WalkingToStage                                  | Location fix              | Distance to the stage has grown for 30 seconds                           | OffRoute        | Say "You're walking away from the stage"; Warning                                                                |
| OffRoute                                        | Location fix              | Distance to the stage has shrunk, 3 fixes in a row                       | WalkingToStage  | Say "Back on track"; Tick                                                                                        |
| AtStage                                         | User taps "I'm on board"  | Always                                                                   | OnBoard         | Say "Sawa, 9 stops to Embakasi. I'll tell you when to get off"; Tick                                             |
| AtStage                                         | Location fix              | Faster than 15 km/h and more than 100m from the stage, 3 fixes in a row  | OnBoard         | Say "Looks like you're on the move. I'll tell you when to get off"; Tick                                         |
| OnBoard                                         | Location fix              | Within 800m of the stop, or past the stop before it, 3 fixes in a row    | ApproachingStop | Say "Get ready, Embakasi is coming up"; GetReady                                                                 |
| OnBoard                                         | Location fix              | More than 300m from the route, 5 fixes in a row, not yet warned          | OnBoard         | Say "This bus may not be going to Embakasi, check with the conductor"; Warning; once per trip                    |
| ApproachingStop                                 | Location fix              | Within 300m of the stop, 2 fixes in a row                                | AlightNow       | Say "Alight at Embakasi in 300 metres"; Alight                                                                   |
| AlightNow                                       | Location fix              | Within 50m of the stop and slower than 7 km/h, 3 fixes in a row          | Arrived         | Say "You've arrived at Embakasi"; Milestone; end trip                                                            |
| OnBoard, ApproachingStop, AlightNow             | Location fix              | More than 150m past the stop and faster than 15 km/h, 3 fixes in a row   | MissedStop      | Say "You've passed your stop. Ask to be dropped now if it's safe, otherwise at the next stage"; Alight           |
| MissedStop                                      | Location fix              | Slower than 7 km/h for 60 seconds                                        | Arrived         | Say "You're off the bus. Embakasi stage is Xm behind you"; Milestone; end trip                                   |
| OnBoard, ApproachingStop, AlightNow, MissedStop | User taps "I've alighted" | Always                                                                   | Arrived         | Say "Trip ended"; Milestone; end trip                                                                            |
| Any except Arrived, Cancelled                   | User taps Cancel          | Always                                                                   | Cancelled       | Say "Guidance stopped"; Tick; end trip                                                                           |
| Any except Arrived, Cancelled                   | GPS lost                  | No fix for 60 seconds, not yet warned since the last fix                 | Same state      | Say "I've lost your location, watch for your stop"; Warning                                                      |

"Past the stop" means the nearest stop on the route comes after the alight stop in the stop sequence.

At the stage for a long time: no row on purpose. The user may be in a queue, so we stay quiet.

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
| Wrong-bus distance   | 300m       | Bus has left the route                      |
| GPS lost timeout     | 60 seconds | No location fixes                           |
