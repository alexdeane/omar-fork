<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms"
    exclude-result-prefixes="chiba xforms xlink xsl">

    <xsl:import href="html-form-controls.xsl"/>
    <xsl:import href="util.xsl"/>

    <xsl:output method="html" encoding="ISO-8859-1" omit-xml-declaration="yes"/>

    <xsl:param name="action-url" select="''"/>
    <xsl:param name="form-id" select="'chiba-form'"/>
    <xsl:param name="form-name" select="'Chiba XForms Processor'"/>
    <xsl:param name="debug-enabled" select="'false'"/>
    <xsl:param name="selector-prefix" select="'s_'"/>
    <xsl:param name="first-column-width" select="'25%'"/>


    <!-- match html or other surrounding markup -->
    <xsl:template match="/">
        <xsl:message>hello new stylesheet ...</xsl:message>
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="html">
        <html>
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="head">
        <head>
            <title><xsl:value-of select="$form-name"/></title>
            <xsl:apply-templates/>
        </head>
    </xsl:template>

    <!-- copy body and build form -->
    <xsl:template match="body">
        <xsl:copy>
            <xsl:attribute name="bgcolor">lightgrey</xsl:attribute>
        </xsl:copy>
        <xsl:call-template name="build-form"/>
    </xsl:template>

    <xsl:template match="chiba:data"/>
    <xsl:template match="xforms:choices"/>

    <!-- copy unmatched mixed markup, comments, whitespace, and text -->
    <xsl:template match="*|@*|text()">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>copying &apos;<xsl:value-of select="name(.)"/>&apos; ...</xsl:message>
        </xsl:if>

        <xsl:copy>
            <xsl:apply-templates select="*|@*|text()"/>
        </xsl:copy>
    </xsl:template>

    <!-- skip explicitely disabled control -->
    <xsl:template match="*[chiba:data/@chiba:enabled='false']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>disabling &apos;<xsl:value-of select="name(.)"/>&apos; ...</xsl:message>
        </xsl:if>
    </xsl:template>

    <!-- skip model section -->
    <xsl:template match="xforms:model">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>skipping model section ...</xsl:message>
        </xsl:if>
    </xsl:template>

    <!-- handle group -->
    <xsl:template match="xforms:group" name="group">
        <xsl:param name="span"/>
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling group <xsl:value-of select="xforms:label"/>...</xsl:message>
        </xsl:if>

        <table border="0" cellspacing="1" cellpadding="6" width="95%">
            <!-- handling group label -->
            <xsl:if test="xforms:label">
                <xsl:message>handling outergroup label ...</xsl:message>
                <tr>
                    <td bgcolor="9999CC" colspan="{$span}"><b><xsl:apply-templates select="xforms:label"/></b></td>
                </tr>
            </xsl:if>
            <!-- handling group children -->
            <tr>

                <td bgcolor="#CCCCFF" colspan="{$span}" align="left">

                    <xsl:for-each select="*">
                        <xsl:choose>

                            <xsl:when test="name(.)='xforms:label'">
                                <xsl:message>found group label - ignoring...</xsl:message>
                            </xsl:when>

                            <xsl:when test="name(.)='xforms:group'">
                                <xsl:message>found inner group</xsl:message>

                                    <xsl:call-template name="group" >
                                        <xsl:with-param name="span" select="2"/>
                                    </xsl:call-template>

                            </xsl:when>

                            <xsl:when test="name(.)='xforms:repeat'">
                                <xsl:message>found repeat child</xsl:message>
                                <xsl:apply-templates select="."/>
                            </xsl:when>

                            <xsl:when test="name(.)='xforms:trigger'">
                                <xsl:message>handling trigger:<xsl:value-of select="xforms:label"/></xsl:message>
                                <xsl:apply-templates select="."/>
                            </xsl:when>

                            <xsl:when test="xforms:*">
                                <xsl:if test="not(chiba:data/@chiba:enabled='false')">
                                <xsl:message>handling control</xsl:message>
                                <xsl:message><xsl:value-of select="name()"/>-<xsl:value-of select="xforms:label"/></xsl:message>
                                <table width="95%" border="0" cellspacing="1" cellpadding="1">
                                <tr>
                                    <td width="{$first-column-width}">
                                        <xsl:apply-templates select="./xforms:label"/>
                                    </td>
                                    <td>
                                        <font size="-1">
                                        <xsl:apply-templates select="."/>
                                        </font>
                                    </td>
                                </tr>
                                </table>
                                </xsl:if>
                            </xsl:when>

                            <xsl:otherwise>
                                <xsl:apply-templates/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </td>
            </tr>
        </table>
    </xsl:template>

    <!-- handle repeat -->
    <xsl:template match="xforms:repeat">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling repeat ...</xsl:message>
        </xsl:if>

        <xsl:variable name="fieldcount" select="count(./*)"/>

        <table border="0" cellspacing="1" cellpadding="1" width="95%">
            <xsl:apply-templates />
        </table>
    </xsl:template>

    <xsl:template match="*[@xforms:repeat-bind]|*[@xforms:repeat-nodeset]">
        <xsl:message>processing table repeat ...</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="tr">
        <xsl:message>processing table row ...</xsl:message>

        <xsl:copy>
            <xsl:call-template name="copy-attributes"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="td">
        <xsl:message>processing table cell ...</xsl:message>
        <xsl:copy>
            <xsl:call-template name="copy-attributes"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xforms:group[@chiba:transient='true']" priority="1">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>processing transient group ...</xsl:message>
        </xsl:if>

        <xsl:for-each select="*">
            <xsl:message>processing repeat child ...</xsl:message>
            <xsl:choose>
                <xsl:when test="xforms:*">
                    <tr>
                        <td width="{$first-column-width}" valign="middle">
                            <nobr><xsl:call-template name="build-Selector" /><xsl:apply-templates select="./xforms:label"/></nobr>
                        </td>
                        <td>
                            <xsl:apply-templates select="."/>
                        </td>
                    </tr>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="."/>
                </xsl:otherwise>
            </xsl:choose>
<!--
            <xsl:if test="xforms:*">
                <tr>
                    <td>
                        <xsl:call-template name="build-Selector" />
                    </td>
                    <td>
                        <xsl:apply-templates select="./xforms:label"/>
                    </td>
                    <td>
                        <xsl:apply-templates select="."/>
                    </td>
                </tr>
            </xsl:if>
-->
        </xsl:for-each>
    </xsl:template>

    <!-- handle switch -->
    <xsl:template match="xforms:switch">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling switch ...</xsl:message>
        </xsl:if>

        <xsl:apply-templates/>
    </xsl:template>

    <!-- handle selected case -->
    <xsl:template match="xforms:case[@xforms:selected='true']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling selected case ...</xsl:message>
        </xsl:if>

        <xsl:apply-templates />
    </xsl:template>


    <!-- skip unselected case -->
    <xsl:template match="xforms:case">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>skipping unselected case ...</xsl:message>
        </xsl:if>
    </xsl:template>

<!-- ************************ CONTROLS ***************************************** -->
    <!-- handle select -->
    <xsl:template match="xforms:select">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling select ...</xsl:message>
        </xsl:if>

        <!--<xsl:apply-templates select="xforms:label"/>-->
        <xsl:call-template name="select">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="appearance" select="@xforms:appearance"/>
        </xsl:call-template>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle select1 -->
    <xsl:template match="xforms:select1">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling select1 ...</xsl:message>
        </xsl:if>

        <!--<xsl:apply-templates select="xforms:label"/>-->

        <xsl:call-template name="select1">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="appearance" select="@xforms:appearance"/>
        </xsl:call-template>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle input -->
    <xsl:template match="xforms:input">
        <xsl:param name="repeated" />

        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling input ...</xsl:message>
        </xsl:if>

        <nobr>
        <xsl:call-template name="input">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="size" select="60"/>
        </xsl:call-template>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
        </nobr>

    </xsl:template>

    <!-- handle textarea -->
    <xsl:template match="xforms:textarea">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling textarea ...</xsl:message>
        </xsl:if>

        <xsl:call-template name="textarea">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="rows" select="3"/>
            <xsl:with-param name="cols" select="60"/>
        </xsl:call-template>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle secret -->
    <xsl:template match="xforms:secret">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling secret ...</xsl:message>
        </xsl:if>


        <xsl:call-template name="secret">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
            <xsl:with-param name="value" select="chiba:data"/>
            <xsl:with-param name="size" select="60"/>
        </xsl:call-template>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle output -->
    <xsl:template match="xforms:output">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling output ...</xsl:message>
        </xsl:if>


        <xsl:value-of select="chiba:data"/>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle upload -->
    <xsl:template match="xforms:upload">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling upload ...</xsl:message>
        </xsl:if>

        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling upload not implemented yet ...</xsl:message>
        </xsl:if>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle range -->
    <xsl:template match="xforms:range">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling range ...</xsl:message>
        </xsl:if>

        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling range not implemented yet ...</xsl:message>
        </xsl:if>

        <xsl:apply-templates select="xforms:help"/>
        <xsl:apply-templates select="xforms:alert"/>
    </xsl:template>

    <!-- handle trigger / submit -->
    <xsl:template match="xforms:trigger|xforms:submit">
        <font size="-1">
        <xsl:call-template name="trigger">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
        </xsl:call-template>
        </font>
    </xsl:template>

    <!-- handle image trigger -->
    <xsl:template match="xforms:trigger[starts-with(xforms:label/@xlink:href,'images')]">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling image trigger ...</xsl:message>
        </xsl:if>

        <xsl:call-template name="image-trigger">
            <xsl:with-param name="name" select="chiba:data/@chiba:name"/>
        </xsl:call-template>
    </xsl:template>




    <!-- skip value -->
    <xsl:template match="xforms:value">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>skipping value ...</xsl:message>
        </xsl:if>

        <!--  already handled by html-form-controls.xsl -->
    </xsl:template>

    <!-- handle label -->
    <xsl:template match="xforms:label">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>writing label ...</xsl:message>
        </xsl:if>

    <!--    <xsl:copy-of select="*|text()"/>-->
        <font size="-1">
        <xsl:apply-templates />
        </font>
    </xsl:template>

    <!-- handle hint -->
    <xsl:template match="xforms:hint">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>skipping hint ...</xsl:message>
        </xsl:if>

        <!--  already handled by html-form-controls.xsl -->
    </xsl:template>

    <!-- handle help -->
    <xsl:template match="xforms:help">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>skipping help ...</xsl:message>
        </xsl:if>

    </xsl:template>

    <!-- handle explicitely enabled alert -->
    <xsl:template match="xforms:alert[../chiba:data/@chiba:valid='false']">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling alert ...</xsl:message>
        </xsl:if>

        <xsl:call-template name="alert">
            <xsl:with-param name="message" select="text()"/>
        </xsl:call-template>
    </xsl:template>

    <!-- skip alert -->
    <xsl:template match="xforms:alert">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>skipping alert ...</xsl:message>
        </xsl:if>
    </xsl:template>

    <!-- handle extensions -->
    <xsl:template match="xforms:extension">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>handling extension ...</xsl:message>
        </xsl:if>

        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template name="build-Selector">
        <xsl:variable name="transient-group" select="ancestor::xforms:group[@chiba:transient='true'][1]"/>
        <xsl:variable name="current-position" select="$transient-group/@chiba:position"/>
        <xsl:variable name="repeat-node" select="$transient-group/../."/>
        <xsl:variable name="repeat-id" select="$repeat-node/@id"/>
        <xsl:variable name="repeat-index" select="$repeat-node/@chiba:index"/>
        <xsl:choose>
            <xsl:when test="$current-position=$repeat-index">
                <input type="radio" name="{$selector-prefix}{$repeat-id}" value="{$current-position}" checked="checked"/>
            </xsl:when>
            <xsl:otherwise>
                <input type="radio" name="{$selector-prefix}{$repeat-id}" value="{$current-position}"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="chiba:selector">
        <xsl:call-template name="build-Selector"/>
    </xsl:template>

    <!-- handle selector
    <xsl:template match="chiba:selector">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>building repeat selector ...</xsl:message>
        </xsl:if>

        <xsl:variable name="transient-group" select="ancestor::xforms:group[@chiba:transient='true'][1]"/>
        <xsl:variable name="current-position" select="$transient-group/@chiba:position"/>
        <xsl:variable name="repeat-node" select="$transient-group/../."/>
        <xsl:variable name="repeat-id" select="$repeat-node/@id"/>
        <xsl:variable name="repeat-index" select="$repeat-node/@chiba:index"/>

        <xsl:choose>
            <xsl:when test="$current-position=$repeat-index">
                <input type="radio" name="{$selector-prefix}{$repeat-id}" value="{$current-position}" checked="checked"/>
            </xsl:when>
            <xsl:otherwise>
                <input type="radio" name="{$selector-prefix}{$repeat-id}" value="{$current-position}"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>
    -->
    <!-- build form skeleton -->
    <xsl:template name="build-form">
        <xsl:if test="$debug-enabled='true'">
            <xsl:message>building form ...</xsl:message>
        </xsl:if>

<!--        <table border="0" cellspacing="0" cellpadding="0">-->
            <form name="{$form-id}" action="{$action-url}" method="post" enctype="application/x-www-form-urlencoded">
                <!-- provide a first submit which does not map to any xforms:trigger -->
                <!-- todo: find a better way -->
                <input type="image" name="dummy" style="width:0pt;height:0pt;" value="dummy"/>
                <xsl:apply-templates/>
            </form>
        <!--</table>-->
    </xsl:template>


    <!-- ########################## ACTIONS ####################################################### -->
    <!-- ########################## ACTIONS ####################################################### -->
    <!-- ########################## ACTIONS ####################################################### -->

    <!-- action nodes are simply copied to output without any modification -->
    <!--
        <xsl:template match="xforms:dispatch">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:refresh">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:recalculate">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:revalidate">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:setFocus">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:setValue">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:submitInstance">
            <xsl:copy>
                <xsl:variable name="submission" select="@xforms:submission"/>
                <xsl:for-each select="//xforms:submission[@id=$submission]/@*">
                    <xsl:copy/>
                </xsl:for-each>
            </xsl:copy>
        </xsl:template>

        <xsl:template match="xforms:resetInstance">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:insert">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:delete">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:setindex">
            <xsl:call-template name="setindex"/>
        </xsl:template>

        <xsl:template match="xforms:toggle">
            <xsl:copy-of select="."/>
        </xsl:template>

        <xsl:template match="xforms:message">
            <xsl:copy-of select="."/>
        </xsl:template>

       <xsl:template match="xforms:loadURI">
            <xsl:copy-of select="."/>
        </xsl:template>
    -->
</xsl:stylesheet>
<!--
$Log: html-standard.xsl,v $
Revision 1.1  2003/11/14 20:30:09  psterk
Initial version of XSLT files used by chiba XForms processor. This files can be modified to transform the standard chiba look and feel to omar look and feel.

Revision 1.13  2003/10/01 21:09:18  joernt
simplified/clarified action URL handling

Revision 1.12  2003/09/08 12:48:25  joernt
changed occurrences of xforms:id into id to clarify the examples (there's
nothing like a xforms:id out there).

Revision 1.11  2003/08/20 16:34:14  joernt
now only renders enabled controls

Revision 1.10  2003/08/12 13:59:02  joernt
fixes

Revision 1.9  2003/08/11 15:02:42  joernt
numerous changes to handle the new unrolled UI

Revision 1.8  2003/08/07 21:46:36  unl
- added chiba:selector support

Revision 1.7  2003/08/06 15:54:33  joernt
fixes

Revision 1.6  2003/08/05 14:01:30  joernt
integration of recalculation

Revision 1.5  2003/08/05 08:57:22  joernt
xforms + chiba namespaces adapted

Revision 1.4  2003/07/21 17:17:17  unl
- fixed ui unrolling

Revision 1.3  2003/07/16 16:27:12  unl
- fixed id handling (@id -> @id)

Revision 1.2  2003/07/14 08:28:36  unl
- fixed select/select1 handling

Revision 1.1  2003/07/12 12:26:23  joernt
package refactoring: move from xforms.xsl

Revision 1.3  2003/05/28 15:12:59  joernt
renamed xforms:value elements to chiba:data

Revision 1.2  2003/05/27 17:36:45  unl
- adapted latest changes from 0.8

Revision 1.7  2003/05/13 08:47:18  unl
- removed limitation on forms to have a body tag

Revision 1.6  2003/04/30 13:35:51  unl
- fixed switch/case handling

Revision 1.5  2003/03/21 16:27:28  joernt
fixed problem with label-copy

Revision 1.4  2003/03/18 22:00:12  joernt
patched stylesheet entry template

Revision 1.3  2003/03/04 17:18:12  unl
adapted new repeat cursor handling

Revision 1.2  2003/02/16 20:46:16  unl
fixed too much to write a log message

Revision 1.1  2003/02/07 00:10:17  joernt
stylesheet for use with DynamicUI

-->
