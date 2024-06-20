<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<jsp:useBean id="registryBrowser" 
    class="org.freebxml.omar.client.ui.thin.RegistryBrowser" 
    scope="session" />
<% 
String url = registryBrowser.getStandardContextPath().toString()+"/registry/thin/EndSessionMessage.jsp";
registryBrowser.doEndSession();
response.sendRedirect(url);
%>

    
