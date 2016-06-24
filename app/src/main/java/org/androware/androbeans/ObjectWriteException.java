package org.androware.androbeans;

/**
 * Created by jkirkley on 6/24/16.
 */
public class ObjectWriteException extends Exception {
    public ObjectWriteException() {
        super();
    }

    public ObjectWriteException(String detailMessage) {
        super(detailMessage);
    }

    public ObjectWriteException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ObjectWriteException(Throwable throwable) {
        super(throwable);
    }
}
