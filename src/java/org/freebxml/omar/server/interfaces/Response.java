/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/interfaces/Response.java,v 1.8 2005/04/01 11:16:49 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.interfaces;

import java.util.HashMap;

import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * A Response encapsulates all aspects of an outgoing registry response to an ebXML registry client.
 * @see
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class Response {
    private RegistryResponseType message = null;
    private HashMap idToRepositoryItemMap = null;

    
    public Response(RegistryResponseType message,
        HashMap idToRepositoryItemMap) {
        this.message = message;
        this.idToRepositoryItemMap = idToRepositoryItemMap;
    }
    
    public RegistryResponseType getMessage() {
        return message;
    }

    public HashMap getIdToRepositoryItemMap() {
        return idToRepositoryItemMap;
    }
}
