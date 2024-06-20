/*
 * JAXRTest.java
 *
 * Created on April 8, 2002, 10:53 AM
 *
 * $Header: /cvsroot/ebxmlrr/omar/test/org/freebxml/omar/client/xml/registry/JAXRTest.java,v 1.3 2005/03/24 22:15:39 psterk Exp $
 *
 */

package org.freebxml.omar.client.xml.registry;

import java.util.Properties;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

import org.freebxml.omar.client.xml.registry.util.JAXRUtility;

/**
 * Common base class for all JAXR tests
 *
 * To use the JAAS authentication mechanisms you must create a file ~/.java.login.config with following content
 *
 *  JAXRTest {
 *   com.sun.security.auth.module.KeyStoreLoginModule required debug=true keyStoreURL="file://c:/Docume~1/najmi/jaxr-ebxml/security/keystore.jks";
 *  };
 *
 * Note that the keyStoreURL must point to wherever your keySTore file is. The ~ home directory is teh one pointed to the 
 * user.home System property. On windows 2000 it is file://c:/Docume~1/<uour login>.
 *
 * The password dialog usually takes a little while to pop up and does not always appear on top of other windows.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public abstract class JAXRTest {
    
    Connection connection = null;
    RegistryService service = null;
    BusinessLifeCycleManager lcm = null;
    BusinessQueryManager bqm = null;
    DeclarativeQueryManager dqm = null;
    
    private JAXRTest() {
        //Not allowed to be used
    }
    
    /** Creates new JAXRTest */
    public JAXRTest(Properties connectionProps) throws JAXRException {        
        createConnection(connectionProps);
    }
    
    /**
     * Makes a connection to a JAXR Registry.
     *
     * @param url The URL of the registry.
     */
    public void createConnection(Properties connectionProps) throws JAXRException {        
        if (connectionProps == null) {
            connectionProps = new Properties();
            connectionProps.put("javax.xml.registry.queryManagerURL", 
                "http://localhost:8080/ebxmlrr/registry/soap"); //http://registry.csis.hku.hk:8201/ebxmlrr/registry/soap
        }
        
        ConnectionFactory connFactory = getConnectionFactory(connectionProps);
        connFactory.setProperties(connectionProps);
        connection = connFactory.createConnection();
        service = connection.getRegistryService();
        
        bqm = service.getBusinessQueryManager();
        dqm = service.getDeclarativeQueryManager();
        lcm = service.getBusinessLifeCycleManager();        
    }
    
    private ConnectionFactory getConnectionFactory(Properties connectionProps) throws JAXRException {
        //Get factory class
        ConnectionFactory connFactory =  JAXRUtility.getConnectionFactory();
        String url = (String)connectionProps.get("javax.xml.registry.queryManagerURL");
        if (url == null) {
            throw new JAXRException("Connection property javax.xml.registry.queryManagerURL not defined.");
        }      
        return connFactory;
    }
        
}
