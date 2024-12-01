package com.fandreuz.grpc.uds;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import lombok.experimental.UtilityClass;

@UtilityClass
class Utils {
    static int findPort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
