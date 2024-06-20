<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<html>
<%-- //TODO: localize this page: --%>

<head><title>Registry Browser App</title></head>
<body>

  <h1>Registry Browser App</h1>
  
  <p>Registry Browser Application can be started with Java WebStart:</p>
  
  <p><a href="registryBrowser-jnlp.jsp">Launch Registry Browser</a></p>

  <p>In case the application fails to start you may need to install/configure
  Java WebStart (<a href="http://java.sun.com/products/javawebstart/">
  http://java.sun.com/products/javawebstart/</a>) and consult the 
  <a href="http://java.sun.com/products/javawebstart/faq.html">FAQ</a>.</p>

  <h3>Configuring Proxy Settings</h3>

  <p>Note that if you are running inside a company firewall you will need to set 
  proxy settings in the Java Web Start Application Manager:
  <ul>
  <li>Start the Java Web Start Application Manager: $JDK_HOME/jre/javaws/javaws</li>
  <li>Use File => Preferences => General and choosing "User Browser" or "Manual"</li>
  </ul>
  </p>

</body>
</html>
