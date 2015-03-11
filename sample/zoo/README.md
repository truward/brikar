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