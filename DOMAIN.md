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
- events table, an append only events log - emulates a persistent log data structure
- damaged_cars table, read side view of the available cars.
