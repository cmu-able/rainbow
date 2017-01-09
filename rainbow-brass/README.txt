This repository contains the code for Rainbow instantiation for the BRASS robotics project. It contains probes, gauges, effectors, analyses, planners, and models.

=== BUILDING ===
This repository is to be built with Maven 3, which uses the pom.xml to describe the build. You need to have maven installed on your machine (or m2e in your Eclipse environment). 
Make sure in your maven settings that you point to the maven repo to get access to the libraries needed to compile. This file is located in ~/.m2/settings.xml and should look like:

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
       http://maven.apache.org/xsd/settings-1.0.0.xsd">
       <servers>
               <server>
         <!-- just add this server below if you already have a settings.xml -->
         <server>
            <id>able.maven.repository</id>
            <username>maven</username>
            <password>maven3repo</password>
        </server>

       </servers>
</settings>

mvn compile - will compile everything
mvn install - will build the jar file and copy all dependent jar files into the targets/ directory
mvn deploy - will copy the jar file to the maven repository so that others can access it