package server.tcp;

import com.twitter.hpack.Encoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersEncoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HPackEncoder {

    public static void main(String[] args) throws Exception {
        twitterHpackEncoder();
        System.out.println();
        System.out.println();
        nettyHpackEncoder();
    }

    public static void twitterHpackEncoder() throws Exception {
        int maxHeaderSize = 4096;
        int maxHeaderTableSize = 0;
        boolean sensitive = false;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // encode header list into header block
        Encoder encoder = new Encoder(maxHeaderTableSize);
        encoder.encodeHeader(out, "content-type".getBytes(), "application/json".getBytes(), sensitive);
        encoder.encodeHeader(out, "user-agent".getBytes(), "wso2".getBytes(), sensitive);
//        encoder.encodeHeader(out, "connection".getBytes(), "upgrade".getBytes(), sensitive);
//        encoder.encodeHeader(out, "upgrade".getBytes(), "h2c".getBytes(), sensitive);
        encoder.encodeHeader(out, ":path".getBytes(), "/hello".getBytes(), sensitive);
        encoder.encodeHeader(out, ":method".getBytes(), "GET".getBytes(), sensitive);
        byte[] bArr = out.toByteArray();
        StringBuilder hex = new StringBuilder(bArr.length * 2);
        for (byte b: bArr) {
            hex.append(String.format("%02x", b));
        }
        int j = 0;
        for (int i = 0; i < hex.length(); i = i + 2) {
            System.out.print("(byte) 0x" + hex.substring(i, i + 2) + ", ");
            j = j + 1;
        }
        System.out.print("\n");
        System.out.println(hex);
        System.out.println(j);
    }

    public static void nettyHpackEncoder() throws Exception {
        int maxHeaderSize = 4096;
        int maxHeaderTableSize = 0;
        boolean sensitive = false;

        DefaultHttp2HeadersEncoder encoder = new DefaultHttp2HeadersEncoder((name, value) -> false, true);
        Http2Headers http2Headers = new DefaultHttp2Headers();
        http2Headers.add("content-type", "text/plain");
        http2Headers.add("user-agent", "wso2");
        http2Headers.path("/hello");
        http2Headers.method("POST");

        ByteBuf buf = Unpooled.buffer();

        encoder.encodeHeaders(3, http2Headers, buf);
        int readblebytes = buf.readableBytes();
        byte[] bytes = new byte[readblebytes];
        buf.readBytes(bytes);

//        int j = 0;
//        for (int i = 0; i < readblebytes; i = i + 2) {
//            System.out.print("(byte) 0x" + bytes[i] + ", ");
//            j = j + 1;
//        }
//        System.out.print("\n");
////        System.out.println(hex);
//        System.out.println(j);

        byte[] bytes1 = new byte[]{0, 0, 12, 4, 0, 0, 0, 0, 0, 0, 4, 0, 0, -1, -1, 0, 6, 0, 0, 32, 0, 0, 0, 0, 4, 1, 0, 0, 0, 0};

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes1.length * 2];
        for ( int j = 0; j < bytes1.length; j++ ) {
            int v = bytes1[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String hexVal = new String(hexChars);
        System.out.println(hexVal);
        int j = 0;
        for (int i = 0; i < hexVal.length(); i = i + 2) {
            System.out.print("(byte) 0x" + hexVal.substring(i, i + 2) + ", ");
            j = j + 1;
        }
        System.out.print("\n");
        System.out.println(j);
    }

//    public static void main(String[] args) {
//        addXtoHeaders();
//    }
//
//    public static void addXtoHeaders() {
//        String s = "48 54 54 50 2f 31 2e 31 20 31 30 31 20 53 77 69 74 63 68 69 6e 67 20 50 72 6f 74 6f 63 6f 6c 73 a 63 6f 6e 6e 65 63 74 69 6f 6e 3a 20 75 70 67 72 61 64 65 a 75 70 67 72 61 64 65 3a 20 68 32 63 a";
//        String[] arr = s.split(" ");
//        for (String ss: arr) {
//            System.out.print("(byte) 0x" + ss + ", ");
//        }
//    }

}
