package ru.rompet.cloudstorage.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.Response;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<byte[]> {
    ObjectMapper om = new ObjectMapper();
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] msg, List<Object> out) throws Exception {
        Request request = om.readValue(msg, Request.class);
        out.add(request);
    }
}