package client;

import server.tcp.TcpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpClient {
    static AtomicBoolean commDone = new AtomicBoolean(false);

    public static void main(String[] args) throws Exception {
        while (true) {
            for (int i = 0; i < 100; i++) {
                new Thread(() -> {
                    try {
                        Socket socket = new Socket("localhost", 8080);
                        OutputStream outputStream = socket.getOutputStream();
                        InputStream inputStream = socket.getInputStream();
                        startReadingFromInputStream(inputStream);
                        sendSuccessfulPriorKnowledgeRequest(outputStream);
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            Thread.sleep(1000);
//            break;
        }
    }

    public static void sendSuccessfulPriorKnowledgeRequest(OutputStream outputStream) throws IOException, InterruptedException {
//        System.out.println("Writing preface frame");
        outputStream.write(new byte[]{0x50, 0x52, 0x49, 0x20, 0x2a, 0x20, 0x48, 0x54, 0x54, 0x50, 0x2f, 0x32, 0x2e, 0x30, 0x0d, 0x0a, 0x0d, 0x0a, 0x53, 0x4d, 0x0d, 0x0a, 0x0d, 0x0a});// Sending setting frame with HEADER_TABLE_SIZE=25700
//        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        Thread.sleep(1000);

//        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});

//        System.out.println("Writing headers frame stream 03");
        // Sending headers frame with status 200
//        outputStream.write(new byte[]{0x00, 0x00, (byte) 0x28, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x5f, (byte) 0x8b, (byte) 0x1d, (byte) 0x75, (byte) 0xd0, (byte) 0x62, (byte) 0x0d, (byte) 0x26, (byte) 0x3d, (byte) 0x4c, (byte) 0x74, (byte) 0x41, (byte) 0xea, (byte) 0x7a, (byte) 0x83, (byte) 0xf0, (byte) 0x83, (byte) 0x8b, (byte) 0x40, (byte) 0x83, (byte) 0xac, (byte) 0x69, (byte) 0x9f, (byte) 0x85, (byte) 0x62, (byte) 0x72, (byte) 0xd1, (byte) 0x41, (byte) 0xff, (byte) 0x40, (byte) 0x85, (byte) 0xa4, (byte) 0xa9, (byte) 0x9c, (byte) 0xf2, (byte) 0x7f, (byte) 0x03, (byte) 0x47, (byte) 0x45, (byte) 0x54});
        outputStream.write(new byte[]{0x00, 0x00, (byte) 0x1b, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x44, (byte) 0x06, (byte) 0x2F, (byte) 0x68, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F, (byte) 0x83, (byte) 0x5F, (byte) 0x0A, (byte) 0x74, (byte) 0x65, (byte) 0x78, (byte) 0x74, (byte) 0x2F, (byte) 0x70, (byte) 0x6C, (byte) 0x61, (byte) 0x69, (byte) 0x6E, (byte) 0x7A, (byte) 0x04, (byte) 0x77, (byte) 0x73, (byte) 0x6F, (byte) 0x32});
        Thread.sleep(1000);

//        System.out.println("Writing data frame stream 03");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
        Thread.sleep(2000);

//        System.out.println("Writing headers frame stream 05");
        // Sending headers frame with status 200
//        outputStream.write(new byte[]{0x00, 0x00, (byte) 0x28, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x5f, (byte) 0x8b, (byte) 0x1d, (byte) 0x75, (byte) 0xd0, (byte) 0x62, (byte) 0x0d, (byte) 0x26, (byte) 0x3d, (byte) 0x4c, (byte) 0x74, (byte) 0x41, (byte) 0xea, (byte) 0x7a, (byte) 0x83, (byte) 0xf0, (byte) 0x83, (byte) 0x8b, (byte) 0x40, (byte) 0x83, (byte) 0xac, (byte) 0x69, (byte) 0x9f, (byte) 0x85, (byte) 0x62, (byte) 0x72, (byte) 0xd1, (byte) 0x41, (byte) 0xff, (byte) 0x40, (byte) 0x85, (byte) 0xa4, (byte) 0xa9, (byte) 0x9c, (byte) 0xf2, (byte) 0x7f, (byte) 0x03, (byte) 0x47, (byte) 0x45, (byte) 0x54});
//        outputStream.write(new byte[]{0x00, 0x00, (byte) 0x1b, 0x01, 0x04, 0x00, 0x00, 0x00, 0x05, (byte) 0x44, (byte) 0x06, (byte) 0x2F, (byte) 0x68, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F, (byte) 0x83, (byte) 0x5F, (byte) 0x0A, (byte) 0x74, (byte) 0x65, (byte) 0x78, (byte) 0x74, (byte) 0x2F, (byte) 0x70, (byte) 0x6C, (byte) 0x61, (byte) 0x69, (byte) 0x6E, (byte) 0x7A, (byte) 0x04, (byte) 0x77, (byte) 0x73, (byte) 0x6F, (byte) 0x32});
//        Thread.sleep(1000);

//        System.out.println("Writing a goaway frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x0b});

//        System.out.println("Writing data frame stream 05");
//        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
//        Thread.sleep(2000);

//        System.out.println("Writing rst frame");
        // Sending a RstFrame in the middle of reading headers (last stream id - 5, length - 8, error_code - 11, type - 7)
//        outputStream.write(new byte[]{0x00, 0x00, 0x04, 0x03, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x02});

//        Thread.sleep(30000);
        commDone.set(true);
        Thread.sleep(200000);
    }

    private static void startReadingFromInputStream(InputStream inputStream) {
        Thread readerThread = new Thread(new ReadFromInputStream(inputStream));
        readerThread.start();
    }

    static class ReadFromInputStream implements Runnable {

        final InputStream inputStream;

        public ReadFromInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            while (!commDone.get()) {
                int bytesRead = 0;
                try {
                    Thread.sleep(1000);
                    bytesRead = inputStream.read(buffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String data = new String(buffer, 0, bytesRead);
                System.out.println("Received data: " + data);
//                System.out.println(buffer);
            }
        }
    }
}