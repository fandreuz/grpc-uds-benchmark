# Python gRPC server

Simple Python gRPC server based on
[helloworld.proto](../app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo/helloworld.proto)

## Getting started

```bash
python -m venv venv
source venv/bin/activate
python -m pip install --requirement requirements.txt
```

## Compile `.proto` files 

```bash
python -m grpc_tools.protoc \
    -I../app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo \
    --python_out=. \
    --pyi_out=. \
    --grpc_python_out=. \
    ../app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo/helloworld.proto
```
