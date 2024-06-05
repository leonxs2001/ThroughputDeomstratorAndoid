package de.thb.demonstrator.exception;

public class ServerNotReadyException extends Exception{
    public ServerNotReadyException() {
        super();
    }

    public ServerNotReadyException(String message) {
        super(message);
    }
}
