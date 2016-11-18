
# 1.7.30 (PLANNED)

(TBD)

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
