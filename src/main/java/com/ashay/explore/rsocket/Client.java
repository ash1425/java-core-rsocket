package com.ashay.explore.rsocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.metadata.CompositeMetadataFlyweight;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;

import java.nio.CharBuffer;

import static io.netty.buffer.ByteBufAllocator.DEFAULT;
import static io.rsocket.metadata.WellKnownMimeType.MESSAGE_RSOCKET_ROUTING;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Client {

    public static void main(String[] args) {
        RSocket sendingSocket = RSocketFactory.connect()
                .transport(TcpClientTransport.create(7777))
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
