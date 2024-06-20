/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/repository/hibernate/DerbyDialect.java,v 1.5 2006/06/08 16:00:50 doballve Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.repository.hibernate;

import java.sql.Types;
import net.sf.hibernate.dialect.GenericDialect;


/**
 * A quick extension to Hibernate 2.1 GenericDialect to support setting length
 * to BLOBs and CLOBs (Hibernate 2.1 had no custom support for Derby).
 *
 * Usage: set Hibernate dialect property to point to this class when using Derby.
 * At the time this was written, that is achieved by setting
 *   dbDialect=org.freebxml.omar.server.repository.hibernate.DerbyDialect
 * in build.properties.
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class DerbyDialect extends GenericDialect {
    
    public DerbyDialect() {
        super();
        // If length <= 255 (default, includes not set), use blob.
        // Otherwise use blob($l). Same logic for clob.
        registerColumnType( Types.BLOB, 255, "blob" );
        registerColumnType( Types.BLOB, "blob($l)" );
        registerColumnType( Types.CLOB, 255, "clob" );
        registerColumnType( Types.CLOB, "clob($l)" );
    }
}
