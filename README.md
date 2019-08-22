# Rainbow Self-Adaptive Framework (Version Yellow - 3)

This repository contains the source code for the Rainbow self-adaptive framework, which is a research project within the Institute for Software Research, Carnegie Mellon University, Pittsburgh, PA, USA.

Rainbow uses model-based self-adaptation, primarly focussing on software architecture models, to help diagnose and localize problems and select adaptations that based on how they fix or improve architectural issues.

Research into Rainbow at CMU can be found in quite a few papers, detailed on the (Rainbow Project Site)[http://www.cs.cmu.edu/~able/self-adaptation.html#rainbow].

## Repository organization
This repository is organized into the following folders:

- **libs**: Contains the source of some in-house libraries used by Rainbow for distributed communication.
- **rainbow**: Contains Rainbow framework code, including definition of general Acme models for use inside the Models Manager, and the source for the adaptation language *Stitch*.
- **deployments**: Contains code to implement various specializations of Rainbow used in research projects
- **ide**: Contains code to implement specialized UIs, IDE integration, etc. that are probably not of general interest but are here for completeness.

# Building
Rainbow is built using Maven, and you will need to have access to the Maven repository that contains some dependencies. Please contact the owner of this repository for details on how to set this up. 

## Building using Docker 
The Dockerfile in the root directory can be used for building Rainbow. It installs the necessary packages (java, maven, etc.) and uses `build.sh` to construct a .tgz file containing the directory that will be deployed when using Rainbow. To build the _Docker container_:

```
> docker build -t rainbow-build .
```

To run the build, the directory containing the source needs to be mounted to the container, and the appropriate parameters that are passed on to the build script. The parameters are:

```
Usage ./build.sh [-s] [-p rainbow-dir] [-d deployment-dir] [-t target] [-v version] [command]
    -d -the deployment that you want included in the build, either relative or in the dir deployments
    -t -the comma separated list of targets to include in the build, either relative or in the targets dir
    -v -the version label to give the release
    -s -skip tests in the build
    -p -the path containing the rainbow source code
command -the build command to give, defaults to install
```

For example, to build the `rainbow-znn` deployment using the scripts, models, etc. defined in `targets/znews-ss`, the following command should be issued after the Docker container is built.

`> docker run -v "$PWD":/root/rainbow -it rainbow-build -p /root/rainbow -d rainbow-znn -t znews-ss -s install`

This will produce a file `Rainbow-YYYMMDDHHmm.tgz` file in the directory.

Note, building this way will not cache any libraries that maven may download, and will not cache any install results. Do do this, you will need to mount a ~/.m2 directory, e.g.:

`> docker run -v /home/YOU/.m2:/root/.m2 -v "$PWD":/root/rainbow -it rainbow-build -p /root/rainbow -d rainbow-znn -t znews-ss -s install`

# New and Noteworthy

A summary of the changes that have been made in this version can be found at [New and Noteworthy](NewAndNoteworthy.md)

