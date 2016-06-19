package org.androware.androbeans;

import java.io.IOException;

/**
 * Created by jkirkley on 6/17/16.
 */
public interface ObjectWriter {

    public void write(Object object) throws IOException;
}
