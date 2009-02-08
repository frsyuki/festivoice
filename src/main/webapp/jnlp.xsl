<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="xml"/>
<xsl:template match="/">
<jnlp spec="1.0+" codebase="http://festivoice.net/start/">
	<xsl:attribute name="href"><xsl:value-of select="start/channel"/></xsl:attribute>

	<information>
		<title>festivoice.net</title>
		<vendor>festivoice developer team</vendor>
	</information>

	<security>
		<all-permissions />
	</security>

	<resources>
		<j2se version="1.5+"/>
		<jar href="../pkg/festivoice.jar"/>
	</resources>

	<application-desc main-class="festivoice.net">
		<argument>-G</argument>
		<argument>stream.festivoice.net</argument>
		<argument>11100</argument>
		<argument><xsl:value-of select="start/channel"/></argument>
		<argument>-<!-- user name --></argument>
	</application-desc>

	<update check="always" policy="always" />
</jnlp>
</xsl:template>
</xsl:stylesheet>
