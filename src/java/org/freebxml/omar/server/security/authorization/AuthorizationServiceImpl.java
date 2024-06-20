/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/server/security/authorization/AuthorizationServiceImpl.java,v 1.71 2007/04/13 14:30:57 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.server.security.authorization;

import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BagAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.Function;
import com.sun.xacml.finder.PolicyFinderModule;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freebxml.omar.client.xml.registry.infomodel.PersonNameImpl;
import org.freebxml.omar.common.BindingUtility;
import javax.xml.registry.JAXRException;
import org.freebxml.omar.common.ReferenceInfo;
import javax.xml.registry.RegistryException;
import org.freebxml.omar.common.exceptions.AuthorizationException;
import org.freebxml.omar.common.exceptions.ObjectNotFoundException;
import org.freebxml.omar.common.exceptions.UnauthorizedRequestException;
import org.freebxml.omar.server.common.RegistryProperties;
import org.freebxml.omar.server.common.ServerRequestContext;
import org.freebxml.omar.server.persistence.PersistenceManager;
import org.freebxml.omar.server.persistence.PersistenceManagerFactory;
import org.freebxml.omar.server.security.authentication.AuthenticationServiceImpl;
import org.freebxml.omar.server.util.ServerResourceBundle;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeDesignator;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.FunctionFactory;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.ctx.Subject;
import com.sun.xacml.finder.AttributeFinder;
import com.sun.xacml.finder.AttributeFinderModule;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.impl.CurrentEnvModule;


/**
 * AuthorizationService implementation for the ebxml Registry.
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class AuthorizationServiceImpl {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private AuthorizationServiceImpl _ authorizationServiceImpl; */
    private static AuthorizationServiceImpl instance = null;

    /**
     * The prefix for all access control attribute ids as defined by ebRIM.
     */
    public static final String REGISTRY_ACP_RPREFIX = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:";

    /**
     * The prefix for all resource attribute designators as defined by ebRIM.
     */
    public static final String RESOURCE_ATTRIBUTE_PREFIX = REGISTRY_ACP_RPREFIX + "resource:";

    /**
     * The prefix for all subject attribute designators as defined by ebRIM.
     */
    public static final String SUBJECT_ATTRIBUTE_PREFIX = REGISTRY_ACP_RPREFIX + "subject:";

    /**
     * The prefix for all action attribute designators as defined by ebRIM. TODO: Add it to ebRIM.
     */
    public static final String ACTION_ATTRIBUTE_PREFIX = REGISTRY_ACP_RPREFIX + "action:";

    /**
     * The prefix for all environment attribute designators (impl specific).
     */
    public static final String ENVIRONMENT_ATTRIBUTE_PREFIX = REGISTRY_ACP_RPREFIX + "environment:";

            
    /** The owner resource attribute from V3 spec.*/
    public static final String RESOURCE_ATTRIBUTE_OWNER = RESOURCE_ATTRIBUTE_PREFIX + "owner";

    /** The owner resource attribute from V3 spec.*/
    public static final String RESOURCE_ATTRIBUTE_SELECTOR = RESOURCE_ATTRIBUTE_PREFIX + "selector";
    
    /** The owner resource attribute (impl specific).*/
    public static final String RESOURCE_ATTRIBUTE_RESOURCE = RESOURCE_ATTRIBUTE_PREFIX + "resource";

    /** The request-context environment attribute specific to ebxmlrr (not from V3 spec).*/
    public static final String ENVIRONMENT_ATTRIBUTE_REQUEST_CONTEXT = ENVIRONMENT_ATTRIBUTE_PREFIX + "request-context";

    /** The subject-id subject attribute from XACML 1.0 spec. Should be in XACML impl??*/
    public static final String SUBJECT_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";

    /** The role subject attribute from V3 spec.*/
    public static final String SUBJECT_ATTRIBUTE_ROLES = SUBJECT_ATTRIBUTE_PREFIX + "roles";
    
    /** The role subject attribute from V3 spec.*/
    public static final String SUBJECT_ATTRIBUTE_GROUPS = SUBJECT_ATTRIBUTE_PREFIX + "groups";

    /** The user subject attribute specific to ebxmlrr (not from V3 spec).*/
    public static final String SUBJECT_ATTRIBUTE_USER = SUBJECT_ATTRIBUTE_PREFIX + "user";

    /** The action-id action attribute from V3 spec.*/
    public static final String ACTION_ATTRIBUTE_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    /** The reference-source action attribute. Needs to be added to V3 specs. */
    public static final String ACTION_ATTRIBUTE_REFERENCE_SOURCE = ACTION_ATTRIBUTE_PREFIX + "reference-source";
    
    /** The reference-source-attribute action attribute. Needs to be added to V3 specs. */
    public static final String ACTION_ATTRIBUTE_REFERENCE_SOURCE_ATTRIBUTE = ACTION_ATTRIBUTE_PREFIX + "reference-source-attribute";
    
    
    /** 
     * The reference-source-attribute-filter action attribute prefix. 
     * Suffix is an attribute name in RIM class for reference-source.
     * Needs to be added to V3 specs. 
     */
    public static final String ACTION_ATTRIBUTE_REFERENCE_SOURCE_ATTRIBUTE_FILTER_PREFIX = ACTION_ATTRIBUTE_PREFIX + "reference-source-attribute-filter:";
                   
    public static final String PROP_REGISTRY_REQUEST = "org.freebxml.omar.server.security.authorization.RegistryRequest";

    /** The standard namespace where all the ebRIM spec-defined functions live */
    public static final String FUNCTION_NS = "urn:oasis:names:tc:ebxml-regrep:3.0:rim:acp:function:";
    
    private static BindingUtility bu = BindingUtility.getInstance();
    private PDP pdp = null;
    private static Log log = LogFactory.getLog(AuthorizationServiceImpl.class);
    String idForDefaultACP = RegistryProperties.getInstance().getProperty("omar.security.authorization.defaultACP");
    private AuthenticationServiceImpl ac = null;

    /**
     * Class Constructor. Protected and only used by getInstance()
     *
     */
    protected AuthorizationServiceImpl() {
        //Load the functions
        Set customFunctions = loadCustomFunctions();
        Iterator funcIter = customFunctions.iterator();
        while (funcIter.hasNext()) {
            Function func = (Function)funcIter.next();
            FunctionFactory.addTargetFunction(func);
        }
        
        // Add any custom PolicyFinderModules to XACML engine
        Set policyFinders = loadCustomPolicyFinderModules();
        PolicyFinder policyFinder = new PolicyFinder();
        policyFinder.setModules(policyFinders);

        // Add any custom AttributeFinderModules to XACML engine
        Set customAttributeFinderModules = loadCustomAttributeFinderModules();
        CurrentEnvModule envModule = new CurrentEnvModule();
        List attrModules = new ArrayList();
        attrModules.addAll(customAttributeFinderModules);
        attrModules.add(envModule);

        AttributeFinder attrFinder = new AttributeFinder();
        attrFinder.setModules(attrModules);

        pdp = new PDP(new PDPConfig(attrFinder, policyFinder, null));
        ac = AuthenticationServiceImpl.getInstance();
    }
    
    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public synchronized static AuthorizationServiceImpl getInstance() {
        if (instance == null) {
            instance = new AuthorizationServiceImpl();
        }

        return instance;
    }
    
    /**
      *
      * @return the Set of custom AttributeFinder modules for use with the XACML engine.
      */    
    private Set loadCustomAttributeFinderModules() {
        HashSet customAFMs = new HashSet();
        String customAFMList = RegistryProperties.getInstance().getProperty("omar.security.authorization.customAttributeFinderModules");
        if (customAFMList != null) {
            StringTokenizer st = new StringTokenizer(customAFMList, ",");
            while (st.hasMoreTokens()) {
                String customAFMClassName = st.nextToken();
                try {
                    AttributeFinderModule afm = (AttributeFinderModule)Class.forName(customAFMClassName).newInstance();
                    customAFMs.add(afm);
                    log.debug("Loaded custom attribute finder module '" + customAFMClassName + "'");
                }
                catch (Throwable t) {
                    log.warn(ServerResourceBundle.getInstance().getString("message.FailedToLoadCustomAttributeFinderModuleException", new Object[]{customAFMClassName, t.getMessage()}));
                }
            }
        }
        return customAFMs;
    }

    /**
      *
      * @return the Set of custom PolicyFinder modules for use with the XACML engine.
      */    
    private Set loadCustomPolicyFinderModules() {
        HashSet customPFMs = new HashSet();
        String customPFMList = RegistryProperties.getInstance().getProperty("omar.security.authorization.customPolicyFinderModules");
        if (customPFMList != null) {
            StringTokenizer st = new StringTokenizer(customPFMList, ",");
            while (st.hasMoreTokens()) {
                String customPFMClassName = st.nextToken();
                try {
                    PolicyFinderModule pfm = (PolicyFinderModule)Class.forName(customPFMClassName).newInstance();
                    customPFMs.add(pfm);
                    log.debug("Loaded custom policy finder module '" + customPFMClassName + "'");
                }
                catch (Throwable t) {
                    log.warn(ServerResourceBundle.getInstance().getString("message.FailedToLoadCustomPolicyFinderModuleException", new Object[]{customPFMClassName, t.getMessage()}));
                }
            }
        }
        return customPFMs;
    }
    
    /**
      *
      * @return the Set of custom Functions for use with the XACML engine.
      */    
    private Set loadCustomFunctions() {
        HashSet customFuncs = new HashSet();
        String customFuncList = RegistryProperties.getInstance().getProperty("omar.security.authorization.customFunctions");
        if (customFuncList != null) {
            StringTokenizer st = new StringTokenizer(customFuncList, ",");
            while (st.hasMoreTokens()) {
                String customFuncClassName = st.nextToken();
                try {
                    Function func = (Function)Class.forName(customFuncClassName).newInstance();
                    customFuncs.add(func);
                    log.debug("Loaded custom attribute finder module '" + customFuncClassName + "'");
                }
                catch (Throwable t) {
                    log.warn(ServerResourceBundle.getInstance().getString("message.FailedToLoadCustomFunctionException", new Object[]{customFuncClassName, t.getMessage()}));
                }
            }
        }
        return customFuncs;
    }
    
    /** Check if user is authorized to perform specified request using V3 specification.
      * <p>
      * Check if the specified User (requestor) is authorized to make this request or not.
      * The initial subject lists contains the object in the request is a resource.
      * The primary action is determined by the type of request. In addition
      *
      * <ul>
      * <li>
      * <b><i>AdhocQueryRequest: </i></b>
      *     Process query as normal and then filter out objects that should 
      *     not be visible to the client.
      * </li>
      * <li>
      * <b><i>ApproveObjectRequest: </i></b>
      *     Check if subject is authorized for the approve action.
      * </li>
      * <li>
      * <b><i>Deprecate/UndeprecateRequest: </i></b>
      *     Check if subject is authorized for the deprecate/undeprecate action.
      * </li>
      * <li>
      * <b><i>RemoveObjectRequest: </i></b>
      *     Check if subject is authorized for the delete action.
      * </li>
      * <li>
      * <b><i>SubmitObjectsRequest/UpdateObjectsRequest: </i></b>
      *     Check if subject authorized for the create action. Check any 
      *     referenced objects and see if their policies allows reference action.
      * </li>
      * </ul>
      *  
      * @todo Do we need any new Attribute types by Extending AttributeValue (have string URI etc.)??
      * @todo Do we need any new functions??
      *
      * @throws RegistryException
      */
    public AuthorizationResult checkAuthorization(ServerRequestContext context)
        throws RegistryException 
    {
        try {     
            RegistryRequestType registryRequest = context.getCurrentRegistryRequest();
            
            if (null == context.getUser()) {
                //Set context user as RegistryGuest built in 
                context.setUser(ac.registryGuest);
            }
            
            String userId = context.getUser().getId();
            AuthorizationResult authRes = new AuthorizationResult(userId);
        
            boolean isAdmin = context.isRegistryAdministrator();
            if (isAdmin) {
                // Allow RegistryAdmin role all privileges
                return authRes;
            }

            Set subjects = new HashSet();
            Set actions = new HashSet();
            Set environment = new HashSet();            
            Attribute actionAttr = null;
            boolean readOnly = false;

            String action = bu.getActionFromRequest(registryRequest);
            actionAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                    new URI(StringAttribute.identifier), null, null,
                    new StringAttribute(action));
                       
            // Determine the action attributes.
            if (registryRequest instanceof AdhocQueryRequest) {
                readOnly = true;
            } 
            actions.add(actionAttr);

            // Init subject attributes
            Set userSubjectAttributes = new HashSet();
            Attribute idSubjectAttr = new Attribute(new URI(SUBJECT_ATTRIBUTE_ID),
                                                    new URI(AnyURIAttribute.identifier), 
                                                    null, null,
                                                    new AnyURIAttribute(new URI(userId)));
            userSubjectAttributes.add(idSubjectAttr);
            Attribute userSubjectAttr = new Attribute(new URI(SUBJECT_ATTRIBUTE_USER),
                                                      new URI(ObjectAttribute.identifier), 
                                                      null, null,
                                                      new ObjectAttribute(context.getUser()));
            userSubjectAttributes.add(userSubjectAttr);
            
            Subject userSubject = new Subject(new URI(AttributeDesignator.SUBJECT_CATEGORY_DEFAULT), 
                                              userSubjectAttributes);
            subjects.add(userSubject);
            
            //Pass RequestContext as an environment attribute
            Attribute requestEnvAttr = new Attribute(new URI(ENVIRONMENT_ATTRIBUTE_REQUEST_CONTEXT), 
                                                          new URI(ObjectAttribute.identifier), 
                                                          null, null, 
                                                          new ObjectAttribute(context));
            environment.add(requestEnvAttr);
            

            // Iterate over each resource and see if action is authorized on the
            // resource by the subject
            List ids = new ArrayList();
            if (registryRequest instanceof AdhocQueryRequest) {
                //For AdhocQueryRequest query is already done and result is in
                //queryResults. Now do access control check on queryResults.
                Iterator iter = context.getQueryResults().iterator();
                while (iter.hasNext()) {
                    IdentifiableType ro = (IdentifiableType)iter.next();
                    ids.add(ro.getId());
                }
            }
            else {
                ids.addAll(bu.getIdsFromRequest(registryRequest));
            }
            
            //Optimization: Get ownersMap in a single query and cache it
            HashMap ownersMap = getOwnersMap(context, ids);
            
            Iterator idsIter = ids.iterator();
            while (idsIter.hasNext()) {
                String id = (String) idsIter.next();
                if (id != null) {
                    if ((!readOnly) && (id.equals(idForDefaultACP))) {
                        // Auth check for defaultACP is special and requires that
                        // it is submitted by RegistryAdministrator role.
                        // Note this will be generalized when we have better 
                        // Role Based Access Control (RBAC) support
                        if (!isAdmin) {
                            String msg = getExceptionMessage("message.error.authorization.allowedOnlyToAdmin.defineDefaultACP", id, context.getUser(), getActionString(actions));
                            throw new UnauthorizedRequestException(id, context.getUser().getId(), getActionString(actions), msg);
                        }
                    } 
                    else {
                        try {
                            checkAuthorizationForResource(context, id, subjects,
                                actions, environment);
                            authRes.addPermittedResource(id);
                        }
                        catch (UnauthorizedRequestException ure) {
                            authRes.addDeniedResourceException(ure);
                        }
                        catch (RegistryException re) {
                            if (re.getCause() instanceof UnauthorizedRequestException) {
                                authRes.addDeniedResourceException((UnauthorizedRequestException)re.getCause());
                            }
                            else {
                                throw re;
                            }
                        }
                    }
                } 
                else {
                    int i = 0;
                }
            }
            
            log.debug("userId=" + userId + " is " + 
                (authRes.getResult() == AuthorizationResult.PERMIT_NONE ? "not " : "") +
                "allowed to perform the requested operation.");
            return authRes;
        } catch (URISyntaxException e) {
            throw new RegistryException(e);
        } catch (AuthorizationException e) {
            throw e;
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    /** Check if subject is authorized to perform action on the resource
      * RegistryObject.
      *
      * @param id
      *     id of the resource being accessed.
      * @param subjects
      *     A list of xacml subject Attributes representing the subject making
      *     the request.
      * @param actions
      *     A list of xacml action Attributes representing the action being
      *     requested.
      * 
      * @throws RegistryException
      */
    private void checkAuthorizationForResource(ServerRequestContext context, 
        String id, 
        Set subjects, 
        Set actions,
        Set environment)
        throws RegistryException 
    {
        boolean isAdmin = context.isRegistryAdministrator();
        RegistryObjectType ro = getRegistryObject(context, id, false);
        if (ro == null) {
            String msg = getExceptionMessage("message.error.authorization.ObjectNotFound", id, context.getUser(), getActionString(actions));
            throw new AuthorizationException(msg);
        }
        
        //Only allow non RegistryAdministartor roles to READ Federation and Registry types
        if (((ro instanceof FederationType) || (ro instanceof RegistryType)) && (!(isAdmin))) {
            String actionName = getActionAttributeValue(actions, ACTION_ATTRIBUTE_ID);
            if (!((actionName.equalsIgnoreCase("read")) || (actionName.equalsIgnoreCase("reference")))) {
                String msg = getExceptionMessage("message.error.authorization.allowedOnlyToAdmin.defineFederation", id, context.getUser(), getActionString(actions));
                throw new UnauthorizedRequestException(id, context.getUser().getId(), getActionString(actions), msg);
            }
        }
        
        String ownerId = getRegistryObjectOwnerId(context, id);

        try {
           
            //Resource Attributes
            Attribute idResourceAttr = new Attribute
                (new URI(EvaluationCtx.RESOURCE_ID),
                new URI(AnyURIAttribute.identifier), null, null,
                new AnyURIAttribute(new URI(id)));

            Attribute resourceResourceAttr = new Attribute(new URI(RESOURCE_ATTRIBUTE_RESOURCE),
                                                      new URI(ObjectAttribute.identifier), 
                                                      null, null,
                                                      new ObjectAttribute(ro));
            
            Attribute ownerResourceAttr = new Attribute
                (new URI(RESOURCE_ATTRIBUTE_OWNER),
                new URI(AnyURIAttribute.identifier), null, null,
                new AnyURIAttribute(new URI(ownerId)));
            Set resourceAttributes = new HashSet();
            resourceAttributes.add(idResourceAttr);
            
            resourceAttributes.add(ownerResourceAttr);

            resourceAttributes.add(resourceResourceAttr);

            int decision = Result.DECISION_DENY;
            Status status = null;
            if (context.getConfirmationAssociations().containsKey(id)) {
                //Bypass auth check for confirm ass
                decision = Result.DECISION_PERMIT;
            } else {
                RequestCtx req = new RequestCtx(subjects,
                        resourceAttributes, actions, environment);

                ResponseCtx resp = pdp.evaluate(req);

                Set results = resp.getResults();

                // Expecting only one Result
                Result result = (Result)results.iterator().next();
                status = result.getStatus();                
                decision = result.getDecision();
            }
            
            if (!(decision == Result.DECISION_PERMIT)) {
                String statusMsg = status.getMessage();
                if (statusMsg == null) {
                    statusMsg = ServerResourceBundle.getInstance()
                                                    .getString("message.NoInfoAvailable");
                }
                String msg = getExceptionMessage("message.UnauthorizedRequestDenied", id, context.getUser(), getActionString(actions), statusMsg);
                throw new UnauthorizedRequestException(id, context.getUser().getId(), getActionString(actions), msg);
            }
            
            //The action is authorized for the resource.
            //However, some actions on a resource may result in other actions on other resources
            //Check authorization for such special secondary actions next
            
            RegistryRequestType registryRequest = context.getCurrentRegistryRequest();

            if ((registryRequest instanceof SubmitObjectsRequest) 
              || (registryRequest instanceof UpdateObjectsRequest)) {

                //Check access control for all refrences from this object
                checkObjectReferencesInResource(
                    context,
                    id, 
                    subjects, 
                    environment);
            }
        } 
        catch (URISyntaxException e) {
            throw new RegistryException(e);
        }
    }
    
    private void checkObjectReferencesInResource(ServerRequestContext context, 
        String id, 
        Set subjects, 
        Set environment)
        throws RegistryException {
            
        try {
            
            //Check if object being submitted has any reference actions that need to be checked.
            RegistryObjectType ro = (RegistryObjectType)context.getSubmittedObjectsMap().get(id);
            Set refInfos = bu.getObjectRefsInRegistryObject(ro, context.getIdMap(), new HashSet(), 1);

            Iterator iter = refInfos.iterator();
            while (iter.hasNext()) {
                ReferenceInfo refInfo = (ReferenceInfo)iter.next();
                
                Set actionAttributes = getReferenceActionAttributes(refInfo);
                if ((actionAttributes != null) && (actionAttributes.size() > 0)) {
                    
                    try {
                        checkAuthorizationForResource(context, refInfo.targetObject, 
                            subjects, actionAttributes, environment);                        
                    } catch (UnauthorizedRequestException e) {
                        String msg = getExceptionMessage("message.error.authorization.referenceToDeprecatedObjectDenied", id, context.getUser(), getActionString(actionAttributes));
                        
                        //?? This is a JAXR spec bug that we do not send an UnauthorizedRequestException
                        // IllegalStateException constructor changed to not include the cause, since that won't compile under JDK 1.4
                        // TODO: Use reflection to issue this call to account for differences between JDK 1.4 and 1.5 constructor parameters?
                        // throw new IllegalStateException(msg, e);
                        throw new IllegalStateException(msg);
                    }
                }
                
                if (ro instanceof AssociationType1) {
                    AssociationType1 ass = (AssociationType1)ro;
                    String assocType = ass.getAssociationType();
                    //Check to prevent multiple AccessControlFileFor associations with same targetObject
                    if ((assocType!= null) && assocType.equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor)) {
                        checkAccessControlPolicyForAssociation(context, ass);
                    }
                    
                    //Check to prevent HasFederationMember assoc to be submitted by non-admin
                    if ((assocType!= null) && assocType.equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember)) {
                        if (!(context.isRegistryAdministrator())) {
                            String msg = getExceptionMessage("message.error.authorization.allowedOnlyToAdmin.defineFederation", id, context.getUser(), getActionString(actionAttributes));
                            throw new UnauthorizedRequestException(id, context.getUser().getId(), getActionString(actionAttributes), msg);
                        }
                    }                    
                }
            }
        } catch (AuthorizationException e) {
            throw e;
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    private void checkAccessControlPolicyForAssociation(ServerRequestContext context, AssociationType1 ass) throws RegistryException {
        try {
            //Make sure that only owner or admin is submitting AccessControlPolicyFor Association
            String protectedResourceId = ass.getTargetObject();
            String protectedResourceOwnerId = getRegistryObjectOwnerId(context, protectedResourceId);
            if (!(protectedResourceOwnerId.equals(context.getUser().getId()))) {
                String msg = getExceptionMessage("message.error.authorization.allowedOnlyToOwnerOrAdmin.assignACP", protectedResourceId, context.getUser());
                throw new UnauthorizedRequestException(protectedResourceId, context.getUser().getId(), "", msg);
            }
            
            //Check and prevent multiple AccessControlPolicyFor Associations for same targetObject            
            org.oasis.ebxml.registry.bindings.query.ResponseOption responseOption =
                BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ReturnType.LEAF_CLASS);
            responseOption.setReturnComposedObjects(true);

            String query =
                "SELECT ass.* from Association ass WHERE ass.targetObject = '" +
                protectedResourceId + "' AND ass.associationType = '" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor +
                "' AND ass.id != '" + ass.getId() + "' ";

            List oldAsses = PersistenceManagerFactory.getInstance()
                .getPersistenceManager().executeSQLQuery(context, query,
                responseOption, "Association", new ArrayList());

            if (oldAsses.size() > 0) {
                String msg = getExceptionMessage("message.error.authorization.AccessControlPolicyForAlreadyExists", protectedResourceId, context.getUser());
                throw new AuthorizationException(msg);
            }
        }
        catch (javax.xml.bind.JAXBException e) {
            throw new RegistryException(e);
        } 
        
    }
    
    private Set getReferenceActionAttributes(ReferenceInfo refInfo) throws RegistryException {
        Set actionAttributes = new HashSet();
        
        try {
            //The id for the "reference" action
            Attribute idAttr = new Attribute(new URI(ACTION_ATTRIBUTE_ID),
                    new URI(StringAttribute.identifier), null, null,
                    new StringAttribute(bu.ACTION_REFERENCE));
            actionAttributes.add(idAttr);

            //The sourceObject for the "reference" action
            Attribute refSourceAttr = new Attribute(new URI(ACTION_ATTRIBUTE_REFERENCE_SOURCE),
                    new URI(AnyURIAttribute.identifier), null, null,
                    new AnyURIAttribute(new URI(refInfo.sourceObject)));
             actionAttributes.add(refSourceAttr);

            //The attribute within the source object for the "reference" action.
            Attribute refSourceAttributeAttr = new Attribute(new URI(ACTION_ATTRIBUTE_REFERENCE_SOURCE_ATTRIBUTE),
                    new URI(StringAttribute.identifier), null, null,
                    new StringAttribute(refInfo.attributeName));
            actionAttributes.add(refSourceAttributeAttr);
        }
        catch (URISyntaxException e) {
            throw new RegistryException(e);
        }
        
        return actionAttributes;
    }
        
    private String getActionString(Set actions) {
        String actionStr = "";
        
        Iterator actionsIter = actions.iterator();
        while (actionsIter.hasNext()) {
            Attribute actionAttr = (Attribute)actionsIter.next();
            String attributeIdStr = actionAttr.getId().toString();
            String attributeValueStr = actionAttr.getValue().encode();
            
            if (attributeIdStr.equals(ACTION_ATTRIBUTE_ID)) {
                actionStr += " id=" + attributeValueStr;
            } else if (attributeIdStr.equals(ACTION_ATTRIBUTE_REFERENCE_SOURCE)) {
                actionStr += " sourceObject=" + attributeValueStr;                
            } else if (attributeIdStr.equals(ACTION_ATTRIBUTE_REFERENCE_SOURCE_ATTRIBUTE)) {
                actionStr += " sourceAttribute=" + attributeValueStr;                
            } else if (attributeIdStr.startsWith(ACTION_ATTRIBUTE_REFERENCE_SOURCE_ATTRIBUTE_FILTER_PREFIX)) {
                actionStr += " filter=" + attributeValueStr;                
            }
            
        }
        
        return actionStr;
    }
    
    /**
     * Gets the AttributeValue matching specified Attribute id.
     */
    private String getActionAttributeValue(Set actions, String attributeName) {
        String attributeValue = "";
        
        Iterator actionsIter = actions.iterator();
        while (actionsIter.hasNext()) {
            Attribute actionAttr = (Attribute)actionsIter.next();
            String attributeIdStr = actionAttr.getId().toString();
            String attributeValueStr = actionAttr.getValue().encode();
            
            if (attributeIdStr.equals(attributeName)) {
                attributeValue = attributeValueStr;
            }
        }
        
        return attributeValue;
    }
    
    /** 
     * Gets the RegistryObject with the specified id, if any. Use context cache for fetched objects.
     * NOTE: If 'requireExisting' is set to false AND request is submission/update, object is returned from request.
     * Otherwise it must come from registry.
     *
     * @param context The RequestContext, used for caching.
     * @param id  The UUID of a RegistryObject.
     * @param requireExisting boolean flag to require object from registry (not from submission)
     * @return The UUID of the user who owns the registry object.
     * @throws RegistryException 
     */
    RegistryObjectType getRegistryObject(ServerRequestContext context, String id, boolean requireExisting) throws RegistryException {
        return context.getRegistryObject(id, "RegistryObject", requireExisting);
    }
    
    /** Gets the id of the user who owns the registry object with the specified
      * id.
      *
      * @param id
      *     The UUID of a RegistryObject.
      * @return
      *     The UUID of the user who owns the registry object.
      * @throws RegistryException
      *     Thrown if no owner can be found for the registry object.
      */
    private HashMap getOwnersMap(ServerRequestContext context, List ids) throws RegistryException {

        PersistenceManager pm = 
            PersistenceManagerFactory.getInstance().getPersistenceManager();
        HashMap ownersMap = pm.getOwnersMap(context, ids);

        //If Object not it registry and If request is submit or update then use caller as owner
        //but only if caller is not RegistryGuest
        
        Iterator iter = ids.iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            if (!(ownersMap.containsKey(id))) {
                //Object has no owner yet. Must not be in registry
                
                if ((context.getCurrentRegistryRequest() instanceof SubmitObjectsRequest) 
                    || (context.getCurrentRegistryRequest() instanceof UpdateObjectsRequest)) {
                  //Request is submit or update
                    
                    if (!(context.getUser().getId().equals(ac.ALIAS_REGISTRY_GUEST))) {
                        String ownerId = context.getUser().getId();
                        ownersMap.put(id, ownerId);
                    } else {
                        // throw exception, cannot continue or will return null
                        String msg = getExceptionMessage("message.error.authorization.unauthenticatedUser", id.toString());
                        throw new UnauthorizedRequestException((String)id, context.getUser().getId(), "", msg);
                    }
                  
                }
            }
        }
                
        //Update cache
        context.getFetchedOwners().putAll(ownersMap);
        return ownersMap;
    }    
    
    /** Gets the id of the user who owns the registry object with the specified
      * id.
      *
      * @param id
      *     The UUID of a RegistryObject.
      * @return
      *     The UUID of the user who owns the registry object.
      * @throws RegistryException
      *     Thrown if no owner can be found for the registry object.
      */
    private String getRegistryObjectOwnerId(ServerRequestContext context, String id) throws RegistryException {
        //Check cache first
        String ownerId = (String)context.getFetchedOwners().get(id);
        if (ownerId != null) {
            return ownerId;
        }
        
        //Check if object is already in Registry
        RegistryObjectType ro = null;
        try {
            ro = getRegistryObject(context, id, true);
        } catch (ObjectNotFoundException e) {
            //Not in registry.
        }
        if (ro == null) {
            //Object not it registry. If request is submit or update then use caller as owner
            if ((context.getCurrentRegistryRequest() instanceof SubmitObjectsRequest) 
                || (context.getCurrentRegistryRequest() instanceof UpdateObjectsRequest)) {
              
                //Dont let RegistryGuest be owner
                //Without this extra check RegistryGuest was able to save
              
                if (!(context.getUser().getId().equals(ac.ALIAS_REGISTRY_GUEST))) {
                    ownerId = context.getUser().getId();
                    return ownerId;
                } else {
                    // throw exception, cannot continue or will return null
                    String msg = getExceptionMessage("message.error.authorization.unauthenticatedUser", id.toString());
                    throw new UnauthorizedRequestException(id, context.getUser().getId(), "", msg);
                }
            }
        }
        
        //Object SHOULD be in registry already. Get owner from registry.
        //Note that RemoveObjectsRequest for an object that is not in registry
        //is a case where object may not be in the registry.
        List ids = new ArrayList();
        ids.add(id);

        PersistenceManager pm = 
            PersistenceManagerFactory.getInstance().getPersistenceManager();
        HashMap ownersMap = pm.getOwnersMap(context, ids);
        ownerId = (String)ownersMap.get(id);

        //Special case for getting RegistryOperator as owner of AuditableEvents.
        if ((ownerId == null) && (ro !=null)) {

            String className = "Unknown";  
            className = ro.getClass().getName();
            
            //AuditableEvents owner is undefined and is implicitly RegistryOperator
            if (ro instanceof AuditableEventType) {
                ownerId = ac.ALIAS_REGISTRY_OPERATOR;
            } else {                
                //throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ownersNotFound", new Object[]{id,className}));
                //This can happen for transient objects created and returned by special queries such as Export Query
                ownerId = ac.ALIAS_REGISTRY_GUEST;
            }                
        }

        //Update cache
        context.getFetchedOwners().put(id, ownerId);
        return ownerId;
    }
    
    
    /*
     * Utility method to get ServerRequestContext from xacml EValuationContext
     */
    static ServerRequestContext getRequestContext(EvaluationCtx context) {
        
        Object obj = null;
        try {
            if (context != null) {
                EvaluationResult result = context.getEnvironmentAttribute
                (new URI(ObjectAttribute.identifier), new URI(AuthorizationServiceImpl.ENVIRONMENT_ATTRIBUTE_REQUEST_CONTEXT), null);
                AttributeValue attrValue = result.getAttributeValue();
                BagAttribute bagAttr = (BagAttribute)attrValue;
                if (bagAttr.size() == 1) {
                    Iterator iter = bagAttr.iterator();
                    ObjectAttribute objAttr = (ObjectAttribute)iter.next();
                    if (objAttr != null) {
                        obj = objAttr.getValue();
                    }
                }
            } else {
                //This path is only used by unit tests like AssociationExistsFunctionTest which do not have an EvaluationCtx to pass.
                obj = new ServerRequestContext("org.freebxml.omar.server.security.authorization:AuthorizationServiceImpl:getRequestContext", null);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } 
        
        return (ServerRequestContext)obj;
    }
    
    private String getExceptionMessage(String messageId, String objectId) {
        return getExceptionMessage(messageId, objectId, null, null, null);
    }
    
    private String getExceptionMessage(String messageId, String objectId, UserType user) {
        return getExceptionMessage(messageId, objectId, user, null, null);
    }
    
    private String getExceptionMessage(String messageId, String objectId, UserType user, String action) {
        return getExceptionMessage(messageId, objectId, user, action, null);
    }
    
    private String getExceptionMessage(String messageId, String objectId, UserType user, String action, String info) {
        List params = new ArrayList();
        if (objectId != null) {
            params.add(objectId);
        }
        if (user != null) {
            params.add(getUserInfo(user));
        }
        if (action != null) {
            params.add(action);
        }
        if (info != null) {
            params.add(info);
        }
        String msg = ServerResourceBundle.getInstance().getString(messageId, params.toArray());
        
        return msg;
    }
    
    private String getUserInfo(UserType user) {
        String userInfo=" id=" + user.getId();
        
        try {
            PersonNameImpl personName = new PersonNameImpl(null, null, user.getPersonName());
            userInfo += " name=" + personName.getFormattedName();
        } catch (Exception e) {
            log.warn(e, e);
        }
        
        return userInfo;
    }
}
