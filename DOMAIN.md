### Garage management domain

#### Events 

- DamagedCarRegistered,          (A new car was registered to the garage)
- DamagedPartAdded,              (A damaged part of a car was identified)
- DamagedPartRemoved,            (A previously damaged part of a car was removed)
- DamagePartRepaired,            (A damaged car part was repaired)
- DamagedCarRepaired,            (The car was repaired)

#### Database 
- events table, an append only events log - emulates a persistent log data structure
- damaged_car table, read side view of the available damaged cars.
