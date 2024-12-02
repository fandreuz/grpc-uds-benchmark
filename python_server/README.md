## Compile `.proto` files 

python -m grpc_tools.protoc \
    -I../app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo \
    --python_out=. \
    --pyi_out=. \
    --grpc_python_out=. \
    ../app/src/main/proto/com/fandreuz/grpc/uds/benchmark/echo/helloworld.proto
