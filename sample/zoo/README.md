zoo
===

Zoo Sample.

```
curl -u testonly:test -H 'Accept: application/json' http://127.0.0.1:8080/rest/zoo/animal/1 -s | python -mjson.tool
```

Results in:

```js
{
    "id": 1,
    "name": "crow"
}
```

## How to run Apache Bench

```
ab -u testonly:test -n 10000 -c 50 http://127.0.0.1:8080/rest/zoo/animal/1
```