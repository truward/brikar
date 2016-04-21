
# How to Launch

1. Run ``mvn exec:java``
1. Open ``http://127.0.0.1:8080/rest/UserService/explorer`` in the browser
1. (Alternatively) Test using curl (below).

# How to test using curl

Execute:

```
curl -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json;charset=UTF-8' http://127.0.0.1:8080/rest/UserService?m=getUser -d '{"userId": 10}'
```

Sample Response:

```
{"user":{"id":10,"name":"Sample User","password":"Password"}}
```
