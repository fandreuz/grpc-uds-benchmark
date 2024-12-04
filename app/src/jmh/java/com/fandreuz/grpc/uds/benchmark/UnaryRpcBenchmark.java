package com.fandreuz.grpc.uds.benchmark;

import com.fandreuz.grpc.uds.ClientUtils;
import com.fandreuz.grpc.uds.Server;
import com.fandreuz.grpc.uds.ServerUtils;
import com.fandreuz.grpc.uds.Transport;
import com.fandreuz.grpc.uds.benchmark.echo.helloworld.GreeterGrpc;
import com.fandreuz.grpc.uds.benchmark.echo.helloworld.HelloRequest;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.List;
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
public class UnaryRpcBenchmark {

    private static final String JAVA_SERVER = "Java";
    private static final String PYTHON_SERVER = "Python";
    private static final String PYTHON_ASYNCIO_SERVER = "PythonAsyncIO";

    @Param({"1", "1024", "1048576", Server.GRPC_MESSAGE_SIZE_LIMIT_BYTES})
    private int bytesCount;

    @Param({"UDS", "TCP_IP"})
    private Transport transport;

    @Param({JAVA_SERVER, PYTHON_SERVER, PYTHON_ASYNCIO_SERVER})
    private String serverType;

    private GreeterGrpc.GreeterBlockingStub stub;
    private Process process;
    private HelloRequest request;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        var channelWithToken = ClientUtils.makeChannel(transport);

        String target = ServerUtils.makeTarget(transport, channelWithToken.token());
        List<String> command =
                switch (serverType) {
                    case JAVA_SERVER -> List.of(
                            "app/bin/app", transport.name(), String.valueOf(channelWithToken.token()));
                    case PYTHON_SERVER -> makePythonScriptTrigger("server.py", target);
                    case PYTHON_ASYNCIO_SERVER -> makePythonScriptTrigger("asyncio_server.py", target);
                    default -> throw new IllegalArgumentException("Unexpected server type: " + serverType);
                };
        process = new ProcessBuilder()
                .command(command)
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

    private static List<String> makePythonScriptTrigger(String scriptName, String target) {
        return List.of(
                "bash",
                "--noprofile",
                "--norc",
                "-c",
                String.format("cd python_server; source venv/bin/activate; python %s %s", scriptName, target));
    }
}
