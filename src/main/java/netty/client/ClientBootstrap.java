package netty.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.TimeUnit;

public class ClientBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(ClientBootstrap.class);
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;
    private static SslContext sslCtx;
    private static Channel channel;

    public static void main(String[] args) throws Exception {
//        sslCtx = Http2Util.createSSLContext(false);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Http2ClientInitializer initializer = new Http2ClientInitializer(sslCtx, Integer.MAX_VALUE, HOST, PORT);

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(HOST, PORT);
            b.handler(initializer);

            channel = b.connect().syncUninterruptibly().channel();

            logger.info("Connected to [" + HOST + ':' + PORT + ']');

            Http2SettingsHandler http2SettingsHandler = initializer.getSettingsHandler();
            http2SettingsHandler.awaitSettings(60, TimeUnit.SECONDS);

            logger.info("Sending request(s)...");

            FullHttpRequest request = Http2Util.createGetRequest(HOST, PORT);

            Http2ClientResponseHandler responseHandler = initializer.getResponseHandler();
            int streamId = 3;

            responseHandler.put(streamId, channel.write(request), channel.newPromise());
            channel.flush();

            String response = responseHandler.awaitResponses(60, TimeUnit.SECONDS);

            System.out.println(response);
            logger.info("Finished HTTP/2 request(s)");
        } finally {
            workerGroup.shutdownGracefully();
        }

    }
}
