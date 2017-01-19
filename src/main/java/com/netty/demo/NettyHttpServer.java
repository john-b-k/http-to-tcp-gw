package com.netty.demo;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyHttpServer {
	public static final int PORT = 8080;
	public static void main(String[] args) throws Exception {
		//Server Config
		EventLoopGroup parentGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap boot = new ServerBootstrap();
			boot.option(ChannelOption.SO_BACKLOG, 1024);
			boot.group(parentGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new HttpServerInitializer());
			
			Channel channel = boot.bind(PORT).sync().channel();
			
			channel.closeFuture().sync();
		} finally {
			
			
			
		}
	}
}

class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// TODO Auto-generated method stub
		ChannelPipeline pipe = ch.pipeline();
		pipe.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192, false));
		pipe.addLast("aggregator", new HttpObjectAggregator(100 * 1024 * 1024));
		pipe.addLast("encoder", new HttpResponseEncoder());
		pipe.addLast(new MyHttpServerHandler());
	}
	
}

class MyHttpServerHandler extends ChannelHandlerAdapter {
	@Override
	public void channelReadComplete(ChannelHandlerContext context) {
		System.out.println("Http Channel Read");
		context.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable error) {
		error.printStackTrace();
		context.close();
	}
	
	@Override
	public void channelRead(final ChannelHandlerContext httpServerContext, Object message) {
		FullHttpRequest req = (FullHttpRequest) message;
		String dataFromUser = req.content().toString(Charset.defaultCharset());
		System.out.println("HTTP Server Recieve Data From User : "+dataFromUser);
	
		NettyTcpClient client = new NettyTcpClient();
		client.call(req, new TcpSendCallback(){

			@Override
			public void onSuccess(String responseFromVan) {
				// TODO Auto-generated method stub
				FullHttpResponse response = new DefaultFullHttpResponse(
						HttpVersion.HTTP_1_1,
						HttpResponseStatus.OK, 
						Unpooled.wrappedBuffer(responseFromVan.getBytes()),
						false);
				
				response.headers().set(new AsciiString("content-type"), "text/plain");
				response.headers().set(new AsciiString("custom-header"), "doyoon");
				
				
				System.out.println("Http Response Data Writing start");
				//We can see data in Browser for this code.
				httpServerContext.write(response).addListener(ChannelFutureListener.CLOSE);
			//	httpServerContext.flush();
				System.out.println("Http Response Data Writing end");
			}
		});
		
		
		/*
		FullHttpResponse response = new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK, 
				Unpooled.wrappedBuffer(dataFromUser.getBytes()),
				false);
		
		response.headers().set(new AsciiString("content-type"), "text/plain");
		response.headers().set(new AsciiString("custom-header"), "doyoon");
		
		httpServerContext.write(response).addListener(ChannelFutureListener.CLOSE);
		*/
		//context.flush();
	}
	
	public static interface TcpSendCallback {
		public void onSuccess(String message);
	}
}