package cn.mastercom.nettypush.websocket;

import cn.hutool.json.JSONUtil;
import cn.mastercom.nettypush.config.NettyConfig;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.EOFException;


/**
 * TextWebSocketFrame类型， 表示一个文本帧
 * @author sixiaojie
 * @date 2020-03-28-13:47
 */
@Component
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 一旦连接，第一个被执行
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded 被调用"+ctx.channel().id().asLongText());
        // 添加到channelGroup 通道组
        NettyConfig.getChannelGroup().add(ctx.channel());
    }

    /**
     * 读取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        log.info("服务器收到消息：{}",msg.text());

        // 获取用户ID
//        JSONObject jsonObject = JSONUtil.parseObj(msg.text());
//        String uid = jsonObject.getStr("uid");
        String uid = getUserInfo(msg.text());
        // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        ctx.channel().attr(key).setIfAbsent(uid);

        // 关联channel
        NettyConfig.getUserChannelMap().put(uid,ctx.channel());

        // 回复消息
        ctx.channel().writeAndFlush(new TextWebSocketFrame(writeConnectReponse(uid)));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerRemoved 被调用"+ctx.channel().id().asLongText());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常：{}",cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserId(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     * @param ctx
     */
    private void removeUserId(ChannelHandlerContext ctx){
        AttributeKey<String> key = AttributeKey.valueOf("userId");
        String userId = ctx.channel().attr(key).get();
        NettyConfig.getUserChannelMap().remove(userId);
    }
    /**
     * 获取用户连接信息
     */
    private String getUserInfo(String text) throws EOFException {

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(text);
        com.alibaba.fastjson.JSONObject data = jsonObject.getJSONObject("data");
        if (null == jsonObject) {
            throw new EOFException();
        }
        String clientUser = data.getString("user");
        return clientUser;
    }

    private String writeConnectReponse(String user){
        com.alibaba.fastjson.JSONObject reponse = new com.alibaba.fastjson.JSONObject();
        reponse.put("type","connect");
        reponse.put("result","连接成功");
        reponse.put("code",1);
        com.alibaba.fastjson.JSONObject data = new JSONObject();
        data.put("connection_id",user);
        reponse.put("data",data);
        return reponse.toString();
    }
}
