<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="xml"/>

  <xsl:template match="/">
    <lcm:SubmitObjectsRequest
      xmlns:lcm="urn:oasis:names:tc:ebxml-regrep:xsd:lcm:3.0">
      <xsl:apply-templates/>
    </lcm:SubmitObjectsRequest>
  </xsl:template>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
