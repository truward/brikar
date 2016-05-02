[![GitHub license](https://img.shields.io/badge/license-Apache%202-green.svg)](https://raw.githubusercontent.com/truward/brikar/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.truward.brikar/brikar.svg)](https://repo1.maven.org/maven2/com/truward/brikar/brikar/)

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
<brikar.version>1.4.23</brikar.version>
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

## Logging and Metrics

Brikar comes with pre-configured logging (slf4j+logback). See also ``default-service-logback.xml``.

Metrics are necessary to capture information on how application processed particular request.
That metric-specific information usually has the following attributes:

* Operation name. This is used to associate the particular place in code with business procedure.
It can be a class and method names combined if such processing is encapsulated in the particular method, or it can
match an exposed REST API URL.
* Time, to do the corresponding data processing, for example how much time it took to process an HTTP request.
* Various extra parameters, such as whether or not operation succeeded, HTTP method name for capturing RESTful operations behavior, etc.

Examples of request-response-related metrics (both client and server):

```
2015-07-28 06:34:05,942 INFO c.t.b.s.t.RequestIdAwareFilter rid=26lrh002OXW/bE, oid=JsonProtocol [qtp1556534733-21] @metric op=/test/exposure/greet, tDelta=9
2015-07-28 06:34:05,942 INFO c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=11, method=POST, responseCode=200, responseRid=26lrh002OXW/bE
2015-07-28 06:34:05,946 INFO c.t.b.s.t.RequestIdAwareFilter rid=n0lz0o0JFHolU7, oid=JsonProtocol [qtp1556534733-15] @metric op=/test/exposure/greet, tDelta=2
2015-07-28 06:34:05,946 WARN c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=3, method=POST, responseCode=400, responseRid=n0lz0o0JFHolU7
2015-07-28 06:34:05,951 INFO c.t.b.s.t.RequestIdAwareFilter rid=FQnDh+T9A2zPKY, oid=JsonProtocol [qtp1556534733-21] @metric op=/test/exposure/greet, tDelta=2
2015-07-28 06:34:05,952 WARN c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=5, method=POST, responseCode=400, responseRid=FQnDh+T9A2zPKY
```

Each line contains specifically formatted metric entry.

For example ``@metric op=/test/exposure/greet, tDelta=9`` line means that service invocation of ``/test/exposure/greet`` took ``9`` milliseconds.
