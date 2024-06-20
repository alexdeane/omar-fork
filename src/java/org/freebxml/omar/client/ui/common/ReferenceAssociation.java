/*
 * ReferenceAssociation.java
 *
 * Created on January 27, 2005, 12:33 PM
 */

package org.freebxml.omar.client.ui.common;

import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.BusinessLifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.BusinessQueryManagerImpl;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author Anand
 */

public class ReferenceAssociation {
    public static final String RELATIONSHIP_TYPE_ASSOCIATION = "Association";
    public static final String RELATIONSHIP_TYPE_REFERENCE = "Reference";
    private static final String REL_CLASSIFICATION_RO = "classifiedObject";
    private static final String REL_CLASSIFICATION_SCHEME = "classificationScheme";
    private static final String REL_CLASSIFICATION_CONCEPT = "concept";
    private static final String REL_CONCEPT_PARENT = "parent";
    private static final String REL_EXTERNALID_IDSCHEME = "identificationScheme";
    private static final String REL_ORGANIZATION_CHILDORGS = "childOrganizations";
    private static final String REL_ORGANIZATION_CONTACT = "primaryContact";
    private static final String REL_BINDING_SPECLINKS = "specificationLinks";
    private static final String REL_SERVICE_BINDINGS = "serviceBindings";
    private static final String REL_SPECLINK_SPEC = "specificationObject";
    private Log log = LogFactory.getLog(this.getClass());
    private String referenceStatus = null;
    private RegistryObject src = null;
    private RegistryObject target = null;
    private String relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;
    private ArrayList map = new ArrayList();
    //String[] refAttributes = {  };
    private String refAttribute = null;
    boolean isCollectionRef = false;
    private String[] referenceAttributes = {  };    
    private String[][][] refMatrix = {
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //0 - Association
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //1 - AuditableEvent
        {
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_SCHEME, REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_CONCEPT, REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO },
            { REL_CLASSIFICATION_RO }
        },
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //3 - ClassificationScheme
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //4 - Concept
        {
            {  },
            {  },
            {  },
            { REL_EXTERNALID_IDSCHEME },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //5 - ExternalIdentifier
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //6 - ExternalLink
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //7 - ExtrinsicObject
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            { REL_ORGANIZATION_CHILDORGS },
            {  },
            {  },
            {  },
            {  },
            { REL_ORGANIZATION_CONTACT }
        }, //8 - Organization
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //9 - RegistryPackage
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            { REL_BINDING_SPECLINKS },
            {  }
        }, //10 - ServiceBinding
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            { REL_SERVICE_BINDINGS },
            {  },
            {  },
            {  }
        }, //11 - Service
        {
            {  },
            {  },
            {  },
            {  },
            { REL_SPECLINK_SPEC },
            {  },
            {  },
            { REL_SPECLINK_SPEC },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //12- SpecificationLink
        {
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  },
            {  }
        }, //13 - User
    };

    
    /** Creates a new instance of ReferenceAssociation */

    public ReferenceAssociation(RegistryObject src, RegistryObject target) {

        this.src = src;
        this.target = target;
        
        initMap();
        
        String[] refs = possibleRelations(src, target);
        try {

            if (refs.length == 0) {
                  this.referenceStatus = "Association";
            } else {
                relationshipType = RELATIONSHIP_TYPE_REFERENCE;
                this.referenceStatus = "Reference";
                setReferenceAttribute(refs);
            }
         }catch (Exception ex){
            log.error("Failed while initialize ReferenceAssociation", ex);
         }
    }

    private void initMap() {
        map.add("Association"); //0
        map.add("AuditableEvent"); //1
        map.add("Classification"); //2
        map.add("ClassificationScheme"); //3
        map.add("Concept"); //4
        map.add("ExternalIdentifier"); //5
        map.add("ExternalLink"); //6
        map.add("ExtrinsicObject"); //7
        map.add("Organization"); //8
        map.add("RegistryPackage"); //9
        map.add("ServiceBinding"); //10
        map.add("Service"); //11
        map.add("SpecificationLink"); //12
        map.add("User"); //13
    }
    
    public String[] possibleRelations(RegistryObject src, RegistryObject target) {
        if (map.size() == 0) {
            initMap();
        }
        
        String refs[] = refMatrix[0][0];
        int row = map.indexOf(getJAXRName(src));
        int col = map.indexOf(getJAXRName(target));
        if(row >= 0 && col >= 0){ 
           refs = refMatrix[row][col];
        }
        return refs;
    }
            
    public String getReferenceStatus(){
        return this.referenceStatus;
    }
    
    

    public String getRelationshipType() {
        return relationshipType;
    }


    String getJAXRName(RegistryObject ro) {
        String newClassName = ro.getClass().getName();
        newClassName = newClassName.substring(newClassName.lastIndexOf(".") +
                1);

        if (newClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            newClassName = newClassName.substring(0, newClassName.length() - 4);
        }

        return newClassName;
    }

    
    public void setReferenceAttributeOnSourceObject() throws JAXRException {
        String referenceAttribute = this.getReferenceAttribute();

        //Now use Refelection API to add target to src
        try {
            Class srcClass = src.getClass();
            Class targetClass = target.getClass();
            Class registryObjectClass = null;

            String targetInterfaceName = targetClass.getName();
            targetInterfaceName = targetInterfaceName.substring(targetInterfaceName.lastIndexOf(
                        ".") + 1);

            if (targetInterfaceName.endsWith("Impl")) {
                //Remove Impl suffix for JAXR provider Impl classes
                targetInterfaceName = targetInterfaceName.substring(0,
                        targetInterfaceName.length() - 4);
            }

            targetInterfaceName = "javax.xml.registry.infomodel." +
                targetInterfaceName;

            ClassLoader classLoader = srcClass.getClassLoader();

            try {
                targetClass = classLoader.loadClass(targetInterfaceName);
                registryObjectClass = classLoader.loadClass(
                        "javax.xml.registry.infomodel.RegistryObject");
            } catch (ClassNotFoundException e) {
                throw new JAXRException("No JAXR interface found by name " +
                    targetInterfaceName);
            }

            String suffix = UIUtility.getInstance().initCapString(referenceAttribute);
            Method method = null;
            Class[] paramTypes = new Class[1];

            //See if there is a simple attribute of this name using type of targetObject
            try {
                paramTypes[0] = targetClass;
                method = srcClass.getMethod("set" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = false;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //See if there is a simple attribute of this name using base type RegistryObject
            try {
                paramTypes[0] = registryObjectClass;
                method = srcClass.getMethod("set" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = false;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //See if there is a addCXXX method for suffix of XXX ending in "s" for plural
            if (suffix.endsWith("s")) {
                suffix = suffix.substring(0, suffix.length() - 1);
            }

            try {
                paramTypes[0] = targetClass;
                method = srcClass.getMethod("add" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = true;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //See if there is a addCXXX method for suffix of XXX ending in "es" for plural
            if (suffix.endsWith("e")) {
                suffix = suffix.substring(0, suffix.length() - 1);
            }

            try {
                paramTypes[0] = targetClass;
                method = srcClass.getMethod("add" + suffix, paramTypes);

                Object[] params = new Object[1];
                params[0] = target;
                method.invoke(src, params);
                isCollectionRef = true;

                return;
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
            }

            //Special case while adding child organization to an organization
            if (src instanceof Organization && target instanceof Organization) {
                try {
                    paramTypes[0] = targetClass;
                    method = srcClass.getMethod("addChildOrganization", paramTypes);

                    Object[] params = new Object[1];
                    params[0] = target;
                    method.invoke(src, params);
                    isCollectionRef = true;

                    return;
                } catch (NoSuchMethodException e) {
                } catch (IllegalAccessException e) {
                }
            }
            throw new JAXRException("No method found for reference attribute " +
                referenceAttribute + " for src object of type " +
                srcClass.getName());
        } catch (IllegalArgumentException e) {
            throw new JAXRException(e);
        } catch (InvocationTargetException e) {
            throw new JAXRException(e.getCause());
        } catch (ExceptionInInitializerError e) {
            throw new JAXRException(e);
        }
    }

   
    public String getReferenceAttribute() {
       return this.refAttribute;
    }

    public void setReferenceAttribute(String refAttribute) {
        this.refAttribute = refAttribute;
    }

    public String[] getReferenceAttributes() {
       return this.referenceAttributes;
    }

    public void setReferenceAttribute(String referenceAttributes[]) {
        this.referenceAttributes = referenceAttributes;
    }

}
