package server.tcp;

import com.twitter.hpack.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HPackEncoder {

    public static void main(String[] args) throws IOException {
        int maxHeaderSize = 4096;
        int maxHeaderTableSize = 4096;
        boolean sensitive = false;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // encode header list into header block
        Encoder encoder = new Encoder(maxHeaderTableSize);
//        encoder.encodeHeader(out, ":status".getBytes(), "200".getBytes(), sensitive);
//        encoder.encodeHeader(out, "content-type".getBytes(), "text/plain".getBytes(), sensitive);
        encoder.encodeHeader(out, "user-agent".getBytes(), "wso2".getBytes(), sensitive);

        byte[] bArr = out.toByteArray();
        StringBuilder hex = new StringBuilder(bArr.length * 2);
        for (byte b: bArr) {
            hex.append(String.format("%02x", b));
        }
        System.out.println(hex);

    }

}
