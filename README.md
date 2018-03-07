# pure-cqrs-es-example
An implementation of a CQRS/ES system using functional FP in scala.

### Goal
This is a side project for learning and experimentation purposes. It is not a "production ready" system :)

We use a simplified domain model in order to focus only on technical implementation details and functional programming principles.
CQRS and Event Sourcing is a huge domain with many flavors and variations on its own.

Our goal is to implement some of the principles and ideas that we have in __a purely functional way__.

### How to build run and test?
```TODO```


### This is work in progress
and under heavy development. Many of the design decisions will propably change.

### Domain
The domain of the project is an minimal garage management system.

### Project tasks and status

[x] Write side server (that serves ES commands via REST API)

[x] Write side validator in memory store

[x] An event log (used for Event Sourcing) implemented in postgres

[ ] Read side server that serves a materialized view of the log (via REST API).

[ ] Read side server websocket support

[ ] An alternative event log implemented using kafka

[ ] Complete dockerized build process

[ ] User authentication / authorization (using TSec?)

[ ] A front SPA application (using Angular 2)

[ ] Nginx configuration as a reverse proxy for the read/write servers

[ ] Read side server testing

[ ] Write side server testing

### Stack

- Cats
- Http4s
- fs2
- Doobie
- Circe

- Docker
- Postgres

```TODO Fill the list```


### Resources 
These are some of the stuff we use to learn and take inspiration from:

- [Free monads and event sourcing architecture](http://www.stephenzoio.com/free-monads-and-event-sourcing/)
- [Introduction to tagless final](https://www.beyondthelines.net/programming/introduction-to-tagless-final/)
- [Smart Backpacker App (core on Github)](https://github.com/SmartBackpacker/core)
- [Scala Pet Store (on Github)](https://github.com/pauljamescleary/scala-pet-store)
