todo
====

## Ad Hoc Start

### Logging configuration

VM Args:

```
-Dapp.logback.rootLogId=ROLLING_FILE -Dapp.logback.logBaseName=/tmp/todoServer
```

CLI Args:

```
--graceful-shutdown-millis 100
```

## Demo Request

TODO Sample.

```
curl -u todoServiceUser:todoPassword -H 'Accept: application/json' http://127.0.0.1:8080/rest/todo/items -s | python -mjson.tool
```

Results in:

```js
{
    "items": [
        {
            "id": 10,
            "name": "Run Service!"
        }
    ]
}
```

## Quick Start

Prepare properties file:

```
echo "
todoService.auth.demoUser.username=demo-todo-user
todoService.auth.demoUser.password=Par12341wqwe
" > /tmp/todo.properties
```

In todo-server start application:

```
mvn exec:java -Dexec.args="--port 9050 --config file:///tmp/todo.propties"
```

Then invoke test API:

```
curl -u demo-todo-user:Par12341wqwe -H 'Accept: application/json' http://127.0.0.1:9050/rest/todo/items -s | python -mjson.tool
```
