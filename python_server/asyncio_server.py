import sys
from concurrent import futures
from typing import Final
import asyncio

import grpc

import helloworld_pb2
import helloworld_pb2_grpc

grpc_max_bytes: Final = 209715200


class Greeter(helloworld_pb2_grpc.GreeterServicer):
    async def SayHello(
        self, request: helloworld_pb2.HelloRequest, context
    ) -> helloworld_pb2.HelloReply:
        return helloworld_pb2.HelloReply(payload=request.payload)


async def run():
    server = grpc.aio.server(
        futures.ThreadPoolExecutor(max_workers=10),
        options=[
            ("grpc.max_send_message_length", grpc_max_bytes),
            ("grpc.max_receive_message_length", grpc_max_bytes),
        ],
    )
    helloworld_pb2_grpc.add_GreeterServicer_to_server(Greeter(), server)
    server.add_insecure_port(sys.argv[1])
    await server.start()
    await server.wait_for_termination()


if __name__ == "__main__":
    asyncio.run(run())
