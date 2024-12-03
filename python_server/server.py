import sys
from concurrent import futures
from typing import Final

import grpc

import helloworld_pb2
import helloworld_pb2_grpc

grpc_max_bytes: Final = 209715200


class Greeter(helloworld_pb2_grpc.GreeterServicer):
    def SayHello(self, request: helloworld_pb2.HelloRequest, context):
        return helloworld_pb2.HelloReply(payload=request.payload)


if __name__ == "__main__":
    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10),
        options=[
            ("grpc.max_send_message_length", grpc_max_bytes),
            ("grpc.max_receive_message_length", grpc_max_bytes),
        ],
    )
    helloworld_pb2_grpc.add_GreeterServicer_to_server(Greeter(), server)
    server.add_insecure_port(sys.argv[1])
    server.start()
    server.wait_for_termination()
