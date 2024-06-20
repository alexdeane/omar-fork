/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Import.java,v 1.1 2005/11/23 21:39:16 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.xml.registry.ClientRequestContext;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.BindingUtility;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;


public class Import extends AbstractAdminFunction {
    private HashMap attachments = new HashMap();
    private String request;
    
    public void execute(AdminFunctionContext context, String args)
    throws Exception {
	if (args != null) {
	    String[] tokens = args.split("\\s+");

	    int tIndex = 0;

	    for (tIndex = 0;
		 ((tIndex < tokens.length) && tokens[tIndex].startsWith("-"));
		 tIndex++) {
		String option = tokens[tIndex];

		if ((collator.compare(option, "-a") == 0) ||
                    (collator.compare(option, "--attach") == 0)) {
		    if (++tIndex == tokens.length) {
			context.printMessage(getUsage());

			return;
		    }

                    String attachData = tokens[tIndex++];
                    StringTokenizer tokenizer = new StringTokenizer(attachData,
								    ",");

                    String attachFileName = null;
                    String mimeType = null;
                    String attachId = "id";

                    int j = 0;

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();

                        if (j == 1) {
                            attachFileName = token;
                        } else if (j == 2) {
                            mimeType = token;
                        }

                        if (j == 3) {
                            attachId = token;
                        }

                        j++;
                    }
                    
                    if (attachFileName == null) {
                        context.printMessage(format(rb,"invalidArgument",
						    new Object[] { attachData }));
                        return;
                    }
                    
		} else {
		    context.printMessage(format(rb,"invalidArgument",
						new Object[] { option }));
		    return;
		}
            }
            
            if (tIndex == tokens.length - 1) {
                request = tokens[tIndex];
            } else {
                context.printMessage(format(rb,"argumentRequired"));
            
                return;
            }
        } else {
            context.printMessage(format(rb,"argumentRequired"));
            
            return;
        }
       
	Unmarshaller unmarshaller = BindingUtility.getInstance().getJAXBContext().createUnmarshaller();
                    
	SubmitObjectsRequest submitRequest = (SubmitObjectsRequest)unmarshaller.unmarshal(new File(request));
	HashMap attachMap = new HashMap();  //id to attachments map
                    
	//Look for special temporary Slot on ExtrinsicObjects to resolve to RepositoryItem
	//If a file in same directory is found with filename same as slot value
	//then assume it is the matching RepositoryItem
	List ros = submitRequest.getRegistryObjectList().getIdentifiable();
	Iterator iter=ros.iterator();
	while (iter.hasNext()) {
	    Object obj = iter.next();
	    if (obj instanceof ExtrinsicObjectType) {
		ExtrinsicObjectType eo = (ExtrinsicObjectType)obj;
		HashMap slotsMap = BindingUtility.getInstance().getSlotsFromRegistryObject(eo);
		String slotName = BindingUtility.getInstance().CANONICAL_SLOT_EXTRINSIC_OBJECT_REPOSITORYITEM_URL;
		if (slotsMap.containsKey(slotName)) {
		    String riURLStr = (String)slotsMap.get(slotName);
		    File riFile = new File(riURLStr);
		    DataHandler riDataHandler = new DataHandler(new FileDataSource(riFile));                                
		    attachMap.put(eo.getId(), riDataHandler);
                                
		    //Remove transient slot
		    slotsMap.remove(slotName);
		    eo.getSlot().clear();
		    BindingUtility.getInstance().addSlotsToRegistryObject(eo, slotsMap);
		}
	    }
	}
	ClientRequestContext requestContext = new ClientRequestContext("AdminTool:import", submitRequest);
	requestContext.setRepositoryItemsMap(attachMap);
	BulkResponse br = ((LifeCycleManagerImpl) context.getService().getLCM()).doSubmitObjectsRequest(requestContext);
	JAXRUtility.checkBulkResponse(br);
    }
    
    public String getUsage() {
        return format(rb, "usage.import");
    }
}
