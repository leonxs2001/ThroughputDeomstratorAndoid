package de.thb.demonstrator.client;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import de.thb.demonstrator.enums.CommunicationType;
import de.thb.demonstrator.enums.SendingType;
import de.thb.demonstrator.exception.ServerNotReadyException;
import kotlin.jvm.Throws;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;


public class Client {

    public static final int DEFAULT_BUFFER_SIZE = 1024;

    private Socket socket;

    public Client() {
        socket = null;
    }

    public boolean isClientRunning() {
        return socket != null;
    }

    public void stopClient() {
        if (isClientRunning()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ClientLoadingResult startClient(CommunicationType communicationType, SendingType sendingType, int bufferSize, int dataSize, String host, int port, String filePath) {
        return startClient(communicationType, sendingType, bufferSize, dataSize, host, port, filePath, null, null, null);
    }

    public ClientLoadingResult startClient(CommunicationType communicationType, SendingType sendingType, int bufferSize, int dataSize, String host, int port, LoadObserverInterface loadObserver) {
        return startClient(communicationType, sendingType, bufferSize, dataSize, host, port, null, null, null, loadObserver);
    }

    public ClientLoadingResult startClient(CommunicationType communicationType, SendingType sendingType, int bufferSize, int dataSize, String host, int port) {
        return startClient(communicationType, sendingType, bufferSize, dataSize, host, port, null, null, null, null);
    }

    public ClientLoadingResult startClient(CommunicationType communicationType, SendingType sendingType, int bufferSize, int dataSize, String host, int port, String filePath, Uri fileUri, Context context, LoadObserverInterface loadObserver) {
        try (Socket socket = new Socket(host, port)) {
            this.socket = socket;
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write((communicationType.toString() + ";" + sendingType.toString()).getBytes());
            int readySignal = in.read();

            if (readySignal == 1) {
                ClientLoadingResult clientLoadingResult;
                if (sendingType == SendingType.DUMMY) {
                    if (communicationType == CommunicationType.DOWNLOAD) {
                        clientLoadingResult = receiveDummyData(out, bufferSize, dataSize, in, loadObserver);
                    } else {
                        clientLoadingResult = sendDummyData(out, bufferSize, dataSize, in, loadObserver);
                    }

                } else {
                    if (communicationType == CommunicationType.DOWNLOAD) {
                        if (filePath != null && !filePath.isEmpty()) {
                            clientLoadingResult = receiveFile(out, bufferSize, in, filePath, loadObserver);
                        } else {
                            clientLoadingResult = new ClientLoadingResult(0);
                        }
                    } else {
                        clientLoadingResult = sendFile(out, bufferSize, dataSize, in, loadObserver, fileUri, context.getContentResolver());
                    }
                }
                return clientLoadingResult;
            } else {
                throw new ServerNotReadyException("Server is not ready");
            }

        } catch (IOException | ServerNotReadyException e) {
            throw new RuntimeException(e);
        } finally {
            this.socket = null;
        }
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

    private static ClientLoadingResult sendDummyData(OutputStream out, int bufferSize, int dataSize, InputStream in, LoadObserverInterface loadObserver) throws IOException, ServerNotReadyException {
        out.write(String.valueOf(bufferSize).getBytes());
        int readySignal = in.read();

        int sendDataSize = dataSize;

        if (readySignal == 1) {
            long startTime = System.currentTimeMillis();
            while (sendDataSize > 0) {
                int sendingSize;
                if (sendDataSize < bufferSize) {
                    sendingSize = sendDataSize;
                    sendDataSize = 0;
                } else {
                    sendingSize = bufferSize;
                    sendDataSize -= bufferSize;
                }
                out.write(new byte[sendingSize]);
                if (loadObserver != null) loadObserver.update((dataSize - sendDataSize) * 100.0 / dataSize);
            }
            long endTime = System.currentTimeMillis();
            return new ClientLoadingResult(endTime - startTime);
        } else {
            throw new ServerNotReadyException("Server is not ready");
        }

    }

    public static ClientLoadingResult sendFile(OutputStream out, int bufferSize, int dataSize, InputStream in, LoadObserverInterface loadObserver, Uri fileUri, ContentResolver contentResolver) throws IOException, ServerNotReadyException {
        out.write(String.valueOf(bufferSize).getBytes());

        String filename = fileUri.getLastPathSegment();
        System.out.println(filename);
        out.write((filename+";").getBytes());

        int readySignal = in.read();

        if (readySignal == 1) {
            long startTime = System.currentTimeMillis();

            // Open FileInputStream for the fileUri
            try (InputStream fileInputStream = contentResolver.openInputStream(fileUri)) {
                if (fileInputStream == null) {
                    throw new IOException("Failed to open InputStream for URI: " + fileUri);
                }

                // Initialize variables for sending data
                long sendDataSize = dataSize;
                byte[] buffer = new byte[bufferSize];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    sendDataSize -= bytesRead;

                    // Update loadObserver with progress
                    if (loadObserver != null) {
                        double progress = (dataSize - sendDataSize) * 100.0 / dataSize;
                        loadObserver.update(progress);
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            return new ClientLoadingResult(endTime - startTime);
        } else {
            throw new ServerNotReadyException("Server is not ready");
        }
        }
}