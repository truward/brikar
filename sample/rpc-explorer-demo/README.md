
# How to Launch

1. Run ``mvn exec:java``
1. Run ``curl -s -X POST http://127.0.0.1:8080/api/health`` to see if server is healthy
1. Open ``http://127.0.0.1:8080/api/explorer/UserService`` in the browser
1. (Alternatively) Test using curl (below).

# How to test using curl

Execute:

```
curl -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json;charset=UTF-8' http://127.0.0.1:8080/api/rpc/UserService/getUser -d '{"userId": 10}'
```

Sample Response:

```
{"user":{"id":10,"name":"Sample User","password":"Password"}}
```

Binary data retrieval can be illustrated as follows:

```
$ curl -s -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/x-protobuf' http://127.0.0.1:8080/api/rpc/UserService/getUser -d '{"userId": 10}' | hexdump
0000000 0a 19 08 0a 12 0b 53 61 6d 70 6c 65 20 55 73 65
0000010 72 1a 08 50 61 73 73 77 6f 72 64
000001b
```

Error response illustration:

```
$ curl -s -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json;charset=UTF-8' http://127.0.0.1:8080/api/rpc/UserService/getUser -d '{}' | python -mjson.tool
{
    "code": 400,
    "message": "Illegal Argument: userId=0"
}
```

# RPCv2 Sample

Get User:

```
curl -s -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json' http://127.0.0.1:8080/rpc/api/UserService/getUser -d '{"userId": 10}' | python -mjson.tool
```

Query Users:

```
curl -s -X POST -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json' http://127.0.0.1:8080/rpc/api/UserService/queryUsers -d '{"userIds": [10, 11, 12, 13, 14]}' | python -mjson.tool
```

Apache Bench:

```
echo '{"userIds": [10, 11, 12, 13, 14]}' > /tmp/queryUsersRequest.txt

ab -p /tmp/queryUsersRequest.txt -T application/json -c 1 -n 5 http://127.0.0.1:8080/rpc/api/UserService/queryUsers
```

# Trigger Out-Of-Memory Error

```
curl -s -X POST -H 'Content-Type: application/json;charset=UTF-8' http://127.0.0.1:8080/api/rpc/UserService/triggerOutOfMemoryError -d '{}'
```
