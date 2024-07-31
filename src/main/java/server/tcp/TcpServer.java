package server.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

// Use https://onlinestringtools.com/convert-string-to-hexadecimal to convert a string to hexa-decimal to send inside the dataframe

public class TcpServer {

    static AtomicBoolean commDone = new AtomicBoolean(false);

    public static void main(String[] args) {
        int port = 9000;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("HTTP/2 Server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from: " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (InputStream inputStream = clientSocket.getInputStream();
                 OutputStream outputStream = clientSocket.getOutputStream()) {
                startReadingFromInputStream(inputStream);

//                sendRstWhenReceivingHeaders(inputStream, outputStream);
                sendSuccessfulResponse(outputStream);
//                sendHalfResponseAndHold(outputStream);
//                sendSuccessfulResponseWithAConnectionUpgrade(outputStream);
//                sendSuccessfulResponseWithContinueHeaderInAConnectionUpgrade(outputStream);
//                sendGoAway(outputStream);
//                sendGoAwayWhenReceivingHeaders(outputStream);
//                sendSuccessfulResponseToMultipleRequests(inputStream, outputStream);
//                sendSuccessfulResponseToMultipleRequestsAndGoAwayToASingleRequest(outputStream);
//                sendSuccessfulResponseToMultipleRequestsAndGoAwayToASingleRequestWhenReceivingHeaders(outputStream);
                commDone.set(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void startReadingFromInputStream(InputStream inputStream) {
            Thread readerThread = new Thread(new ReadFromInputStream(inputStream));
            readerThread.start();
        }
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
//                String data = new String(buffer, 0, bytesRead);
//                System.out.println("Received data: " + data);
                System.out.println(buffer);
            }
        }
    }

    private static void sendSuccessfulResponse(OutputStream outputStream) throws IOException, InterruptedException {
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        Thread.sleep(1000);

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack

        Thread.sleep(1000);

        System.out.println("Writing headers frame");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        Thread.sleep(1000);

        System.out.println("Writing data frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
    }

    private static void sendHalfResponseAndHold(OutputStream outputStream) throws IOException, InterruptedException {
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        Thread.sleep(1000);

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack

        Thread.sleep(1000);

        System.out.println("Writing headers frame");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x0b});

        Thread.sleep(1000000);

//        System.out.println("Writing data frame");
//        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
    }

    private static void sendSuccessfulResponseWithAConnectionUpgrade(OutputStream outputStream) throws IOException, InterruptedException {
        Thread.sleep(1000);
        System.out.println("Writing 101 Switching protocols header");
        /*
        HTTP/1.1 101 Switching Protocols
        connection: upgrade
        upgrade: h2c

        */ //encode this using https://onlinestringtools.com/convert-string-to-hexadecimal
        outputStream.write(new byte[]{(byte) 0x48, (byte) 0x54, (byte) 0x54, (byte) 0x50, (byte) 0x2f, (byte) 0x31, (byte) 0x2e, (byte) 0x31, (byte) 0x20, (byte) 0x31, (byte) 0x30, (byte) 0x31, (byte) 0x20, (byte) 0x53, (byte) 0x77, (byte) 0x69, (byte) 0x74, (byte) 0x63, (byte) 0x68, (byte) 0x69, (byte) 0x6e, (byte) 0x67, (byte) 0x20, (byte) 0x50, (byte) 0x72, (byte) 0x6f, (byte) 0x74, (byte) 0x6f, (byte) 0x63, (byte) 0x6f, (byte) 0x6c, (byte) 0x73, (byte) 0xa, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x6e, (byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x69, (byte) 0x6f, (byte) 0x6e, (byte) 0x3a, (byte) 0x20, (byte) 0x75, (byte) 0x70, (byte) 0x67, (byte) 0x72, (byte) 0x61, (byte) 0x64, (byte) 0x65, (byte) 0xa, (byte) 0x75, (byte) 0x70, (byte) 0x67, (byte) 0x72, (byte) 0x61, (byte) 0x64, (byte) 0x65, (byte) 0x3a, (byte) 0x20, (byte) 0x68, (byte) 0x32, (byte) 0x63, (byte) 0xa, 0xa});

        Thread.sleep(1000);
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        Thread.sleep(1000);

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});

        Thread.sleep(8000);

        System.out.println("Writing headers frame");
        // Sending headers frame with status 200 to stream 1 (When upgrade happens, first stream is used as 1)
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x01, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        Thread.sleep(1000);

        System.out.println("Writing data frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
    }

    private static void sendSuccessfulResponseWithContinueHeaderInAConnectionUpgrade(OutputStream outputStream) throws IOException, InterruptedException {
        Thread.sleep(1000);
        System.out.println("Writing 101 Switching protocols header");
        outputStream.write(new byte[]{(byte) 0x48, (byte) 0x54, (byte) 0x54, (byte) 0x50, (byte) 0x2f, (byte) 0x31, (byte) 0x2e, (byte) 0x31, (byte) 0x20, (byte) 0x31, (byte) 0x30, (byte) 0x31, (byte) 0x20, (byte) 0x53, (byte) 0x77, (byte) 0x69, (byte) 0x74, (byte) 0x63, (byte) 0x68, (byte) 0x69, (byte) 0x6e, (byte) 0x67, (byte) 0x20, (byte) 0x50, (byte) 0x72, (byte) 0x6f, (byte) 0x74, (byte) 0x6f, (byte) 0x63, (byte) 0x6f, (byte) 0x6c, (byte) 0x73, (byte) 0xa, (byte) 0x63, (byte) 0x6f, (byte) 0x6e, (byte) 0x6e, (byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x69, (byte) 0x6f, (byte) 0x6e, (byte) 0x3a, (byte) 0x20, (byte) 0x75, (byte) 0x70, (byte) 0x67, (byte) 0x72, (byte) 0x61, (byte) 0x64, (byte) 0x65, (byte) 0xa, (byte) 0x75, (byte) 0x70, (byte) 0x67, (byte) 0x72, (byte) 0x61, (byte) 0x64, (byte) 0x65, (byte) 0x3a, (byte) 0x20, (byte) 0x68, (byte) 0x32, (byte) 0x63, (byte) 0xa, 0xa});

        Thread.sleep(1000);
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        Thread.sleep(1000);

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});

        Thread.sleep(1000);

        System.out.println("Writing headers frame");
        // Sending headers frame with status 200 to stream 1 (When upgrade happens, first stream is used as 1)
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

//        System.out.println("Writing a goaway frame");
//        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0b});

        System.out.println("Writing a continuation frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x05, 0x09, 0x04, 0x00, 0x00, 0x00, 0x01, 0x7a, (byte) 0x83, (byte) 0xf0, (byte) 0x83, (byte) 0x8b});

        Thread.sleep(1000);

        System.out.println("Writing data frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});

        Thread.sleep(1000);
    }

    private static void sendGoAway(OutputStream outputStream) throws IOException, InterruptedException {
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack
        Thread.sleep(1000);

//        System.out.println("Writing headers frame");
//        // Sending headers frame with status 200
//        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        Thread.sleep(1000);

//        System.out.println("Writing data frame");
//        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});

        Thread.sleep(1000);

        System.out.println("Writing a goaway frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x0b});

        Thread.sleep(10000);

        System.out.println("Request completed");
    }

    private static void sendSuccessfulResponseToMultipleRequests(OutputStream outputStream) throws IOException, InterruptedException {
        Thread.sleep(1000);
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending settings frame with HEADER_TABLE_SIZE=25700

        Thread.sleep(1000);
        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack

        Thread.sleep(1000);
        System.out.println("Writing headers frame to stream 3");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 3");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x33});

        Thread.sleep(1000);
        System.out.println("Writing headers frame to stream 5");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x05, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        Thread.sleep(1000);
        System.out.println("Writing headers frame to stream 7");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x07, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 7");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x07, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x37});

        System.out.println("Writing data frame to stream 5");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x35});

        Thread.sleep(1000);
        System.out.println("Writing headers frame to stream 9");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x09, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        System.out.println("Writing headers frame to stream 11");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x0b, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        System.out.println("Writing data frame to stream 9");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x09, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x39});

        Thread.sleep(1000);
        System.out.println("Writing data frame to stream 11");
        outputStream.write(new byte[]{0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0b, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x31, 0x31});

        System.out.println("Request completed....");
        Thread.sleep(10000);
    }

    private static void sendSuccessfulResponseToMultipleRequestsAndGoAwayToASingleRequestWhenSendingDataFrame(OutputStream outputStream) throws IOException, InterruptedException {
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending settings frame with HEADER_TABLE_SIZE=25700

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack
        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 3");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 5");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x05, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 5");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x35});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 11");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x0b, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 11");
        outputStream.write(new byte[]{0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0b, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x31, 0x31});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 9");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x09, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing a go away frame to stream 9");
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x0b});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 7");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x07, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 7");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x07, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x37});
        System.out.println("Request completed....");

        Thread.sleep(1000);
        System.out.println("Writing data frame to stream 3");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x33});
    }

    private static void sendGoAwayWhenReceivingHeaders(OutputStream outputStream) throws IOException, InterruptedException {
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack

        Thread.sleep(1000);

        System.out.println("Writing a goaway frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x0b});

        // Sending a continuation frame to continue headers
        System.out.println("Writing continuation header frame");
//        outputStream.write(new byte[]{0x00, 0x00, 0x05, 0x09, 0x04, 0x00, 0x00, 0x00, 0x03, 0x7a, (byte) 0x83, (byte) 0xf0, (byte) 0x83, (byte) 0x8b});

        System.out.println("Writing data frame");
//        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});

        Thread.sleep(10000);

        System.out.println("Request completed");
    }

    private static void sendSuccessfulResponseToMultipleRequestsAndGoAwayToASingleRequestWhenReceivingHeaders(OutputStream outputStream) throws IOException, InterruptedException {
        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending settings frame with HEADER_TABLE_SIZE=25700

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack
        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 3");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 5");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x05, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        System.out.println("Writing data frame to stream 5");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x35});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 11");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x0b, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 11");
        outputStream.write(new byte[]{0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0b, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x31, 0x31});

        Thread.sleep(1000);

        System.out.println("Writing a go away frame to stream 9");
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x0b});

        System.out.println("Writing headers frame to stream 9");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x09, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 9");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x09, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x39});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 7");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x07, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 7");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x07, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x37});

        Thread.sleep(1000);

        System.out.println("Writing headers frame to stream 13");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x0d, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 13");
        outputStream.write(new byte[]{0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0d, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x31, 0x33});

        System.out.println("Writing headers frame to stream 15");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x0f, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        System.out.println("Writing data frame to stream 15");
        outputStream.write(new byte[]{0x00, 0x00, 0x0c, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0f, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x31, 0x35});

        Thread.sleep(1000);

        System.out.println("Writing data frame to stream 3");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x33});

        Thread.sleep(10000);
        System.out.println("Request completed....");
    }

    private static void sendGoAwayWhenSendingHeaders(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        String data = new String(buffer, 0, bytesRead);
        System.out.println("Received data: " + data);

        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        bytesRead = inputStream.read(buffer);
        data = new String(buffer, 0, bytesRead);
        System.out.println("Received data: " + data);

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack

        System.out.println("Writing headers frame");
        // Sending headers frame with status 200 and end-stream 0, end-headers 0
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x0b});

        // Sending a continuation frame to continue headers
//        outputStream.write(new byte[]{0x00, 0x00, 0x05, 0x09, 0x04, 0x00, 0x00, 0x00, 0x03, 0x7a, (byte) 0x83, (byte) 0xf0, (byte) 0x83, (byte) 0x8b});
        System.out.println("Request completed");
    }

    private static void sendGoAwayAfterSendingHeaders(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        String data = new String(buffer, 0, bytesRead);
        System.out.println("Received data: " + data);

        System.out.println("Writing settings frame with header table size");
        outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700

        bytesRead = inputStream.read(buffer);
        data = new String(buffer, 0, bytesRead);
        System.out.println("Received data: " + data);

        System.out.println("Writing settings frame with ack");
        outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack

        System.out.println("Writing headers frame");
        // Sending headers frame with status 200
        outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});

        System.out.println("Writing data frame");
        outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
        // Sending GoAway frame
        outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x0b});

        bytesRead = inputStream.read(buffer);
        data = new String(buffer, 0, bytesRead);
        System.out.println("Received data: " + data);
        System.out.println("Request completed....");
    }

    private static void sendRstWhenReceivingHeaders(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            String data = new String(buffer, 0, bytesRead);
            System.out.println("Received data: " + data);
            // Sending a settings frame first to initiate a connection
            outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700
            // Sending a RstFrame in the middle of reading headers (last stream id - 5, length - 8, error_code - 11, type - 7)
            outputStream.write(new byte[]{0x00, 0x00, 0x04, 0x03, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x02});
            break;
        }
        System.out.println("Request completed....");
    }

//    static class ClientHandlerWithGoAwayFrames implements Runnable {
//        private final Socket clientSocket;
//
//        public ClientHandlerWithGoAwayFrames(Socket clientSocket) {
//            this.clientSocket = clientSocket;
//        }
//
//        @Override
//        public void run() {
//            try (InputStream inputStream = clientSocket.getInputStream();
//                 OutputStream outputStream = clientSocket.getOutputStream()) {
//
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//
//                    // Sending a settings frame first to initiate a connection
//                    outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});
//                    // Sending a GoAwayFrame in the middle of reading headers (last stream id - 5, length - 8, error_code - 11, type - 7)
//                    outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x0b});
//                    break;
//                }
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    static class ClientHandlerWithHttpNativeSuccess implements Runnable {
//        private final Socket clientSocket;
//
//        public ClientHandlerWithHttpNativeSuccess(Socket clientSocket) {
//            this.clientSocket = clientSocket;
//        }
//
//        @Override
//        public void run() {
//            try (InputStream inputStream = clientSocket.getInputStream();
//                 OutputStream outputStream = clientSocket.getOutputStream()) {
//
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                        break;
//                }
//                System.out.println("Writing settings frame with header table size");
//
//                outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700
//                System.out.println("Done");
//
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//                System.out.println("Writing settings frame with ack");
//                outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack
//                System.out.println("Done");
//                System.out.println("Writing headers frame");
////                outputStream.write(new byte[]{0x00, 0x00, 0x1a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x86, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa, 0x76, (byte) 0x8d, (byte) 0x8c, 0x74, 0x50, 0x5b, 0x0d, 0x50, (byte) 0xd8, 0x10, (byte) 0x80, 0x15, (byte) 0xde, 0x5c, 0x1f});
//                outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
//                outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
//                System.out.println("Done");
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//                System.out.println("Writing data frame");
//                outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});// Sending Data frame
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//                System.out.println("Request completed....");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    static class ClientHandlerWithHttpNativeGoAwayCase implements Runnable {
//        private final Socket clientSocket;
//
//        public ClientHandlerWithHttpNativeGoAwayCase(Socket clientSocket) {
//            this.clientSocket = clientSocket;
//        }
//
//        @Override
//        public void run() {
//            try (InputStream inputStream = clientSocket.getInputStream();
//                 OutputStream outputStream = clientSocket.getOutputStream()) {
//
//                byte[] buffer = new byte[4096];
//                int bytesRead;
//
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    // Sending a settings frame first to initiate a connection
//                    // Sending a GoAwayFrame in the middle of reading headers (last stream id - 5, length - 8, error_code - 11, type - 7)
//                    outputStream.write(new byte[]{0x00, 0x00, 0x08, 0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x0b});
//                    break;
//                }
//                System.out.println("Writing settings frame with header table size");
//
//                outputStream.write(new byte[]{0x00, 0x00, 0x06, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x64, 0x64});// Sending setting frame with HEADER_TABLE_SIZE=25700
//                System.out.println("Done");
//
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//                System.out.println("Writing settings frame with ack");
//                outputStream.write(new byte[]{0x00, 0x00, 0x00, 0x04, 0x01, 0x00, 0x00, 0x00, 0x00});// Sending settings frame with ack
//                System.out.println("Done");
//                System.out.println("Writing headers frame");
////                outputStream.write(new byte[]{0x00, 0x00, 0x1a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x86, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa, 0x76, (byte) 0x8d, (byte) 0x8c, 0x74, 0x50, 0x5b, 0x0d, 0x50, (byte) 0xd8, 0x10, (byte) 0x80, 0x15, (byte) 0xde, 0x5c, 0x1f});
//                outputStream.write(new byte[]{0x00, 0x00, 0x0a, 0x01, 0x04, 0x00, 0x00, 0x00, 0x03, (byte) 0x88, 0x5f, (byte) 0x87, 0x49, 0x7c, (byte) 0xa5, (byte) 0x8a, (byte) 0xe8, 0x19, (byte) 0xaa});
//                outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});
//                System.out.println("Done");
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//                System.out.println("Writing data frame");
//                outputStream.write(new byte[]{0x00, 0x00, 0x0b, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64});// Sending Data frame
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    String data = new String(buffer, 0, bytesRead);
//                    System.out.println("Received data: " + data);
//                    break;
//                }
//                System.out.println("Request completed....");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
