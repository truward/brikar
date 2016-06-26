
# 1.5.25 (Not Yet Released)

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
