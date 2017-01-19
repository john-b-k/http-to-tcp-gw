package com.netty.demo;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class VanTcpMockServer {
	public static final int PORT = 8880;
	
	public static void main(String[] args ) throws Exception {
		EventLoopGroup parentGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.group(parentGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						ChannelPipeline pipe = ch.pipeline();
						pipe.addLast(new VanEchoHandler());
					}
					
				});
			
			ChannelFuture future = boot.bind(PORT).sync();
			future.channel().closeFuture().sync();
				
		} finally {
			workerGroup.shutdownGracefully();
			parentGroup.shutdownGracefully();
		}
	}
	
	
	
}

class VanEchoHandler extends ChannelHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext context, Object message) {
		String data = ((ByteBuf) message).toString(Charset.defaultCharset());
		
		System.out.println("\n\ndata for log : "+data);
		
		context.write(message);
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext context){
		context.flush();
	}
}