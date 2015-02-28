Brikar Sample Calc
==================

## How to start

Launch CalcLauncher.main in calc-server

## How to verify

Run sample client (``CalcClient`` class in ``calc-client`` module):

```
variables = vars {
  varName: "over"
  value: 9000
}
```

Sample (local server assumed to be up and running at port 8080):

JSON:

```
curl -H 'Accept: application/json; charset=UTF-8' 127.0.0.1:8080/rest/calc/variable -v -s | python -mjson.tool
{
    "vars": [
        {
            "value": 9000,
            "varName": "over"
        }
    ]
}
```

Protobuf:

```
curl 127.0.0.1:8080/rest/calc/variable -s | protoc --decode_raw --proto_path=$PROJ/brikar/sample/calc/calc-model/src/main/resources/
1 {
  1: "over"
  2: 9000
}
```

Another sample (dumps output as binary):

```
curl 127.0.0.1:8080/rest/calc/variable -s -vv | hexdump
```

This should produce output like that:

```
* About to connect() to 127.0.0.1 port 8080 (#0)
*   Trying 127.0.0.1...
* Adding handle: conn: 0x7f80f400d000
* Adding handle: send: 0
* Adding handle: recv: 0
* Curl_addHandleToPipeline: length: 1
* - Conn 0 (0x7f80f400d000) send_pipe: 1, recv_pipe: 0
* Connected to 127.0.0.1 (127.0.0.1) port 8080 (#0)
> GET /rest/calc/variable HTTP/1.1
> User-Agent: curl/7.30.0
> Host: 127.0.0.1:8080
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Type: application/x-protobuf
< Transfer-Encoding: chunked
<
{ [data not shown]
* Connection #0 to host 127.0.0.1 left intact
0000000 0a 09 0a 04 6f 76 65 72 10 a8 46
000000b
```

Stress testing sample:

```
ab -n 1000 -c 20 http://<<IP>>:<<PORT>>/rest/calc/variable
```
