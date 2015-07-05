brikar
======

Framework for exposing microservices using Spring MVC + Google Protobuf + Jackson (JSON) + Jetty (Servlet Container)

# Adding to maven project with minimal fuss


Add jar dependency in your pom.xml:

```xml
<!-- in properties: -->
<brikar.version>1.0.11</brikar.version>
<!-- ... skipped ... -->

<dependency>
  <groupId>com.truward.brikar</groupId>
  <artifactId>brikar-client</artifactId>
  <version>${brikar.version}</version>
</dependency>
```

Have fun!

