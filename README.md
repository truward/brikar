brikar
======

Framework for exposing microservices using Spring MVC + Google Protobuf + Jackson (JSON) + Jetty (Servlet Container).
This framework makes a lot of assumptions about client and server.

# Assumptions

* Spring is used to wire dependencies, Spring MVC - for exposing RESTful controllers.
* Embedded jetty is used as servlet container.
* All server applications are written as command line applications with a set of predefined parameters,
e.g. port number, configuration path, stop timeout.
* Protocol buffers and JSON are used for model generation.
* Slf4j + Logback is used as logging facility. Predefined format is defined in brikar-server -
see ``default-service-logback.xml``.
* Log includes specially formatted meta information.
See ``LogUtil.java`` and ``TrackingHttpHeaderNames.java`` for details.

# How to use

Add jar dependency in your pom.xml:

```xml
<!-- in properties: -->
<brikar.version>1.2.17</brikar.version>
<!-- ... skipped ... -->

<dependency>
  <groupId>com.truward.brikar</groupId>
  <artifactId>brikar-client</artifactId>
  <version>${brikar.version}</version>
</dependency>
```

# Examples

## How Logging Looks Like

Request-response-related (both client and server):

```
2015-07-28 06:34:05,942 INFO c.t.b.s.t.RequestIdAwareFilter rid=26lrh002OXW/bE, oid=JsonProtocol [qtp1556534733-21] @metric op=/test/exposure/greet, tDelta=9
2015-07-28 06:34:05,942 INFO c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=11, method=POST, responseCode=200, responseRid=26lrh002OXW/bE
2015-07-28 06:34:05,946 INFO c.t.b.s.t.RequestIdAwareFilter rid=n0lz0o0JFHolU7, oid=JsonProtocol [qtp1556534733-15] @metric op=/test/exposure/greet, tDelta=2
2015-07-28 06:34:05,946 WARN c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=3, method=POST, responseCode=400, responseRid=n0lz0o0JFHolU7
2015-07-28 06:34:05,951 INFO c.t.b.s.t.RequestIdAwareFilter rid=FQnDh+T9A2zPKY, oid=JsonProtocol [qtp1556534733-21] @metric op=/test/exposure/greet, tDelta=2
2015-07-28 06:34:05,952 WARN c.t.b.c.r.s.StandardRestBinder oid=JsonProtocol [main] @metric op=/rest/test/exposure/greet, tDelta=5, method=POST, responseCode=400, responseRid=FQnDh+T9A2zPKY
```
