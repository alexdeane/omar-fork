<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <!--
   Copyright 2001-2004 The Apache Software Foundation
   Copyright 2005 freebXML.org

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 -->

  <!--

 Customization to sample stylesheet to be used with Ant JUnitReport output.
 Based on JUnit default stylesheet.
 
 @author Smitha Prabhu
 @author Tony Graham

  $Id: junit-frames.xsl,v 1.1 2005/10/10 10:28:02 sunsmitha Exp $
  -->

  <xsl:import href="../default/junit-frames.xsl"/>
  <xsl:import href="common.xsl"/>
  
  <xsl:key name="property-by-name" match="property" use="@name" />
<xsl:template match="testsuites" mode="overview.packages">
    <html>
        <head>
            <title>Unit Test Results: Summary</title>
            <xsl:call-template name="create.stylesheet.link">
                <xsl:with-param name="package.name"/>
            </xsl:call-template>
        </head>
        <body>
        <xsl:attribute name="onload">open('allclasses-frame.html','classListFrame')</xsl:attribute>
        <xsl:call-template name="pageHeader"/>

        <xsl:call-template name="summary"/>
        
        <h2>Packages</h2>
        <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
            <xsl:call-template name="testsuite.test.header"/>
            <xsl:for-each select="testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
                <xsl:sort select="@package" order="ascending"/>
                <!-- get the node set containing all testsuites that have the same package -->
                <xsl:variable name="insamepackage" select="/testsuites/testsuite[./@package = current()/@package]"/>
                <tr valign="top">
                    <!-- display a failure if there is any failure/error in the package -->
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="sum($insamepackage/@errors) &gt; 0">Error</xsl:when>
                            <xsl:when test="sum($insamepackage/@failures) &gt; 0">Failure</xsl:when>
                            <xsl:otherwise>Pass</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <td><a href="./{translate(@package,'.','/')}/package-summary.html">
                        <xsl:value-of select="@package"/>
                        <xsl:if test="@package = ''">&lt;none&gt;</xsl:if>
                    </a></td>
                    <td><xsl:value-of select="sum($insamepackage/@tests)"/></td>
                    <td><xsl:value-of select="sum($insamepackage/@errors)"/></td>
                    <td><xsl:value-of select="sum($insamepackage/@failures)"/></td>
                    <td>
                    <xsl:call-template name="display-time">
                        <xsl:with-param name="value" select="sum($insamepackage/@time)"/>
                    </xsl:call-template>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
        </body>
        </html>
</xsl:template>
</xsl:stylesheet>

