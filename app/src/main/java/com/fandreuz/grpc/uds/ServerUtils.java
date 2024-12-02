package com.fandreuz.grpc.uds;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerUtils {

    public String makeTarget(Transport transport, Object token) {
        return switch (transport) {
            case TCP_IP -> "localhost:" + token;
            case UDS -> "unix:///" + token;
        };
    }

    public ServerBuilder<?> makeServerBuilder(Transport transport, String serializedToken) {
        return switch (transport) {
            case TCP_IP -> {
                var port = Integer.parseInt(serializedToken);
                yield Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create());
            }
            case UDS -> NettyServerBuilder.forAddress(
                            new DomainSocketAddress(Path.of(serializedToken).toFile()),
                            InsecureServerCredentials.create())
                    .bossEventLoopGroup(new EpollEventLoopGroup(5, Utils.makeDaemonThreadFactory("boss")))
                    .workerEventLoopGroup(new EpollEventLoopGroup(10, Utils.makeDaemonThreadFactory("worker")))
                    .channelType(EpollServerDomainSocketChannel.class);
        };
    }
}
