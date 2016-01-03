package study.hard.netty._02_echo;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		String sendMessage = "Hello, Netty!";
		
		ByteBuf messageBuffer = Unpooled.buffer();
		messageBuffer.writeBytes(sendMessage.getBytes());
		
		StringBuilder builder = new StringBuilder();
		builder.append("Requested string: ");
		builder.append(sendMessage);
		
		System.out.println(builder.toString());
		ctx.writeAndFlush(messageBuffer);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String readMessage = ((ByteBuf)msg).toString(Charset.defaultCharset());
		
		StringBuilder builder = new StringBuilder();
		builder.append("Received string: ");
		builder.append(readMessage);
		System.out.println(builder.toString());
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
	
}
