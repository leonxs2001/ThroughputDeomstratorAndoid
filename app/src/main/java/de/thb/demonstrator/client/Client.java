package de.thb.demonstrator.client;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;


public class Client {

    public static final int DEFAULT_BUFFER_SIZE = 1024;

    private Socket socket;

    public Client(){
        socket = null;
    }

    public boolean isClientRunning(){
        return socket != null;
    }

    public void stopClient(){
        if(isClientRunning()){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ClientLoadingResult startClient(SendingType sendingType, int bufferSize, int dataSize, String host, int port, String filePath) {
        return startClient(sendingType, bufferSize, dataSize, host, port, filePath, null);
    }

    public ClientLoadingResult startClient(SendingType sendingType, int bufferSize, int dataSize, String host, int port, LoadObserverInterface loadObserver) {
        return startClient(sendingType, bufferSize, dataSize, host, port, null, loadObserver);
    }

    public ClientLoadingResult startClient(SendingType sendingType, int bufferSize, int dataSize, String host, int port) {
        return startClient(sendingType, bufferSize, dataSize, host, port, null, null);
    }

    public ClientLoadingResult startClient(SendingType sendingType, int bufferSize, int dataSize, String host, int port, String filePath, LoadObserverInterface loadObserver) {
        try (Socket socket = new Socket(host, port)) {
            this.socket = socket;
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write(sendingType.toString().getBytes());
            int readySignal = in.read();

            if (readySignal == 1) {
                ClientLoadingResult clientLoadingResult;
                if (sendingType == SendingType.DUMMY) {

                    clientLoadingResult = receiveDummyData(out, bufferSize, dataSize, in, loadObserver);

                } else {
                    if(filePath != null && !filePath.isEmpty()) {
                        clientLoadingResult = receiveFile(out, bufferSize, in, filePath, loadObserver);
                    }else{
                        clientLoadingResult = new ClientLoadingResult(0);
                    }
                }
                return clientLoadingResult;
            } else {
                System.out.println("Cannot start sending data. Server is not ready");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.socket = null;
        return new ClientLoadingResult(0);
    }

    private ClientLoadingResult receiveFile(OutputStream out, int bufferSize, InputStream in, String filePath, LoadObserverInterface loadObserver) throws IOException {
        out.write(Integer.toString(bufferSize).getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        char[] fileInfoChars = new char[DEFAULT_BUFFER_SIZE];
        int numberOfChars = reader.read(fileInfoChars);
        String[] fileInfo = String.valueOf(Arrays.copyOfRange(fileInfoChars, 0, numberOfChars)).split(";");
        String filename = fileInfo[0];
        int filesize = Integer.parseInt(fileInfo[1]);

        out.write(1);

        float numberOfIterations = (float) filesize / bufferSize;
        if (!(filePath.endsWith("/"))) {
            filePath += "/";
        }
        String fullFilePath = filePath + filename;
        try (FileOutputStream fileOut = new FileOutputStream(fullFilePath)) {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < numberOfIterations; i++) {
                byte[] buffer = new byte[bufferSize];
                int bytesRead = in.read(buffer);
                if (bytesRead == -1) break;
                fileOut.write(buffer, 0, bytesRead);
                double percent = ((i + 1) / (double) numberOfIterations) * 100;
                if (loadObserver != null) loadObserver.update(percent);
            }

            float difference = numberOfIterations - (int) numberOfIterations;

            if (difference != 0) {
                byte[] buffer = new byte[(int) (difference * bufferSize)];
                int bytesRead = in.read(buffer);
                if (bytesRead != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
                if (loadObserver != null) loadObserver.update(100);
            }

            long endTime = System.currentTimeMillis();
            return new ClientLoadingResult(endTime - startTime, fullFilePath, filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ClientLoadingResult receiveDummyData(OutputStream out, int bufferSize, int dataSize, InputStream in, LoadObserverInterface loadObserver) throws IOException {
        out.write((bufferSize + ";" + dataSize).getBytes());

        long startTime = System.currentTimeMillis();

        float numberOfIterations = (float) dataSize / bufferSize;
        for (int i = 0; i < numberOfIterations; i++) {
            byte[] buffer = new byte[bufferSize];
            in.read(buffer);
            double percent = ((i + 1) / (double) numberOfIterations) * 100;
            if (loadObserver != null) loadObserver.update(percent);
        }

        float difference = numberOfIterations - (int) numberOfIterations;

        if (difference != 0) {
            byte[] buffer = new byte[(int) (difference * bufferSize)];
            in.read(buffer);
            if (loadObserver != null) loadObserver.update(100);
        }
        long endTime = System.currentTimeMillis();
        return new ClientLoadingResult(endTime - startTime);
    }
}
