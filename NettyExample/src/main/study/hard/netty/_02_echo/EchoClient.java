package study.hard.netty._02_echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public final class EchoClient {
	public static void main(String[] args) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		
		try {
			Bootstrap b = new Bootstrap();
			b.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new EchoClientHandler());
					}
				});
			
			ChannelFuture f = b.connect("localhost", 8888).sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
}
