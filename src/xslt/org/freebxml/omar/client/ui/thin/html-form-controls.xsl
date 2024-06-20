<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xforms="http://www.w3.org/2002/xforms"
    xmlns:chiba="http://chiba.sourceforge.net/2003/08/xforms"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    exclude-result-prefixes="chiba xforms xlink xsl">

    <!--
        this stylesheet contains a collection of templates which map
        XForms controls to HTML controls. It works on the format generated
        by ui-prepare.xsl.

        todo:
        - upload
    -->

    <xsl:output method="html"
        indent="yes"
        omit-xml-declaration="yes"/>

    <!-- match all controls -->
    <xsl:template name="input">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:param name="size"/>
        <xsl:param name="maxlength"/>

        <xsl:element name="input">
            <xsl:attribute name="type">text</xsl:attribute>
            <xsl:attribute name="name">
                <xsl:value-of select="$name"/>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="$value"/>
            </xsl:attribute>
            <xsl:attribute name="class">input</xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="./xforms:hint"/>
            </xsl:attribute>
            <xsl:if test="$size">
                <xsl:attribute name="size">
                    <xsl:value-of select="$size"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="$maxlength">
                <xsl:attribute name="maxlength">
                    <xsl:value-of select="$maxlength"/>
                </xsl:attribute>
            </xsl:if>
        </xsl:element>
        <xsl:if test="./chiba:data/@chiba:required='true'">
            <font color="red">*</font>
        </xsl:if>
    </xsl:template>

    <xsl:template name="textarea">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:param name="rows"/>
        <xsl:param name="cols"/>

        <!--
        <xsl:text disable-output-escaping="yes">&lt;textarea&gt;</xsl:text>
        <xsl:text disable-output-escaping="yes">&lt;/textarea&gt;</xsl:text>
        -->
        <xsl:element name="textarea">
            <xsl:attribute name="name">
                <xsl:value-of select="$name"/>
            </xsl:attribute>
            <xsl:attribute name="class">textarea</xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="./xforms:hint"/>
            </xsl:attribute>
            <xsl:if test="$rows">
                <xsl:attribute name="rows">
                    <xsl:value-of select="$rows"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="$cols">
                <xsl:attribute name="cols">
                    <xsl:value-of select="$cols"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$value"/>
        </xsl:element>
        <xsl:if test="./chiba:data/@chiba:required='true'">
            <font color="red">*</font>
        </xsl:if>
    </xsl:template>

    <xsl:template name="secret">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:param name="maxlength"/>
        <xsl:param name="size"/>

        <xsl:element name="input">
            <xsl:attribute name="name">
                <xsl:value-of select="$name"/>
            </xsl:attribute>
            <xsl:attribute name="value">
                <xsl:value-of select="$value"/>
            </xsl:attribute>
            <xsl:attribute name="type">password</xsl:attribute>
            <xsl:attribute name="class">secret</xsl:attribute>
            <xsl:attribute name="title">
                <xsl:value-of select="./xforms:hint"/>
            </xsl:attribute>
            <xsl:if test="$size">
                <xsl:attribute name="size">
                    <xsl:value-of select="$size"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="$maxlength">
                <xsl:attribute name="maxlength">
                    <xsl:value-of select="$maxlength"/>
                </xsl:attribute>
            </xsl:if>

        </xsl:element>
        <xsl:if test="./chiba:data/@chiba:required='true'">
            <font color="red">*</font>
        </xsl:if>
    </xsl:template>

    <xsl:template name="html-output">
        <xsl:param name="value"/>
        <!--    <xsl:param name="name" /> -->

        <xsl:value-of select="$value"/>
    </xsl:template>

    <!-- the stylesheet using this template has to take care, that form enctype is set to 'multipart/form-data' -->
    <xsl:template name="upload">
        <xsl:param name="name"/>
        <xsl:element name="input">
            <xsl:attribute name="type">file</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template name="select1">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:param name="appearance"/>

        <xsl:variable name="parent" select="."/>
        <!-- todo: provide default -->
        <xsl:choose>
            <xsl:when test="$appearance='compact'">
                <xsl:element name="select">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">5</xsl:attribute>
                    <xsl:attribute name="class">select1</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="./xforms:hint"/>
                    </xsl:attribute>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:if test="./chiba:data/@chiba:required='true'">
                    <font color="red">*</font>
                </xsl:if>
            </xsl:when>
            <xsl:when test="$appearance='full'">
                <xsl:call-template name="build-radiobuttons">
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="value" select="$value"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="select">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">1</xsl:attribute>
                    <xsl:attribute name="class">select1</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="./xforms:hint"/>
                    </xsl:attribute>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:if test="./chiba:data/@chiba:required='true'">
                    <font color="red">*</font>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="select">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:param name="appearance"/>

        <xsl:variable name="parent" select="."/>
        <xsl:message>SELECT called: appearance:
            <xsl:value-of select="$appearance"/>
        </xsl:message>
        <xsl:choose>
            <xsl:when test="$appearance='compact'">
                <xsl:element name="select">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">5</xsl:attribute>
                    <xsl:attribute name="multiple">true</xsl:attribute>
                    <xsl:attribute name="class">select</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="./xforms:hint"/>
                    </xsl:attribute>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="value" select="$value"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:if test="./chiba:data/@chiba:required='true'">
                    <font color="red">*</font>
                </xsl:if>
            </xsl:when>
            <xsl:when test="$appearance='full'">
                <xsl:message>FULL</xsl:message>
                <xsl:call-template name="build-checkboxes">
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="value" select="$value"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
                <!-- 			<xsl:for-each select=".//xforms:item">
                                todo: selected or not?
                                <input type="checkbox" name="{$name}" value="{$value}" />
                            </xsl:for-each>
                            -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>MINIMAL</xsl:message>
                <xsl:element name="select">
                    <xsl:attribute name="name">
                        <xsl:value-of select="$name"/>
                    </xsl:attribute>
                    <xsl:attribute name="size">3</xsl:attribute>
                    <xsl:attribute name="multiple">true</xsl:attribute>
                    <xsl:attribute name="class">select</xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:value-of select="./xforms:hint"/>
                    </xsl:attribute>
                    <xsl:call-template name="build-items">
                        <xsl:with-param name="value" select="$value"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:element>
                <xsl:if test="./chiba:data/@chiba:required='true'">
                    <font color="red">*</font>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>


    </xsl:template>

    <xsl:template name="build-items">
        <xsl:param name="parent"/>

        <!-- add an empty item, cause otherwise deselection is not possible -->
        <option value=""/>

        <xsl:variable name="items" select="$parent//xforms:item"/>
        <xsl:for-each select="$items">
            <xsl:choose>
                <xsl:when test="@xforms:selected='true'">
                    <option value="{./xforms:value}" selected="selected">
                        <xsl:apply-templates select="./xforms:label"/>
                    </option>
                </xsl:when>
                <xsl:otherwise>
                    <option value="{./xforms:value}">
                        <xsl:apply-templates select="./xforms:label"/>
                    </option>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- overwrite/change this template, if you don't like the
    way labels are rendered for checkboxes -->
    <xsl:template name="build-checkboxes">
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <!-- this must be 'checkbox' or 'radio' -->
        <xsl:message>BUILD_CHECKBOXES</xsl:message>
        <xsl:variable name="items" select="$parent//xforms:item"/>
        <xsl:for-each select="$items">
            <xsl:choose>
                <xsl:when test="@xforms:selected='true'">
                    <input class="input1" type="checkbox" name="{$name}" value="{./xforms:value}" checked="checked" title="{./xforms:label}"/>
                </xsl:when>
                <xsl:otherwise>
                    <input class="input1" type="checkbox" name="{$name}" value="{./xforms:value}" title="{./xforms:label}"/>
                    <input type="hidden" name="{$name}" value=""/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="./xforms:label"/>
<!--            <br/>-->
        </xsl:for-each>
        <xsl:if test="./@chiba:required='true'">
            <font color="red">*</font>
        </xsl:if>
    </xsl:template>

    <!-- overwrite/change this template, if you don't the way labels are rendered for checkboxes -->
    <xsl:template name="build-radiobuttons">
        <xsl:param name="name"/>
        <xsl:param name="parent"/>

        <xsl:message>BUILD_RADIOBUTTONS</xsl:message>
        <xsl:variable name="items" select="$parent//xforms:item"/>
        <xsl:for-each select="$items">
            <xsl:choose>
                <xsl:when test="@xforms:selected='true'">
                    <input class="input1" type="radio" name="{$name}" value="{./xforms:value}" checked="checked" title="{./xforms:label}"/>
                </xsl:when>
                <xsl:otherwise>
                    <input class="input1" type="radio" name="{$name}" value="{./xforms:value}" title="{./xforms:label}"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="./xforms:label"/>
<!--            <br/>-->
        </xsl:for-each>
        <xsl:if test="./chiba:data/@chiba:required='true'">
            <font color="red">*</font>
        </xsl:if>
    </xsl:template>

    <xsl:template name="range">
    </xsl:template>

    <xsl:template name="trigger">
        <xsl:param name="name"/>
        <input type="submit" name="{$name}" value="{xforms:label}" title="{xforms:hint}"/>
    </xsl:template>

    <xsl:template name="image-trigger">
        <xsl:param name="name"/>
        <input type="image" name="{$name}" value="{xforms:label}" src="{xforms:label/@xlink:href}" title="{xforms:hint}"/>
    </xsl:template>

    <!-- support for custom chiba:index param. Can be used in local scripts to change the value the server receives
    for update -->
    <xsl:template name="setindex">
        <xsl:param name="repeat-node"/>
        <xsl:param name="index"/>

        <xsl:choose>
            <xsl:when test="$repeat-node/@chiba:index=$index">
                <input type="radio" name="chiba:head/cursor/{$repeat-node/@id}" value="{$index}" checked="true"/>
            </xsl:when>
            <xsl:otherwise>
                <input type="radio" name="chiba:head/cursor/{$repeat-node/@id}" value="{$index}"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="alert">
        <xsl:param name="message"/>
        <br/>
        <b>
            <font color="red" size="-1">
                <xsl:value-of select="$message"/>
            </font>
        </b>
    </xsl:template>

</xsl:stylesheet>