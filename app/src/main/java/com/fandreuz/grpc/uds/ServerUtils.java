package com.fandreuz.grpc.uds;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.unix.DomainSocketAddress;
import java.nio.file.Path;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerUtils {

    public ServerBuilder<?> makeServerBuilder(Transport transport, String serializedToken) {
        return switch (transport) {
            case TCP_IP -> {
                var port = Integer.parseInt(serializedToken);
                yield Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create());
            }
            case UDS -> {
                var bossThreadFactory = new ThreadFactoryBuilder()
                        .setNameFormat("boss-pool-%d")
                        .setDaemon(true)
                        .build();
                var workThreadFactory = new ThreadFactoryBuilder()
                        .setNameFormat("work-pool-%d")
                        .setDaemon(true)
                        .build();
                yield NettyServerBuilder.forAddress(
                                new DomainSocketAddress(Path.of(serializedToken).toFile()))
                        .bossEventLoopGroup(new EpollEventLoopGroup(5, bossThreadFactory))
                        .workerEventLoopGroup(new EpollEventLoopGroup(10, workThreadFactory))
                        .channelType(EpollServerDomainSocketChannel.class);
            }
        };
    }
}
