# webwallet-core

This is the Iridium webwallet server implementation (the core). The webwallet is a distributed system consisting of various microservices. So to run the whole stack you need the following services as well:

- [webwallet-discovery](https://github.com/iridiumdev/webwallet-discovery)
- [webwallet-satellite](https://github.com/iridiumdev/webwallet-satellite)

## server

### Dev requirements
- Java 9 (jdk-1.9)
- Annotation processing enabled, see here for [IntellIJ](https://stackoverflow.com/questions/44452482/enable-annotation-processors-by-default)
- docker-compose v1.21.0+

### Running the server
**NOTE**: Make sure that you have the discovery and at least one instance of a satellite running before you start the server.

Run the [ServerApplication.java](server/src/main/java/cash/ird/webwallet/server/ServerApplication.java) class to start the server.



