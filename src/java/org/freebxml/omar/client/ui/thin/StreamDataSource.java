/*
* StreamDataSource.java
*
* Created on March 29, 2005, 8:07 PM
*/

package org.freebxml.omar.client.ui.thin;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.*; 
import javax.servlet.http.HttpServletRequest;

/**
*
* @author Anand
*/

public class StreamDataSource implements DataSource {

    private HttpServletRequest m_req;

    /**
    * Returns the content type for the request stream.
    */
    public StreamDataSource(HttpServletRequest req) {
        m_req = req;
    }

    /**
    * Returns the content type for the request stream.
    */
    public String getContentType() {
        return m_req.getContentType();
    }

    /**
    * Returns a stream from the request.
    */
    public InputStream getInputStream() throws IOException {
        return m_req.getInputStream();
    }

    /** 
    * This method is useless and it always returns a null.
    */
    public String getName(){
        return null;
    }

    /**
    Maps output to System.out. Do something more sensible here...
    */
    public OutputStream getOutputStream() {
        return System.out;
    }
}

