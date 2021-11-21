package ru.rompet.cloudstorage.server.domain.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.rompet.cloudstorage.common.transfer.Response;

import java.util.List;

public class JsonEncoder extends MessageToMessageEncoder<Response> {
    ObjectMapper om = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Response response, List<Object> out) throws Exception {
        byte[] bytes = om.writeValueAsBytes(response);
        out.add(bytes);
    }
}
