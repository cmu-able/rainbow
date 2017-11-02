# Rainbow components 
This directory contains components that make up most of the core of the Rainbow framework, as well as the more commonly used components such
as using Acme for the architecture model and Stitch for expressing, deciding, and executing adaptations. There are four main components:

1. **rainbow-core**: This is the code that is common to all Rainbow deployments, including definitions of the component types (classes) for
gauges, probes, effectors (actuators), models, analysis, and management components. Also, it defines the way these components can interact 
through port types (APIs) across a communication infrastructure. The most commonly used communication infrastructure is an in-house publish/subscribe
infrastructure called ESEB (Extremely Simple Event Bus) - the interfaces are defined in such a way that other communication infrastructures
can be relatively easily substituted.
2. **rainbow-acme-model**: This project defines an interface to the [Acme](http://acme.able.cs.cmu.edu/pubs/show.php?id=162) architecture description language that acts as a model of the software
being managed to capture, reason, and plan adaptations against. Deployments (e.g., ZNN) will build on this for specialized domains and systems.
3. **rainbow-utility-model**: A model that represents the utilities used by Rainbow and Stitch to decide which adaptation would be best in a given
situation
4. **rainbow-stitch**: An implementation of the [Stitch](http://acme.able.cs.cmu.edu/pubs/show.php?id=341) language.
