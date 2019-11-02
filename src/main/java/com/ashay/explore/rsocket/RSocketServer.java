package com.ashay.explore.rsocket;

import io.netty.buffer.ByteBufUtil;
import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.RoutingMetadata;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;

import static io.netty.buffer.ByteBufAllocator.DEFAULT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RSocketServer {
    private final CloseableChannel server;

    public RSocketServer() {
        server = RSocketFactory.receive()
                .acceptor((setup, sendingSocket) -> Mono.just(new RSocketImpl(setup)))
                .transport(TcpServerTransport.create(7777))
                .start()
                .block();
    }

    public static void main(String[] args) throws InterruptedException {
        RSocketServer rSocketServer = new RSocketServer();
        rSocketServer.start();

        Thread.sleep(10000);

        rSocketServer.dispose();
    }

    public void start() {
        Thread thread = new Thread(() -> server.onClose()
                .log()
                .block(), "rsocket-server-await");
        thread.setContextClassLoader(getClass().getClassLoader());
        thread.setDaemon(false);
        thread.start();
    }

    public void dispose() {
        server.dispose();
    }

    private class RSocketImpl extends AbstractRSocket {

        ConnectionSetupPayload connectionSetupPayload;


        public RSocketImpl(ConnectionSetupPayload setup) {
            connectionSetupPayload = setup;
        }

        @Override
        public Mono<Payload> requestResponse(Payload payload) {
            String route = new CompositeMetadata(payload.sliceMetadata(), false).stream()
                    .filter(entry -> WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.toString().equals(entry.getMimeType()))
                    .findFirst()
                    .orElse(new RoutingMetadata(ByteBufUtil.writeUtf8(DEFAULT, "/default")))
                    .getContent()
                    .toString(UTF_8);

            if ("Try".equals(route)) {
                String request = payload.getDataUtf8();
                payload.release();
                return Mono.just(DefaultPayload.create("Echo : " + request)).log();
            } else {
                return Mono.error(new RuntimeException("Route Unknown"));
            }
        }
    }
}
