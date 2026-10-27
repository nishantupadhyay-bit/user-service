
package com.assignment.userservice.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

@Configuration
public class GrpcServerConfiguration {

    private static final Logger logger =
            LoggerFactory.getLogger(GrpcServerConfiguration.class);

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    private Server server;

    @Bean
    public Server grpcServer(UserGrpcService userGrpcService) throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(userGrpcService)
                .addService(ProtoReflectionService.newInstance())
                .build()
                .start();

        logger.info("gRPC server started on port {}", grpcPort);

        return server;
    }

    @PreDestroy
    public void stopServer() {
        if (server != null) {
            server.shutdown();
            logger.info("gRPC server stopped");
        }
    }
}
