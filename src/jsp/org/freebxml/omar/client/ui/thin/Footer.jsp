<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:subview id="footerView">
  <h:panelGrid id="footerPanel" cellspacing="0" cellpadding="2" 
    columns="1" width="100%" columnClasses="centerColumn,centerColumn" rowClasses="centerColumn">
        <c:if test="${registryBrowser.aboutEbxmlDisplayed}">
        <f:verbatim><hr></f:verbatim>
        <h:outputLink id="aboutebXMLLink"
           value="#{registryBrowser.aboutEbxml}" target="_new">
           <h:outputText id="aboutebXMLOut" value="#{bundle.aboutebXML}"/>
        </h:outputLink>     
        </c:if>        
        <h:outputText escape="false" id="footerCopyrightOut" value="#{registryBrowser.companyCopyright}"/>
        <c:if test="${registryBrowser.aboutRegistryDisplayed}">
        <h:outputLink id="aboutRegistry" 
           value="#{registryBrowser.aboutRegistry}" target="_new">
           <h:outputText id="aboutRegistryOut" value="#{bundle.aboutRegistry}"/>
           </h:outputLink> 
        </c:if>
  </h:panelGrid>
</f:subview>
