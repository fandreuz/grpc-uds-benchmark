package com.fandreuz.grpc.uds;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ThreadFactory;
import lombok.experimental.UtilityClass;

@UtilityClass
class Utils {
    public ThreadFactory makeDaemonThreadFactory(String label) {
        return new ThreadFactoryBuilder()
                .setNameFormat(label + "-pool-%d")
                .setDaemon(true)
                .build();
    }
}
