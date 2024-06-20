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

  $Id: common.xsl,v 1.5 2006/11/03 04:46:50 dougb62 Exp $
   -->

  <xsl:template name="summary">
    <xsl:variable name="testComment" select="testsuite/properties/property/@value[../@name='test.comment']"/>
    <xsl:variable name="testCount" select="sum(testsuite/@tests)"/>
    <xsl:variable name="errorCount" select="sum(testsuite/@errors)"/>
    <xsl:variable name="executedCount" select="$testCount - $errorCount"/>
    <xsl:variable name="failureCount" select="sum(testsuite/@failures)"/>
    <xsl:variable name="passedCount" select="$executedCount - $failureCount"/>
    <xsl:variable name="timeCount" select="sum(testsuite/@time)"/>
    <xsl:variable name="successRate" select="($testCount - $failureCount - $errorCount) div $testCount"/>
    <xsl:variable name="omar.container.url" select="testsuite/properties/property/@value[../@name='omar.container.url']"/>
    <xsl:variable name="omar.name" select="testsuite/properties/property/@value[../@name='omar.name']"/>
    <xsl:variable name="alternateBackground">rgb(255, 204, 153)</xsl:variable>
    <xsl:variable name="display-time">
      <xsl:call-template name="display-time">
        <xsl:with-param name="value" select="$timeCount"/>
      </xsl:call-template>
    </xsl:variable>
    
    <h2>Summary</h2>
    <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%" style="margin-bottom: 1.5em">
      <tr>
        <th>Registry URL</th>
        <th>Registry Name</th>
        <th>Testsuite</th>
        <th>Timestamp</th>
        <th>Client host</th>
        <th>Client OS</th>
        <th>Client JVM</th>
        <th>Tester</th>
      </tr>
      <tr>
        <td>
          <xsl:value-of select="$omar.container.url"/>
        </td>
        <td>
          <xsl:value-of select="$omar.name"/>
        </td>
        <td>
          <xsl:value-of select="testsuite/properties/property/@value[../@name='test.suite']"/>
        </td>
        <td>
          <xsl:value-of select="testsuite/properties/property/@value[../@name='test.tstamp']"/>
        </td>
        <td>
          <xsl:value-of select="testsuite/properties/property/@value[../@name='env.HOSTNAME']"/>
        </td>
        <td>
          <xsl:value-of select="testsuite/properties/property/@value[../@name='os.name']"/>
        </td>
        <td>
          <xsl:value-of select="testsuite/properties/property/@value[../@name='java.runtime.version']"/>
        </td>
        <td>
          <xsl:value-of select="testsuite/properties/property/@value[../@name='env.USER']"/>
        </td>
      </tr>
      <xsl:if test="$testComment">
        <tr>
          <th>Comment</th>
          <td colspan="7">
            <xsl:value-of select="$testComment"/>
          </td>
        </tr>
      </xsl:if>
    </table>

    <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
      <tr valign="top">
        <th>Tests</th>
        <th>Errors</th>
        <th>Executed</th>
        <th>Failures</th>
        <th>Passed</th>
        <th>Success rate</th>
        <th>Time</th>
      </tr>
      <tr valign="top">
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
            <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
            <xsl:otherwise>Pass</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <td style="background-color: {$alternateBackground};"><xsl:value-of select="$testCount"/></td>
        <td><xsl:value-of select="$errorCount"/></td>
        <td style="background-color: {$alternateBackground};"><xsl:value-of select="$executedCount"/></td>
        <td><xsl:value-of select="$failureCount"/></td>
        <td style="background-color: {$alternateBackground};"><xsl:value-of select="$passedCount"/></td>
        <td>
          <xsl:call-template name="display-percent">
            <xsl:with-param name="value" select="$successRate"/>
          </xsl:call-template>
        </td>
        <td><xsl:value-of select="$display-time"/></td>
      </tr>
    </table>

    <!-- Output significant information to the console. -->
    <xsl:message>
  Registry URL: <xsl:value-of select="$omar.container.url"/>
  Registry Name: <xsl:value-of select="$omar.name"/>
    <xsl:if test="$testComment">
  Comment: <xsl:value-of select="$testComment"/>
    </xsl:if>
  Total Tests: <xsl:value-of select="$testCount"/>
  Errors: <xsl:value-of select="$errorCount"/>
  Executed: <xsl:value-of select="$executedCount"/>
  Failures: <xsl:value-of select="$failureCount"/>
  Passed: <xsl:value-of select="$passedCount"/>
  Success Rate: <xsl:value-of select="format-number($successRate,'0.00%')"/>
  Time: <xsl:value-of select="$display-time"/><xsl:text>&#10;</xsl:text>
</xsl:message>

    <table border="0" width="95%">
      <tr>
        <td style="text-align: justify;">
          Note: <em>failures</em> are anticipated and checked for with assertions while <em>errors</em> are unanticipated.
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>

