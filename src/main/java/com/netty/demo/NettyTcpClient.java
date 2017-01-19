package com.netty.demo;

import com.netty.demo.MyHttpServerHandler.TcpSendCallback;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

public class NettyTcpClient {

	public static final int TARGET_PORT = 8880;
	public void call(FullHttpRequest request, TcpSendCallback callback) {
		
		try{
			loadTcpClient(request, callback);
		} catch (Exception e) {
			
		}
	}
	
	public void loadTcpClient(FullHttpRequest request, TcpSendCallback callback) throws InterruptedException {
		EventLoopGroup loop = new NioEventLoopGroup();
		
		try {
			Bootstrap boot = new Bootstrap();
			boot.group(loop)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						ChannelPipeline pipe = ch.pipeline();
						pipe.addLast(new MyTcpClientHandler(request, callback));
					}

				});
			ChannelFuture future = boot.connect("127.0.0.1",TARGET_PORT).sync();
			future.channel().closeFuture().sync();
		} finally {
			
		}
	}
}

class MyTcpClientHandler extends ChannelHandlerAdapter {
	FullHttpRequest request;
	TcpSendCallback callback;
	public MyTcpClientHandler(FullHttpRequest request, TcpSendCallback callback) {
		this.request = request;
		this.callback = callback;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext context) {
		String messageToSend = request.content().toString(CharsetUtil.UTF_8);
		
		ByteBuf messageBuffer = Unpooled.buffer();
		messageBuffer.writeBytes(messageToSend.getBytes());
		
		System.out.println("Send Data To Van via TCP : "+messageToSend);
		//VAN에 TCP로 보내기
		context.writeAndFlush(messageBuffer);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext context, Object message) {
		String dataFromVan = ((ByteBuf) message).toString(CharsetUtil.UTF_8);
		
		System.out.println("Read Data From Van via TCP : "+dataFromVan);
		
		
		//channelReadComplete(context);
		callback.onSuccess(dataFromVan);
		System.out.println("onSuccess finished (response http)");
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext context) {
		context.close();
		System.out.println("TCP client disconnected");
	}
}