/*
 * CMSTestUtility.java
 *
 * Created on December 8, 2004, 1:09 PM
 */
package org.freebxml.omar.server.cms;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CredentialInfo;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.RepositoryItemImpl;

import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.net.URL;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.activation.DataHandler;

/**
 *
 * @author  tg127171
 */
public class CMSTestUtility {
    protected static URL cppaURL = CMSTestUtility.class.getResource(
            "/resources/CPP1.xml");
    protected static BindingUtility bu = BindingUtility.getInstance();
    private static CMSTestUtility instance = null;
    
    /** Creates a new instance of CMSTestUtility */
    private CMSTestUtility() {
    }

    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public static CMSTestUtility getInstance() {
        if (instance == null) {
            synchronized (CMSTestUtility.class) {
                if (instance == null) {
                    instance = new CMSTestUtility();
                }
            }
        }

        return instance;
    }
    /**
     * Creates an ExtrinsicObject of specific type.
     *
     * @return an <code>ExtrinsicObjectType</code> value
     * @exception Exception if an error occurs
     */
    static ExtrinsicObjectType createExtrinsicObject(String desc,
        String objectType) throws Exception {
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObject();

        if (desc != null) {
            eo.setDescription(bu.createInternationalStringType(desc));
        }

        String eoId = org.freebxml.omar.common.Utility.getInstance().createId();
        eo.setId(eoId);
        eo.setObjectType(objectType);
        eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());

        return eo;
    }

    /**
     * Creates a CPP RepositoryItem.
     *
     * @param eoId id to use when signing the RepositoryItem
     * @return a <code>RepositoryItem</code> value
     * @exception Exception if an error occurs
     */
    RepositoryItem createCPPRepositoryItem(String eoId)
        throws Exception {
        DataHandler dataHandler = new javax.activation.DataHandler(cppaURL);

        return createRepositoryItem(dataHandler, eoId);
    }
    
    /**
     * Creates a {@link RepositoryItem}.
     *
     * @param dh the {@link DataHandler} representing the payload
     * @param id the ID to use for the {@link RepositoryItem}
     * @exception Exception if an error occurs
     */
    RepositoryItem createRepositoryItem(DataHandler dh, String id)
        throws Exception {
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        return ri;
    }
    

//    /**
//     * Creates a {@link RepositoryItem} that is signed with generated
//     * credentials.
//     *
//     * @param dh the {@link DataHandler} representing the payload
//     * @param id the ID to use for the {@link RepositoryItem}
//     * @return a signed {@link RepositoryItem}
//     * @exception Exception if an error occurs
//     */
//    RepositoryItem createSignedRepositoryItem(DataHandler dh, String id)
//        throws Exception {
//        return su.signPayload(dh, id, createCredentialInfo(dh));
//    }

    /**
     * Creates a {@link CredentialInfo} for a {@link DataHandler}.
     *
     * <p>Uses a dummy X500 name for signing the {@link CredentialInfo}.
     *
     * @param dh the <code>DataHandler</code>
     * @return the <code>CredentialInfo</code> for the <code>DataHandler</code>
     * @exception Exception if an error occurs
     */
    CredentialInfo createCredentialInfo(DataHandler dh)
        throws Exception {
        //Generating 512 bit DSA key pair and self-signed certificate (SHA1WithDSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("DSA", "SHA1WithDSA");
        X500Name x500name = new X500Name("Tester", "Test Unit", "OMAR", "JKL",
                "KS", "FI");
        certandkeygen.generate(512);

        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] = certandkeygen.getSelfCertificate(x500name,
                90 * 24 * 60 * 60);

        //Create the credentials to use.
        //TODO: this could be created only once per SOAPSender
        //TODO: now signing with null server alias. Might gen overhead by sending cert
        return new CredentialInfo((String) null, ax509cert[0], ax509cert,
            privateKey);
    }
}
