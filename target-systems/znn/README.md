# Installing ZNN

## Introduction
This document describes how to build ZNN on a clean set of machines. ZNN is 
web system based on a LAMP stack. To build ZNN requires that apache, mysql, 
php, and some other packages be compiled into the user space of the virtual 
machine, so that effectors, configuration, etc. can be done by the VM user. 

## Prerequisites

This documentation assumes that you are running a version of Linux (has only 
been tested on Ubuntu). 
It is assumed that the following Ubuntu packages are installed on each virtual 
machine that requires the build:
	build-essentials
	cmake
 	libncurses5-dev
	imagemagick
	libxml2-dev
 	zlib1g-devxml2
    bc
	psmisc
    wget
    
On Ubuntu use sudo apt-get to install the packages.

And that the znn-vm-package folder has been copied to each machine. 

## Building ZNN

The directory znn-vm-package
contains the following files and 
directories, which must be copied onto all machines that are planned to run part of ZNN:

|Directory|Description|
|-|-|
|effectors/ | This directory is part of the znn installation and defines scripts that Rainbow can use to change the configuration of znn at run time. |
|install/   | This directory contains the install scripts for installing various components of znn. It has three main scripts: `install-mysql.sh`, `install-lb.sh`, `install-web.sh` |
| news-src/  | This directory contains raw information for the source of ZNN, which at this stage contains articles, images and scripts for building low resolution images from them and installing them into the database.|
|sw-src/    | Contains the archives of the packages that will be built to install znn |
| znn/       | Contains the php files that implement znn. |
| znn-conf/  | Contains configuration information for the http servers, that are specific to ZNN. |
| znn-config | This file contains the configuration variables for the installation process. They define things like the location that ZNN is to be installed in, the usernames and passwords that are used for MySQL, the ports to be used, etc. |
| znn.properties | This contains property descriptions that define the IPs and ports that define the ZNN network configuration. | 
| sw-bin     | This (empty) directory will be populated with the appropriate binaries that are compiled as part of the build process. |
| sw-exp     | This (empty) directory will be populated with the expanded source archives for building the binaries for ZNN. |

### Configuring the build process

The variables in znn-config and (optionally) those in znn.properties should 
be customized to the configuration that you have in mind for ZNN. In 
particular, the ZNN_USER, ZNN_HOME, and ZNN_LIBXML2_SO variables should be 
changed for your configuration (e.g., on AWS, the ZNN_USER might be ubuntu,
ZNN_HOME might be ~ubuntu, ZNN_LIBXML2 might be /usr/lib/x86_64/libxml2.so).

The znn.properties define the following variables:

| Variable | Description |
|----------|-------------|
| customize.system.target.lb | The IP of the network that will run the load dispatcher.  |
| customize.system.target.lb.httpPort | The port on which the dispatcher will be listening. |
| customize.system.target.db | The IP of the MySQL database that will store articles and images. |
| customize.system.target.web<n> | The IP of a web server that can be dispatched to by the dispatcher. |
| customize.system.target.web<n>.httpPort | The port on which the web server is listening. |

Note that the IPs should all be on the same network (visible to each other 
and Rainbow).

Once these settings have been configured, you should copy all the files to 
each VM.

### Setting up the source

The source packages for ZNN are standard, off-the-shelf, but the versions we use have
been cached on the server at http://acme.able.cs.cmu.edu. These are listed in sw-src/sw-list.txt. 
To download these files, run the command:
```
install/download-packages.sh
```


### Building ZNN

In the machine that you designate as the database, run the command:
```
install/install-mysql.sh
```

This will compile, install, and load the database that is used by ZNN to 
store articles and images.

In the machine that you designate as the dispatcher (or load balancer - 
though technically it doesnt do anything but evenly split traffic between 
servers), run the command
```
install/install-lb.sh znn.properties
```
Note, that `znn.properties` should only be used if you have configured it with 
the information. Otherwise, you will be able to configure ZNN at a later date. 
The `znn.properties` file is used to work out which ip and port traffic should be 
directed to.

In the machine(s) that you designate as a web server, run the command
```
install/install-web.sh web<n> znn.properties
```
`web<n>` says which web server properties to use. In the same properties, it 
could be either web0 or web1, though you can have any number of servers. 
The znn.properties is optional, but is used configure the location of the 
database. It can be configured later.

### Running ZNN

To run ZNN is simply a matter of starting the services on the appropriate 
machines. The dispatcher and web servers can be started with httpd -k start, 
making sure that you refer to the appropriate binaries in sw-bin. The install 
scripts should tell you how to start the services.

If ZNN is running properly, you should be able to go to:
	`http://<customize.system.target.lb>:<customize.system.target.lb.httpPort>/wrapper.php`

This should return a news article.

Alternatively, you can use the following scripts to start, stop, and reset the 
ZNN configuration:

```
start-znn.sh | stop-znn.sh [-i credentials] [-u user] [-d db -l lb -0 w0 -1 w1  | properties]
```
Starts/stops znn on each machine. The IPs need to be those that are reachable 
from the running machine.

```
configure-znn.sh [-d delay] [-i credentials] [-u user] [-d db -l lb -0 w0 -1 w1  | properties]
```
Resets the configuration of ZNN (turns off captcha, etc), and alternatively 
sets a delay for ZNN responses.


