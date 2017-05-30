Beerweb
=======

A simple application that is used for registering ratings for pumps at a beer festival

Building
--------

The project is designed to load into an openshift (v2) wildfly 10 cartridge however, it can be build for running locally using the following [Maven](https://maven.apache.org/) command

    mvn -P package clean verify
    
The war file (beerweb-<version>.war) can be found in the `target` directory
    
Running
-------

The latest version can be found here: [Beerweb Releases](https://github.com/codersparks/beerweb/releases)
    
The server can be started using the following command (obviously with Java 8 (min) installed)

    java -jar -Dspring.profiles.active="local" beerweb-<version>.war
    
Obviously replacing <version> with the downloaded version (or locally built version) To run the server on a different port the `-Dserver.port=<port>` config can be used for example to run on port 9090:

    java -jar -Dspring.profiles.active="local" -Dserver.port=9090 beerweb-<version>.war
    
Note: Only one instance can be running at once (due to database configuration)

