package com.ashay.explore.rsocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.metadata.CompositeMetadataFlyweight;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.InputStream;
import java.nio.CharBuffer;

import static io.netty.buffer.ByteBufAllocator.DEFAULT;
import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_ROUTING;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Client {

    public static void main(String[] args) throws SSLException {

        SslContext sslContext = SslContext.newClientContext(new File("/Users/ashayt/Documents/mydev/rsocket/rsocket-core/src/main/resources/localhost.crt"));
        TcpClient tcpClient = TcpClient.create()
                .host("localhost")
                .port(7777)
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

        RSocket sendingSocket = RSocketFactory.connect()
                .transport(TcpClientTransport.create(tcpClient))
                .start()
                .block();

        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
        CompositeMetadataFlyweight.encodeAndAddMetadata(compositeByteBuf, DEFAULT, MESSAGE_RSOCKET_ROUTING, byteBufFromString("Try"));
        Payload response = sendingSocket.requestResponse(DefaultPayload.create(byteBufFromString("lets try"), compositeByteBuf))
                .block();

        System.out.println(response.getDataUtf8());
    }

    private static ByteBuf byteBufFromString(String s) {
        return ByteBufUtil.encodeString(DEFAULT, CharBuffer.wrap(s), UTF_8);
    }
}
