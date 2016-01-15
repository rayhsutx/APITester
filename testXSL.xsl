<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="1.0">
<xsl:template match="/">
<html>
<head><title>Test Case Results</title></head>
<body>
<hr></hr>
<h2> Basic Information </h2>
<h3>Command Used: <xsl:value-of select="testresults/commandUsed"/> </h3>
<h3>Commands : <br></br><xsl:for-each select="testresults/commands/command">
	<xsl:value-of select="name"/> <br></br>
	</xsl:for-each>
</h3>
<hr>
</hr>
<table border="1px">
<tr>
<tr><td>Test Time Started: </td><td><xsl:value-of select="testresults/timeStart"/></td></tr>
<tr><td>Test Time Ended: </td><td><xsl:value-of select="testresults/timeEnd"/></td></tr>
<tr><td>Test Time Taken : </td><td><xsl:value-of select="testresults/timeDifference"/></td></tr>
</tr>
</table><br></br>
<table border="1px">
<tr>
	<th>Passed</th>
	<th>Failed</th>
	<th>Untested</th>
</tr>

<tr>
	<td><xsl:value-of select="testresults/results/Passed/count"/></td>
	<td><xsl:value-of select="testresults/results/Failed/count"/></td>
	<td><xsl:value-of select="testresults/results/Untested/count"/></td>
</tr>
</table>

<h3>Passed : <xsl:value-of select="testresults/results/Passed/data"/> </h3>
<h3>Failed : <xsl:value-of select="testresults/results/Failed/data"/> </h3>
<h3>Untested : <xsl:value-of select="testresults/results/Untested/data"/> </h3>

<hr></hr> 
<xsl:for-each select="testresults/test">
<h2>test <xsl:value-of select="name"/></h2>
<table border="1px solid black">
	
	<tr bgcolor="#9acd32">
		<th>File Name</th>
		<th>Test Status</th>
		<th>Actions</th>
	</tr>
	<xsl:for-each select="file">
		<tr>
		<td><xsl:value-of select="@name"/></td>
		<xsl:choose>
     		<xsl:when test="TestStatus = 'pass'">
				<td bgcolor="#9acd32">
					<xsl:value-of select="TestStatus"/>
				</td>
			</xsl:when>
			<xsl:when test="TestStatus = 'fail'">
				<td bgcolor="#ff0000">
					<xsl:value-of select="TestStatus"/>
				</td>
			</xsl:when>
			<xsl:otherwise> 
				<td>
					<xsl:value-of select="TestStatus"/>
				</td>
			</xsl:otherwise>
		</xsl:choose>
		<td>
		<table border="1">
			<xsl:for-each select="action">
				<tr>
					<th>
						<td> <xsl:value-of select="name"/></td>
						<xsl:choose>
							<xsl:when test="ActionStatus = 'pass'">
								<td bgcolor="#9acd32">
									<xsl:value-of select="ActionStatus"/>
								</td>
							</xsl:when>
							<xsl:when test="ActionStatus = 'fail'">
								<td bgcolor="#ff0000">
									<xsl:value-of select="ActionStatus"/>
								</td>
							</xsl:when>
							<xsl:otherwise>
								<td>
									<xsl:value-of select="ActionStatus"/>
								</td>
							</xsl:otherwise>
							
						
						</xsl:choose>
						
					</th>	
				</tr>
			</xsl:for-each>
		</table>
		</td>
		</tr>
	</xsl:for-each>
	
</table>
<br></br>
</xsl:for-each>
</body></html>
</xsl:template>
</xsl:stylesheet>