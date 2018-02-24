### Garage management domain

#### Events 
- CarRegistered,            A new car was registered to the garage
- DamagedPartAdded          A damaged part of a car was identified
- DamagedPartRemoved,       A previously damaged part of a car was removed
- DamagedPartRepaired,      A damaged car part was repaired
- CarRepaired,              The car was repaired- all the parts repaired


#### Commands

#### Database 
- events table, an append only events log - emulates a persistent log data structure
- damaged_cars table, read side view of the available cars.
