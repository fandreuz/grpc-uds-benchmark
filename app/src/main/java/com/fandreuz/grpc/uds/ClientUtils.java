package com.fandreuz.grpc.uds;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientUtils {
    public ChannelWithToken makeChannel(Transport transport) {
        return switch (transport) {
            case TCP_IP -> {
                var port = findPort();
                var channel = Grpc.newChannelBuilderForAddress("localhost", port, InsecureChannelCredentials.create())
                        .maxInboundMessageSize(Server.GRPC_MESSAGE_SIZE_LIMIT_BYTES_INT)
                        .build();
                yield new ChannelWithToken(channel, port);
            }
            case UDS -> {
                var socket = Path.of(System.getProperty("java.io.tmpdir"))
                        .resolve(UUID.randomUUID().toString());

                var channel = NettyChannelBuilder.forAddress(
                                new DomainSocketAddress(socket.toFile()), InsecureChannelCredentials.create())
                        .eventLoopGroup(new EpollEventLoopGroup(10, Utils.makeDaemonThreadFactory("client")))
                        .channelType(EpollDomainSocketChannel.class)
                        .maxInboundMessageSize(Server.GRPC_MESSAGE_SIZE_LIMIT_BYTES_INT)
                        .withOption(ChannelOption.SO_KEEPALIVE, false)
                        .build();
                yield new ChannelWithToken(channel, socket);
            }
        };
    }

    private static int findPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record ChannelWithToken(ManagedChannel channel, Object token) {}
}
