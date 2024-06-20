<!-- $Header: /cvsroot/ebxmlrr/omar/src/jsp/org/freebxml/omar/client/ui/swing/registryBrowser-jnlp.jsp,v 1.2 2005/10/28 06:52:21 dougb62 Exp $ -->
<%@page contentType="application/x-java-jnlp-file"%>
<%@page pageEncoding="UTF-8"%>
<% // Document Base is the context
   String documentBase = (new java.net.URL(request.getScheme(),
        request.getServerName(), request.getServerPort(), 
        request.getContextPath()+"/")).toString();
   response.setHeader("Content-Disposition", "attachment; filename=\"registryBrowser.jnlp\"");   
%>
<jnlp codebase="<%=documentBase%>browser/jnlp/" href="registryBrowser-jnlp.jsp" spec="1.0+">
  <information>
    <title>freebXML Registry 3.0: Registry Browser (source: <%=documentBase%>)</title>
    <vendor>freebXML Registry</vendor>
    <homepage href="http://ebxmlrr.sourceforge.net"/>
    <description>A Browser that works with any registry compliant with ebXML Registry 3.0 specifications that support SQL Query optional fetaure.</description>
    <!--icon href="images/registryBrowser.jpg"/-->
    <offline-allowed/>
  </information>
  <security>
    <all-permissions/>
  </security>
  <resources>
    <%-- //TODO: build this list dinamically, with iterate tag + classpath property --%>
    <j2se version="1.5+" href="http://java.sun.com/products/autodl/j2se"/>
    <jar href="registry-browser.jar" main="true"/>
    <jar href="FastInfoset.jar"/>
    <jar href="activation.jar"/>
    <jar href="commons-logging.jar"/>
    <jar href="dom.jar"/>
    <jar href="jax-qname.jar"/>
    <jar href="jaxb-api.jar"/>
    <jar href="jaxb-impl.jar"/>
    <jar href="jaxb-libs.jar"/>
    <jar href="jaxb-xjc.jar"/>
    <jar href="jaxp-api.jar"/>
    <jar href="jaxr-api.jar"/>
    <jar href="jaxr-ebxml.jar"/>
    <jar href="jgraph.jar"/>
    <jar href="log4j.jar"/>
    <jar href="mail.jar"/>
    <jar href="namespace.jar"/>
    <jar href="oasis-regrep.jar"/>
    <jar href="omar-common.jar"/>
    <jar href="omar-ui-conf-bindings.jar"/>
    <jar href="relaxngDatatype.jar"/>
    <jar href="saaj-api.jar"/>
    <jar href="saaj-impl.jar"/>
    <jar href="xalan.jar"/> 
    <jar href="xercesImpl.jar"/> 
    <jar href="xmldsig.jar"/>
    <jar href="xmlsec.jar"/>
    <jar href="xsdlib.jar"/>
    <jar href="xws-saml.jar"/>    
    <jar href="xws-security.jar"/>    
  </resources>
  <application-desc main-class="org.freebxml.omar.client.ui.swing.RegistryBrowser"/>
</jnlp>
