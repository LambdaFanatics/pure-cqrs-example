### Garage management domain

#### Events 
- CarRegistered,            A new car has been registered to the garage
- CarRepaired,              The car has been repaired
- PartMarked,               A damaged part of a car marked to be repaired
- PartUnmarked,             A previously marked part has been unmarked
- PartRepaired,             A damaged part of a car has been repaired



#### Commands
- RegisterCar
- RepairCar  
- MarkPart
- UnmarkPart
- RepairPart

#### Database 
- events table, an append only events log - emulates a persistent log data structure.
- event_consumers table, holds the current offset of each event log consumer (event handler). 
- cars table, read side view of the available cars.
- car_parts table, read side view of the damaged parts of the cars.

