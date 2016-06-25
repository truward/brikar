
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
