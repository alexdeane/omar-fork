/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/cms/CanonicalXMLCatalogingService.java,v 1.17 2007/01/12 21:31:08 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.cms;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.HashMap;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import org.freebxml.omar.common.RepositoryItem;
import org.freebxml.omar.common.exceptions.MissingRepositoryItemException;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.repository.RepositoryManagerFactory;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Canonical XML Content Cataloging Service
 *
 * @author Tony Graham
 *
 */
public class CanonicalXMLCatalogingService extends AbstractContentCatalogingService {
    private static final Log log = LogFactory.getLog(CanonicalXMLCatalogingService.class.getName());

    public ServiceOutput invoke(ServerRequestContext context, ServiceInput input, ServiceType service,
        InvocationController invocationController, UserType user)
        throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("CanonicalXMLCatalogingService.invoke()");
        }
        ServiceOutput so = new ServiceOutput();
        so.setOutput(context);
        RepositoryItem repositoryItem = input.getRepositoryItem();
        // The RI is optional per the [ebRS] spec. Return empty ServiceOutput.
        if (repositoryItem != null) {
            String roId = input.getRegistryObject().getId();

            ServerRequestContext outputContext = null;

            try {
                outputContext = context; //new RequestContext(null);
                StreamSource inputSrc = getAsStreamSource((ExtrinsicObjectType) input.getRegistryObject());

                StreamSource invocationControlFileSrc = rm.getAsStreamSource(invocationController.getEoId());

                //dumpStream(invocationControlFileSrc);

                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = initTransformer(tFactory, invocationControlFileSrc);
                // Use CatalogingService URIResolver to resolve RIs submitted in the
                // ServiceInput object
                transformer.setURIResolver(getURIResolver(context));
                //Set respository item as parameter
                transformer.setParameter("repositoryItem",
                    input.getRegistryObject().getId());

                //Create the output file with catalogedMetadata
                File outputFile = File.createTempFile("CanonicalXMLCatalogingService_OutputFile",
                        ".xml");
                outputFile.deleteOnExit();

                log.debug("Tempfile= " + outputFile.getAbsolutePath());

                StreamResult sr = new StreamResult(outputFile);
                transformer.transform(inputSrc, sr);

                RegistryObjectListType catalogedMetadata = (RegistryObjectListType) bu.getJAXBContext()
                                                                                      .createUnmarshaller()
                                                                                      .unmarshal(outputFile);            

                // TODO: User should refer to "Service object for the
                // Content Management Service that generated the
                // Cataloged Content."
                outputContext.setUser(user);

                bu.getObjectRefsAndRegistryObjects(catalogedMetadata, outputContext.getTopLevelObjectsMap(), outputContext.getObjectRefsMap());
            } catch (Exception e) {
                if (outputContext != context) {
                    outputContext.rollback();
                }
                throw new RegistryException(e);
            }

            
            so.setOutput(outputContext);

            // Setting this error list is redundant, but Content Validation Services
            // currently output a Boolean and a RegistryErrorList, so using
            // same mechanism to report errors from Content Cataloging Services.
            so.setErrorList(outputContext.getErrorList());

            if (outputContext != context) {
                outputContext.commit();
            }
        }
        return so;
    }

    /*
     * This method is used to create and initialize a Transformer instance
     */
    private Transformer initTransformer(TransformerFactory tFactory, 
                                        StreamSource invocationControlFileSrc) 
        throws TransformerConfigurationException {
        Transformer transformer = tFactory.newTransformer(invocationControlFileSrc);
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
        return transformer;
    }
    
    protected void dumpStream(StreamSource ss) {
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
        } catch (Exception e) {
        }
    }
    
    /**
     * A URIResolver for RepositoryItem objects
     *
     */
    class CatalogingServiceURIResolver implements URIResolver {
        private ServerRequestContext context;
        protected CatalogingServiceURIResolver(ServerRequestContext context) {
            this.context = context;
        }
        public Source resolve(String href, String base) throws TransformerException {
            Source source = null;
            try {
                // Should this check that href is UUID URN first?
                RepositoryItem ri = (RepositoryItem)context.getRepositoryItemsMap().get(href);
                if (ri == null) {
                    // if RI is not submitted in the ServiceInput class, use 
                    // the StreamSource to create a Source instance using the 
                    // href as the URI string.
                    try {
                        source = new StreamSource(href);
                        if (source == null) {
                            source = getSourceFromRepositoryManager(href);
                        }
                    } catch (TransformerException te) {
                        throw te;
                    } catch (Throwable ex) {
                        // if RI not found in the Map and a StreamSource could 
                        // not resolve the URI, use the RepositoryManager
                        source = getSourceFromRepositoryManager(href);
                    }
                } else {
                    source = new StreamSource(ri.getDataHandler().getInputStream());
                }
            } catch (TransformerException ex) {
                throw ex;
            } catch (Throwable t) {          
                throw new TransformerException(t);
            }            
            return source;
        }
    }
    
    private Source getSourceFromRepositoryManager(String href) 
    throws TransformerException {
        Source source = null;
        try {
            source = RepositoryManagerFactory.getInstance()
                                             .getRepositoryManager()
                                             .getAsStreamSource(href);
            if (source == null) {
                // RM could not resolve the URI throw exception
                throw new TransformerException(ServerResourceBundle.getInstance()
                                                                   .getString("message.error.couldNotResolveURI", 
                                                                               new Object[] {href}));
            }
        } catch (Throwable t) {
            throw new TransformerException(t);
        }
        return source;
    }
    
    /**
     * Gets a <code>URIResolver</code> that handles locating repository items
     *
     * @return an <code>URIResolver</code> value
     */
    public URIResolver getURIResolver(ServerRequestContext context) {
        return new CatalogingServiceURIResolver(context);
    }
    
}
