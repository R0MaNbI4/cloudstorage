package ru.rompet.cloudstorage.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.Response;

import java.util.List;

public class JsonEncoder extends MessageToMessageEncoder<Response> {
    ObjectMapper om = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Response response, List<Object> out) throws Exception {
        byte[] bytes = om.writeValueAsBytes(response);
        out.add(bytes);
    }
}
