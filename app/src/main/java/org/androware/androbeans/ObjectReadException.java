package org.androware.androbeans;

/**
 * Created by jkirkley on 6/22/16.
 */
public class ObjectReadException extends Exception {
    public ObjectReadException() {
        super();
    }

    public ObjectReadException(String detailMessage) {
        super(detailMessage);
    }

    public ObjectReadException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ObjectReadException(Throwable throwable) {
        super(throwable);
    }
}
