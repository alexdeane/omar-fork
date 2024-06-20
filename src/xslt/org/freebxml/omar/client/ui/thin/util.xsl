<?xml version="1.0" encoding="UTF-8"?>
<!--
    general utility templates
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xforms="http://www.w3.org/2002/xforms"
                xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms"
                exclude-result-prefixes="chiba xsl xsd xforms ev">

<xsl:output method="xml"
            indent="yes"
            omit-xml-declaration="yes"/>

<!-- ##################################### HELPER TEMPLATES ################################### -->
    <!-- copy all attributes of the context node -->
    <xsl:template name="copy-attributes">
        <xsl:for-each select="@*">
            <xsl:copy/>
        </xsl:for-each>
    </xsl:template>


</xsl:stylesheet>

