package com.fandreuz.grpc.uds;

import com.fandreuz.grpc.uds.benchmark.echo.helloworld.GreeterGrpc;
import com.fandreuz.grpc.uds.benchmark.echo.helloworld.HelloReply;
import com.fandreuz.grpc.uds.benchmark.echo.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import java.io.IOException;

public class Server {

    public static final String GRPC_MESSAGE_SIZE_LIMIT_BYTES = "209715200";
    public static final int GRPC_MESSAGE_SIZE_LIMIT_BYTES_INT = Integer.parseInt(GRPC_MESSAGE_SIZE_LIMIT_BYTES);

    public static void main(String[] args) throws InterruptedException, IOException {
        var transport = Transport.valueOf(args[0]);
        io.grpc.Server server = ServerUtils.makeServerBuilder(transport, args[1])
                .addService(new GreeterEcho())
                .maxInboundMessageSize(GRPC_MESSAGE_SIZE_LIMIT_BYTES_INT)
                .build()
                .start();
        server.awaitTermination();
    }

    private static class GreeterEcho extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            var reply = HelloReply.newBuilder() //
                    .setPayload(request.getPayload()) //
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
