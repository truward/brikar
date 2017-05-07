[![GitHub license](https://img.shields.io/badge/license-Apache%202-green.svg)](https://raw.githubusercontent.com/truward/brikar/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.truward.brikar/brikar.svg)](https://repo1.maven.org/maven2/com/truward/brikar/brikar/)
[![Travis CI](https://travis-ci.org/truward/brikar.svg?style=flat-square)](https://travis-ci.org/truward/brikar)
[![Join the chat at https://gitter.im/truward/brikar](https://badges.gitter.im/truward/brikar.svg)](https://gitter.im/truward/brikar)

Brikar
======

Brikar is a set of non-intrusive libraries for exposing microservices.
It uses existing frameworks and libraries, such as Spring, Google Protocol Buffers, Jetty.

It's primary purpose, similar to [dropwizard](https://github.com/dropwizard) is to combine existing framework into a few,
easy to include and use artifacts with some wiring code to connect them together. Main difference from dropwizard is in using Spring instead of Guice and choice of Protobuf as serialization library.

Brikar also offers:

* Wiring protobuf-generated domain model with REST API
* Defining RESTful service interface
* Generating a client by using single service interface definition
* Standardized logging
* Optional spring security configuration
* Simple service authentication
* Emitting metrics to logs
* Tracking request IDs - associating unique request-specific ID with every message in the logs that correspond
to particular request
* Retries with backoff (for generated clients)

# Assumptions

This framework makes a lot of assumptions about client and server.

* Spring is used to wire dependencies, Spring MVC - for exposing RESTful controllers.
* Embedded jetty is used as servlet container.
* All server applications are written as command line applications with a set of predefined parameters,
e.g. port number, configuration path, stop timeout.
* Protocol buffers and JSON are used for model generation.
* Slf4j + Logback is used as logging facility. Predefined format is defined in brikar-server -
see ``default-service-logback.xml``.
* Log includes specially formatted meta information.
See ``LogUtil.java`` and ``TrackingHttpHeaderNames.java`` for details.

Also includes optional components, such as:

* Basic spring context that propagates environment properties as property placeholder making them visible inside the context.
* A controller, that exposes primitive health check API.
* A controller, that exposes text representation of JVM settings.
* Spring Security developer-only configuration
* REST controller that exposes health check API

# How to use

Add jar dependency in your pom.xml:

```xml
<!-- in properties: -->
<!--
  NOTE: pick up exact version number for non-experimental apps,
        LATEST is fine for experimental ones
-->
<brikar.version>LATEST</brikar.version>
<!-- ... skipped ... -->

<dependency>
  <groupId>com.truward.brikar</groupId>
  <artifactId>brikar-client</artifactId>
  <version>${brikar.version}</version>
</dependency>
```

# Notes

## How to Implement Simplest Microservice Using Brikar

10000 foot view:

* First, create a parent pom, say ``hello/pom.xml``
* Create three subprojects:
* One - model definition, say ``hello-model``. This module will contain protocol buffer model definition and service interface contract.
* Other - for server, that will implement and expose service contract from ``hello-model``, say ``hello-server``
* Third (optional) - for client definition, that will contain sample client implementation that will access remote server using contract from ``hello-model``

See also samples in ``sample`` folder. The structure of samples is identical to the one descibed above.

## Configuration

Brikar makes some assumptions on how application needs to be configured. All the configuration is put into the property
files which are then passed to the application. All the properties are then read from that property files.

Some of those properties are standard, such as port number, stop delay, path to static resources, etc.

Sample configuration:

```
# Tells to use 9090 port
brikar.settings.port=9090

# Tells to wait 2500 milliseconds on shutdown, allowing pending requests to be completed
brikar.settings.gracefulShutdownMillis=2500
```

An application can start using this property file. Assuming, that path to sample configuration file above is ``/opt/hello.properties`` and
service is assembled as jar and put into the current folder under the name ``service.jar``,
it can be started using configuration file given above as ``java -jar service.jar -Dbrikar.settings.path=file:/opt/hello.properties``.

## Logging and Metrics

Brikar comes with pre-configured logging (slf4j+logback). See also ``default-service-logback.xml``.

Metrics are necessary to capture information on how application processed particular request.
That metrics-specific information usually has the following attributes:

* Operation name. This is used to associate the particular place in code with business procedure.
It can be a class and method names combined if such processing is encapsulated in the particular method, or it can
match an exposed REST API URL.
* Time, to do the corresponding data processing, for example how much time it took to process an HTTP request.
* Various extra parameters, such as whether or not operation succeeded, HTTP method name for capturing RESTful operations behavior, etc.

Examples of request-response-related metrics (both client and server):

```
2016-11-18 08:20:45,368 INFO BrikarRequestLogger RV=O1jah9BuYnSbiV [qtp728162039-20] @metric1 op=POST_/api/test/exposure/greet, verb=POST, tDelta=1, responseCode=200, url=/test/exposure/greet
2016-11-18 08:20:45,369 INFO BrikarRestClient  [main] @metric1 op=/api/test/exposure/greet, tDelta=4, verb=POST, responseCode=200, responseRV=O1jah9BuYnSbiV
2016-11-18 08:20:45,377 INFO BrikarRequestLogger RV=7qjJwmY7W/kxf4 [qtp728162039-19] @metric1 op=POST_/api/test/exposure/greet, verb=POST, tDelta=7, responseCode=400, url=/test/exposure/greet
2016-11-18 08:20:45,377 WARN BrikarRestClient  [main] @metric1 op=/api/test/exposure/greet, tDelta=8, verb=POST, responseCode=400, responseRV=7qjJwmY7W/kxf4
```

Each line contains specifically formatted metrics entry.

For example ``@metric1 op=/test/exposure/greet, tDelta=9`` line means that service invocation of ``/test/exposure/greet`` took ``9`` milliseconds.
