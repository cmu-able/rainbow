# Rainbow Self-Adaptive Framework

This repository contains the source code for the Rainbow self-adaptive framework, which is a research project within the Institute for Software Research, Carnegie Mellon University, Pittsburgh, PA, USA.

Rainbow uses model-based self-adaptation, primarly focussing on software architecture models, to help diagnose and localize problems and select adaptations that based on how they fix or improve architectural issues.

Research into Rainbow at CMU can be found in quite a few papers, detailed on the (Rainbow Project Site)[http://www.cs.cmu.edu/~able/self-adaptation.html#rainbow].

## Repository organization
This repository is organized into the following folders:

- **libs**: Contains the source of some in-house libraries used by Rainbow for distributed communication.
- **rainbow**: Contains Rainbow framework code, including definition of general Acme models for use inside the Models Manager, and the source for the adaptation language *Stitch*.
- **deployments**: Contains code to implement various specializations of Rainbow used in research projects
- **ide**: Contains code to implement specialized UIs, IDE integration, etc. that are probably not of general interest but are here for completeness.


Rainbow is built using Maven, and you will need to have access to the Maven repository that contains some dependencies. Please contact the owner of this repository for details on how to set this up.
