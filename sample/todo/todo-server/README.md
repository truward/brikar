todo
====

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
