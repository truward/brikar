
# 1.9.40 (PLANNED)

TBD

# 1.9.39

* Use brikar error model v. 2.0.3

# 1.9.38

* Rename ``roles`` property to ``authorities``.
* Use default authority "USER" for uninitialized ``authorities`` collections.
* Rename ``default-service-security.xml`` to ``devonly-service-security.xml``.
* Use brikar error model v. 2.0.2

# 1.9.37

* Most of the methods in ``RestErrors`` class made non-static and that class was made abstract.
* ``brikar-protobuf`` moved to the other repository with the same name.
* ``brikar-error-response``, ``brikar-error-response-parser`` moved to the other repository ``brikar-errors``.

# 1.8.35

* Introduce more helper logging classes: ``PropagateLapseLoggerAspectBean``
  which enables wiring aspects to arbitrary pointcuts. 

# 1.8.34

* Add message source into ``ErrorV2``.
* Deprecate ``TimeSource`` in brikar-common in favor of similar abstraction from ``utc-time`` library.
* Remove the requirement to have class annotated with ``@Service`` annotation to enable ``StandardLapseLoggerAspect``.

# 1.8.33

* RPCv2 support
  * Add option to use custom servlet (as opposed to spring's DispatcherServlet)
    * Potentially w/o Spring MVC at all
  * Add option to use custom error mapper akin to Jackson
  * Add option to inject HttpServletRequest and HttpServletResponse
* Introducing ErrorResponseV2
* Upgrading slf4j to ver. 1.7.24
* Upgrading logback-classic to ver. 1.2.1
* Upgrading Spring to ver. 4.3.7.RELEASE
* Upgrading Spring Security to ver. 4.2.2.RELEASE
* Upgrading httpcore to ver. 4.4.6
* Upgrading httpclient to ver. 4.5.3
* Upgrading jetty to ver. 8.2.0.v20160908
* Upgrading protobuf to ver. 3.2.0

# 1.8.32

* Avoid adding default message converters in RestTemplate created in RestOperationFactory
* Upgrading protobuf-jackson to ver. 1.0.3

# 1.8.31

* Adding support for chaining metrics
* Upgrading slf4j to ver. 1.7.22
* Upgrading Spring to ver. 4.3.5.RELEASE
* Upgrading Spring Security to ver. 4.2.1.RELEASE
* Upgrading httpcore to ver. 4.4.5
* Upgrading httpclient to ver. 4.5.2
* Upgrading jsr305 (com.google.code.findbugs) to ver. 3.0.1
* Upgrading logback-classic to ver. 1.1.8
* Bugfix: proper @Nonnull/@Nullable handling in StandardRestServiceBinder

# 1.8.30

* Adoping OneAPI error response as described in '7.10.2' chapter in [REST API Guidelines](https://github.com/Microsoft/api-guidelines/blob/master/Guidelines.md)
  * Also adding brikar-error-response-parser module with helper class for parsing error response from spring web client's HttpStatusCodeException
* Making Java 8 as a requirement (primarily to enable use of mixins)
* @metrics renamed to @metric1 to allow future extensions.
* Request ID to Request Vector renaming:
  * RequestIdAwareFilter renamed to RequestVectorAwareFilter
  * Request-ID header has been renamed to RV
  * rid logger name has been renamed to RV

# 1.7.29

* OID and RID headers dropped in favor of single Request-ID header which is introduced for the same purpose.
* Introducing "nesting" for new request IDs that would make building cross-service request graphs trivial.
* Introducing TempConfiguration in brikar-maintenance:
  utility class that can write temporary configuration file for brikar server.
* RequestIdAwareFilter now logs combination of HTTP method + URL pattern as operation name
  instead of full operation to allow tools. So, for example, for HTTP request like 'GET /user/1/order/2' the
  following pattern might be recorded in request logs: 'GET_/user/{userId}/order/{orderId}'
  to build metrics for RESTful operations.
* Aliasing logger in RequestIdAwareFilter as 'BrikarRestLogger' for consistency with REST client.
* Changing default '/rest' prefix to '/api'.
* Migrating to protobuf 3.1.0
* Cosmetic fixes

# 1.6.28

* OID is not set if it is missing;
  brikar-client made smart enough to use RID as OID if OID is missing in the logger context.
* Added brikar-maintenance, helper library for performing testing or maintenance operations with brikar services.
* Added automated integration tests that verify log format and propagation of originating request IDs.

# 1.6.27

* (Major Breaking Change!) Refactoring in brikar-client:
  RestBinder becomes RestClientBuilderFactory; support classes split into multiple ones;
  Also inner entities used in previous RestBinder implementation now available as public classes
* Using 'BrikarRestClient' as a logger name for client HTTP calls
* Extending standard error code with extra parameters
* Fixing the bug in client request log interceptor
* Fixing build under Windows
* Fixing the bug in JettyResourceUtil and making it more generic
* Adding Swagger example
* Cosmetic fixes

# 1.5.26

* Migrating to [protobuf 3.0.0](http://mvnrepository.com/artifact/com.google.protobuf/protobuf-java)
* Migrating to [protobuf-jackson 1.0.2](http://mvnrepository.com/artifact/com.truward.protobuf/protobuf-jackson)

# 1.5.25

* (Major Breaking Change!) ``writeDelimitedTo`` and ``parseDelimitedFrom`` are now used to read and write protobuf
  messages from/to HTTP message bodies.
  Unfortunately this breaks compatibility on the message level, so applications built using this library simply won't be
  compatible with the previous ones on protocol level. However, everything will still be working fine if you use JSON.
  See also comments in ``ProtobufHttpMessageConverter`` to find out more about motivation behind this change.
  Long story short: what was possible in Spring 3 is no longer possible in Spring 4. And yeah, it really sucks!
* Fix NPE in RequestIdAwareFilter
* Disabling extra header protection for REST API in ``default-service-security.xml``.

# 1.4.24

* Upgrading all the dependencies, including Spring and Jetty: both bumped to the latest available versions (as of June 25, 2016)
* ``brikar-rpc`` Adding support for service name setter
* ``brikar-rpc`` Changing default invocation method in RPC controller and corresponding client bindings: use path parameter in favor of 'm' query parameter

# 1.4.23

* First version referenced in release notes!
* Adding custom static path settings.
* Implement working RpcServiceExplorer.
* Dropping ``protobuf-java-util`` and transitive gson/guava dependencies in favor of ``protobuf-jackson``.
* Dropping ``brikar-persistence-util``.
