package com.fandreuz.grpc.uds.benchmark;

import com.fandreuz.grpc.uds.ClientUtils;
import com.fandreuz.grpc.uds.Server;
import com.fandreuz.grpc.uds.Transport;
import com.fandreuz.grpc.uds.benchmark.echo.helloworld.GreeterGrpc;
import com.fandreuz.grpc.uds.benchmark.echo.helloworld.HelloRequest;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.Random;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class JavaServerUnaryRpcBenchmark {

    @Param({"1", "1024", "1048576", Server.GRPC_MESSAGE_SIZE_LIMIT_BYTES})
    private int bytesCount;

    @Param({"UDS", "TCP_IP"})
    private Transport transport;

    private GreeterGrpc.GreeterBlockingStub stub;
    private Process process;
    private HelloRequest request;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        var channelWithToken = ClientUtils.makeChannel(transport);

        process = new ProcessBuilder()
                .command("app/build/scripts/app", transport.name(), String.valueOf(channelWithToken.token()))
                .inheritIO()
                .redirectErrorStream(true)
                .start();

        stub = GreeterGrpc.newBlockingStub(channelWithToken.channel()).withWaitForReady();

        if (bytesCount == Server.GRPC_MESSAGE_SIZE_LIMIT_BYTES_INT) {
            bytesCount -= 5;
        }
        var byteArray = new byte[bytesCount];
        new Random().nextBytes(byteArray);
        var payload = ByteString.copyFrom(byteArray);
        request = HelloRequest.newBuilder().setPayload(payload).build();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Fork(value = 3, warmups = 2)
    public void init(Blackhole blackhole) {
        blackhole.consume(stub.sayHello(request));
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        process.destroyForcibly();
    }
}
