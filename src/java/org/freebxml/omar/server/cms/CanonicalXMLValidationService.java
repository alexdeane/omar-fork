/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/CanonicalXMLValidationService.java,v 1.10 2006/08/12 17:12:21 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.MissingResourceException;
import javax.activation.DataHandler;
import org.freebxml.omar.common.exceptions.ValidationException;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.xml.sax.InputSource;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.Utility;
import org.freebxml.omar.common.exceptions.MissingRepositoryItemException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.util.ServerResourceBundle;

import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorListType;


/**
 * Canonical XML Content Validation Service
 *
 * @author Paul Sterk
 *
 */
public class CanonicalXMLValidationService extends ContentValidationServiceImpl {
    private static final Log log = LogFactory.getLog(CanonicalXMLValidationService.class.getName());
    private static final String TMP_DIR = System.getProperty(
            "java.io.tmpdir");
    private static StreamSource schematronXSLTFileSrc = null;
    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {

        ServerRequestContext outputContext = context;

        try {
            //ExtrinsicObjectType eo = (ExtrinsicObjectType)input.getRegistryObject();
            //RepositoryItem repositoryItem = input.getRepositoryItem();
            //DataHandler dh = repositoryItem.getDataHandler();
            InputSource inputSource = null;

            //registryObject MUST be ExrinsicObject or ExternalLink of objectType WSDL
            ExtrinsicObjectType eo = null;
            ExternalLinkType extLink = null;
            RegistryObjectType registryObject = input.getRegistryObject();
            if (registryObject instanceof ExtrinsicObjectType) {
                eo = (ExtrinsicObjectType)registryObject;
                RepositoryItem repositoryItem = input.getRepositoryItem();
                if (repositoryItem == null) {
                    // Section 8.10 of the [ebRS] spec specifies that the RI
                    // is optional. Log message and return
                    log.info(ServerResourceBundle.getInstance()
                                                 .getString("message.noRepositoryItemIncluded",
                                                             new String[] {registryObject.getId()}));
                    ServiceOutput so = new ServiceOutput();
                    so.setOutput(outputContext);
                    return so;
                }
                inputSource = new InputSource(repositoryItem.getDataHandler().getInputStream());
            } else if (registryObject instanceof ExternalLinkType) {
                extLink = (ExternalLinkType)registryObject;
                String urlStr = extLink.getExternalURI();
                urlStr = Utility.absolutize(Utility.getFileOrURLName(urlStr));
                URL url = new URL(urlStr);
                InputStream is = url.openStream();
                inputSource = new InputSource(is);
            } else {
                throw new ValidationException("RegistryObject not ExtrinsicObject or ExternalLink");
            }


            StreamSource schematronInvControlFileSrc = rm.getAsStreamSource(invocationController.getEoId());
            //Commenting out caching until we figure out how to reinit the stream position at begining
            //Currently if we cache then subsequent uses result in error: "Could not compile stylesheet"
            // The schematron XSLT file is expected to be stable and change infrequently. So, cache it.
            //if (schematronXSLTFileSrc == null) {
                schematronXSLTFileSrc = new StreamSource(this.getClass().getClassLoader()
                                                         .getResourceAsStream("org/freebxml/omar/server/cms/conf/skeleton1-5.xsl"));
            //}

            if (log.isDebugEnabled()) {
                dumpStream(schematronInvControlFileSrc);
                dumpStream(schematronXSLTFileSrc);
            }

            // Use the Schematron Invocation Control File and Schematron XSLT to
            // create the XSLT Invocation Control File
            File xsltInvocationControlFile = File.createTempFile("InvocationControlFile_WSDLValidation",
                    ".xslt");
            xsltInvocationControlFile.deleteOnExit();
            StreamResult xsltInvocationControlFileSR = new StreamResult(xsltInvocationControlFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(schematronXSLTFileSrc);

            configureTransformer(transformer);

            // This call creates the XSLT Invocation Control File using
            // schematron invocation control file and schematron xslt files
            transformer.transform(schematronInvControlFileSrc, xsltInvocationControlFileSR);

            // Use generated XSLT Invocation Control File to validate the WSDL file(s)
            StreamSource xsltInvocationControlFileSrc = new StreamSource(xsltInvocationControlFile);
            transformer = tFactory.newTransformer(xsltInvocationControlFileSrc);

            configureTransformer(transformer);

            //Set respository item as parameter
            transformer.setParameter("repositoryItem",
                input.getRegistryObject().getId());

            if ((eo != null) && (eo.getMimeType().equalsIgnoreCase("application/zip"))) {
                ArrayList files = Utility.unZip(TMP_DIR, inputSource.getByteStream());

                //Now iterate and create ExtrinsicObject - Repository Item pair for each unzipped file
                Iterator iter = files.iterator();
                while (iter.hasNext()) {
                    File file = (File)iter.next();
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    StringBuffer sb = new StringBuffer();
                    while (br.ready()) {
                        sb.append(br.readLine());
                    }
                    StringReader reader = new StringReader(sb.toString());
                    StreamSource inputSrc = new StreamSource(reader);
                    validateXMLFile(inputSrc, transformer);
                }
            } else {
                //Following will fail if there are unresolved imports in the WSDL
                StreamSource inputSrc = new StreamSource(inputSource.getByteStream());
                //dumpStream(inputSrc);
                //inputSrc = new StreamSource(inputSource.getByteStream());
                validateXMLFile(inputSrc, transformer);
            }
        } catch (ValidationException e) {
            if (outputContext != context) {
                outputContext.rollback();
            }
            log.error(ServerResourceBundle.getInstance().getString("message.errorValidatingXML"), e);
            throw e;
        }  catch (Exception e) {
            if (outputContext != context) {
                outputContext.rollback();
            }
            log.error(ServerResourceBundle.getInstance().getString("message.errorValidatingXML"), e);
            throw new RegistryException(e);
        }

        ServiceOutput so = new ServiceOutput();
        so.setOutput(outputContext);

        if (outputContext != context) {
            outputContext.commit();
        }

        return so;
    }

    private void configureTransformer(Transformer transformer) {
        transformer.setURIResolver(rm.getURIResolver());
        transformer.setErrorListener(new ErrorListener() {
            public void error(TransformerException exception)
                throws TransformerException {
                log.info(exception);
            }

            public void fatalError(TransformerException exception)
                throws TransformerException {
                log.error(exception);
                throw exception;
            }

            public void warning(TransformerException exception)
                throws TransformerException {
                log.info(exception);
            }
        });
    }

    private void validateXMLFile(StreamSource inputSrc, Transformer transformer)
        throws IOException, TransformerException, RegistryException {
        //Create the output file with validation results
        File outputFile = File.createTempFile("CanonicalXMLValidationService_OutputFile",
                ".xml");
        outputFile.deleteOnExit();
        if (log.isDebugEnabled()) {
            dumpStream(inputSrc);
            log.debug("Tempfile= " + outputFile.getAbsolutePath());
        }

        StreamResult sr = new StreamResult(outputFile);

        transformer.transform(inputSrc, sr);

        BufferedReader outputFileReader = new BufferedReader(new FileReader(outputFile));
        StringBuffer sb2 = null;
        while (outputFileReader.ready()) {
            String line = outputFileReader.readLine();
            if (line.indexOf("error") != -1) {
                // if the line has the word 'error', a validation error has occurred
                // obtain the message and store. There may be multiple
                // violations. So, loop through all, then pass to exception
                if (sb2 == null) {
                    sb2 = new StringBuffer();
                    sb2.append(ServerResourceBundle.getInstance().getString("message.errorValidatingXML"));
                }
                // The error message SHOULD contain the key to an i18n message
                // in the server ResourceBundle.properties file.
                String key = line;
                try {
                    line = ServerResourceBundle.getInstance().getString(key);
                } catch (MissingResourceException ex) {
                    log.error(ServerResourceBundle.getInstance().getString("message.missingMessage", new Object[]{key}));
                }
                sb2.append(line).append(" ");
            }
        }
        if (sb2 != null) {
            throw new ValidationException(sb2.toString());
        }
    }

    protected void dumpStream(StreamSource ss) {
        /* Commented because call screws up subsequent actual read of the stream source.
         * Adding reset at the end did not fix this problem for some reason.
        BufferedReader br = null;

        try {
            InputStream inputStream = ss.getInputStream();
            Reader inputReader = ss.getReader();

            if (inputStream != null) {
                br = new BufferedReader(new InputStreamReader(inputStream));
            } else if (inputReader != null) {
                br = new BufferedReader(inputReader);
            }

            if (br == null) {
                System.err.println("No reader for StreamSource");

                return;
            }

            String line;

            while ((line = br.readLine()) != null) {
                System.err.print(line);
            }

            System.err.println();

            //Reposition to begining oif stream
            inputStream.reset();
        } catch (Exception e) {
        }
         */
    }
}
