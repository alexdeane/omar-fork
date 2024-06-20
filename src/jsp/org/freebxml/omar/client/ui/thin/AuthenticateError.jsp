<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ page import="org.freebxml.omar.client.ui.thin.WebUIResourceBundle" %>

<html>
<head> 
</head>
<body bgcolor='#ffffff'>
    <jsp:useBean id="registryBrowser" class="org.freebxml.omar.client.ui.thin.RegistryBrowser" scope="session"/>
<% 
    registryBrowser.setSessionExpired(false);
    if (registryBrowser.getAtAuthentication()) {
        registryBrowser.reportAuthenticationFailure("cert");
    } else {
        registryBrowser.setErrorMessage(WebUIResourceBundle.getInstance().getString("400Message"));
    }
%>
 <jsp:forward page="/registry/thin/ErrorPage.jsp" />
</body>
</html>

    
