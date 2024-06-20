<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<f:view locale="#{userPreferencesBean.uiLocale}">
<html>
<head> 
<title><h:outputText value="#{registryBrowser.browserTitle}"/></title> 
<link rel="stylesheet" type="text/css" href='<%= request.getContextPath() + "/" %><h:outputText value="#{registryBrowser.cssFile}"/>'>
<META HTTP-EQUIV="Expires" CONTENT="Sat, 6 May 1995 12:00:00 GMT">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-store, no-cache, must-revalidate">
<META HTTP-EQUIV="Cache-Control" CONTENT="post-check=0, pre check=0">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
</head>
<f:loadBundle basename="org.freebxml.omar.client.ui.thin.ResourceBundle" var="bundle"/>
<body bgcolor='#ffffff'>
    <f:verbatim><br><br><br><br></f:verbatim>
    <h:panelGrid id="endSessionWrapperPanel" cellspacing="0" cellpadding="0" width="100%">
        <f:verbatim><br><br><br><br></f:verbatim>
        <h:outputText id="sessionEndedOut" value="#{bundle.sessionEnded}" 
            styleClass="h2"/>
        <f:verbatim><br></f:verbatim>
        <f:verbatim><br></f:verbatim>
        <h:form id="endSessionForm">
            <h:panelGrid id="endSessionPanel" columns="3">
            <h:outputLink id="endSessionRegLink" 
                value="#{facesContext.externalContext.request.contextPath}/registry/thin/WelcomePage.jsp">
                <h:outputText id="endSessionRegOut" value="#{bundle.returnToRegistry}"/>
            </h:outputLink>             
            <h:outputText id="endSessionMessageOut" value="#{bundle.startNewSession}"/>
            </h:panelGrid>
        </h:form>
    </h:panelGrid>
</body>
</html>
</f:view> 
    
