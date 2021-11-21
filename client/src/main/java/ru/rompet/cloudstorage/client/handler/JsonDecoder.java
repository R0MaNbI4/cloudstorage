package ru.rompet.cloudstorage.client.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ru.rompet.cloudstorage.common.transfer.Response;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<byte[]> {
    ObjectMapper om = new ObjectMapper();
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, byte[] msg, List<Object> out) throws Exception {
        Response response = om.readValue(msg, Response.class);
        out.add(response);
    }
}
