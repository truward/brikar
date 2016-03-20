[![GitHub license](https://img.shields.io/badge/license-Apache%202-green.svg)](https://raw.githubusercontent.com/truward/brikar/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.truward.brikar/brikar.svg)](https://repo1.maven.org/maven2/com/truward/brikar/brikar/)

Brikar
======

Framework for exposing microservices using Spring MVC + Google Protobuf + Jackson (JSON) + Jetty (Servlet Container).

It also offers:

* Wiring protobuf-generated domain model with REST API
* Defining RESTful service interface
* Generating a client by using single service interface definition
* Standardized logging
* Emitting metrics to logs
* Tracking request IDs
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
<brikar.version>1.3.19</brikar.version>
<!-- ... skipped ... -->

<dependency>
  <groupId>com.truward.brikar</groupId>
  <artifactId>brikar-client</artifactId>
  <version>${brikar.version}</version>
</dependency>
```

# Notes

## How to Implement Simplest Microservice

1000 foot view:

* First, create a parent pom, say ``hello/pom.xml``
* Create three subprojects:
* One - model definition, say ``hello-model``. This module will contain protocol buffer model definition and service interface contract
* Other - for server, that will implement and expose service contract from ``hello-model``, say ``hello-server``
* Third (optional) - for client definition, that will contain sample client implementation that will access remote server using contract from ``hello-model``

See also samples in ``sample`` folder.

## How Metric Logging Looks Like

Request-response-related (both client and server):

```
2015-07-28 06:34:05,942 INFO c.t.b.s.t.RequestIdAwareFilter rid=26lrh002OXW/bE, oid=JsonProtocol [qtp1556534733-21] @metric op=/test/exposure/greet, tDelta=9
2015-07-28 06:34:05,942 INFO c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=11, method=POST, responseCode=200, responseRid=26lrh002OXW/bE
2015-07-28 06:34:05,946 INFO c.t.b.s.t.RequestIdAwareFilter rid=n0lz0o0JFHolU7, oid=JsonProtocol [qtp1556534733-15] @metric op=/test/exposure/greet, tDelta=2
2015-07-28 06:34:05,946 WARN c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=3, method=POST, responseCode=400, responseRid=n0lz0o0JFHolU7
2015-07-28 06:34:05,951 INFO c.t.b.s.t.RequestIdAwareFilter rid=FQnDh+T9A2zPKY, oid=JsonProtocol [qtp1556534733-21] @metric op=/test/exposure/greet, tDelta=2
2015-07-28 06:34:05,952 WARN c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=5, method=POST, responseCode=400, responseRid=FQnDh+T9A2zPKY
```

Each line contains specifically formatted metric entry.

For example ``@metric op=/test/exposure/greet, tDelta=9`` line means that service invocation of ``/test/exposure/greet`` takes ``9`` milliseconds.
