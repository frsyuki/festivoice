<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output encoding="UTF-8" method="xml"/>
<xsl:template match="/">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta content="application/xhtml+xml; charset=UTF-8" http-equiv="content-type" />
		<meta content="text/css" http-equiv="content-style-type" />
		<link rel="stylesheet" type="text/css" href="/style.css" />
		<title>festivoice.net</title>
	</head>
	<body>
		<div id="header">
			<h1><a href="/"><span id="title">festivoice</span></a></h1>
			<div id="about">
				<h2>音声チャットをはじめよう!</h2>
				<p><strong>festivoice</strong>（フェスティボイス）を使うと気軽に多人数の音声チャットを始められます。</p>
				<ul>
					<li>ミーティングに</li>
					<li>ペアプログラミングに</li>
					<li>おしゃべりに</li>
					<li>IRCのお供に</li>
				</ul>
				<p>ユーザー登録は不要です。<a href="/pkg/festivoice.net.jnlp">festivoiceソフトウェアをダウンロード</a>して今すぐ始めよう！<a href="/pkg/festivoice.net.tar.gz">コマンドライン版</a>もあります。</p>
				<p>festivoiceソフトウェアの動作には<a href="http://www.java.com/">Javaソフトウェア</a>が必要です。</p>
				<ul>
					<li><a href="/client.html">festivoice.netの使い方</a></li>
					<li><a href="/server.html">festivoiceサーバーの導入</a></li>
					<li><a href="http://github.com/frsyuki/festivoice/tree/master">ソースコード</a></li>
				</ul>
				<p><span class="new">New!</span> アンダーバー（_）から始まるチャンネル名はトップページに表示されなくなりました。秘密のチャットにご利用ください。</p>
				<p><span class="new">New!</span> 起動時に自動的に接続しないようになりました。</p>
				<p><span class="new">New!</span> listen onlyモードを追加しました。</p>
				<p><span class="new">New!</span> マイクがない環境にも対応しました。</p>
				<p><span class="new">New!</span> トップページにログが表示されるようになりました。</p>
			</div>
			<ul id="links">
				<li><a href="http://partty.org/">Partty!.org</a></li>
			</ul>
		</div>
		<div id="content" class="clearfix">
			<h2 id="channels_h">チャンネル一覧</h2>
			<xsl:apply-templates select="status/channels"/>
			<form method="get" action="#" name="startf" id="startf">
				<input type="text" name="starti" id="starti" value="ここにチャンネル名を入力（英数字）" onblur="if(document.startf.starti.value=='') document.startf.starti.value='ここにチャンネル名を入力（英数字）'" onfocus="if(document.startf.starti.value=='ここにチャンネル名を入力（英数字）' || document.startf.starti.value=='ダウンロードしたファイルを実行してください') document.startf.starti.value='';"/>
				<input type="submit" value="新しく始める" id="startb" onclick="var name=document.startf.starti.value; if(name=='' || name=='ここにチャンネル名を入力（英数字）' || name=='ダウンロードしたファイルを実行してください') name='-'; document.startf.starti.value='ダウンロードしたファイルを実行してください'; location.href='/start/'+name; return false;" />
			</form>
			<xsl:apply-templates select="status/events"/>
		</div>
		<!--
		<script src="http://java.com/js/deployJava.js"></script>
		<div>
			<script>
				var attributes = { codebase: 'http://festivoice.net/', code: 'net.festivoice.AppletLauncher', archive: 'pkg/festivoice.jar', width: 300, height: 200 };
				var parameters = { host: 'stream.festivoice.net', port: 11100, user: '-', channel: '-' };
				deployJava.runApplet(attributes, parameters, '1.5');
			</script>
		</div>
		-->
		<div id="footer">
			<p><span id="copyright">Copyright (C) 2009 festivoice developer team. All Rights Reserved.</span><a id="staff" href="staff.html">Staff</a></p>
		</div>
		<script type="text/javascript">
			var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
			document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
		</script>
		<script type="text/javascript">
			try {
				var pageTracker = _gat._getTracker("UA-3857272-2");
				pageTracker._trackPageview();
			} catch(err) {}</script>
	</body>
</html>
</xsl:template>

<xsl:template match="channels">
	<xsl:for-each select="channel">
	<xsl:if test="hidden = '0'">
		<div class="channel">
			<h3><a>
				<xsl:attribute name="href">/start/<xsl:value-of select="cname"/></xsl:attribute>
				<xsl:value-of select="cname"/>
			</a></h3>
			<xsl:for-each select="users">
			<ul>
				<xsl:for-each select="user">
				<li><xsl:value-of select="name"/></li>
				</xsl:for-each>
			</ul>
			</xsl:for-each>
		</div>
	</xsl:if>
	</xsl:for-each>
</xsl:template>

<xsl:template match="events">
	<ul id="events">
	<xsl:for-each select="event">
	<xsl:if test="type = '1'">
		<li>
			<span class="time"><xsl:value-of select="time"/></span>
			<xsl:if test="new = '1'"><span class="new">New!</span></xsl:if>
			<xsl:if test="hidden = '0'">
				<a class="channel">
					<xsl:attribute name="href">/start/<xsl:value-of select="channel"/></xsl:attribute>
					<xsl:value-of select="channel"/></a>を作成しました。
			</xsl:if>
			<xsl:if test="hidden = '1'">
				<span class="hidden"><span class="channel">秘密の部屋</span>を作成しました。</span>
			</xsl:if>
		</li>
	</xsl:if>
	<xsl:if test="type = '2'">
		<xsl:if test="hidden = '0'">
			<li>
				<span class="time"><xsl:value-of select="time"/></span>
				<xsl:if test="new = '1'"><span class="new">New!</span></xsl:if>
				<a class="channel">
					<xsl:attribute name="href">/start/<xsl:value-of select="channel"/></xsl:attribute>
					<xsl:value-of select="channel"/></a>に<span class="user"><xsl:value-of select="user"/></span>が入室しました。
			</li>
		</xsl:if>
	</xsl:if>
	<xsl:if test="type = '3'">
		<xsl:if test="hidden = '0'">
			<li>
				<span class="time"><xsl:value-of select="time"/></span>
				<xsl:if test="new = '1'"><span class="new">New!</span></xsl:if>
				<span class="channel">
					<xsl:value-of select="channel"/></span>から<span class="user"><xsl:value-of select="user"/></span>が退室しました。
			</li>
		</xsl:if>
	</xsl:if>
	<xsl:if test="type = '4'">
		<li>
			<span class="time"><xsl:value-of select="time"/></span>
			<xsl:if test="new = '1'"><span class="new">New!</span></xsl:if>
			<xsl:if test="hidden = '0'">
				<span class="channel">
					<xsl:value-of select="channel"/></span>が無くなりました。
			</xsl:if>
			<xsl:if test="hidden = '1'">
				<span class="hidden"><span class="channel">秘密の部屋</span>が無くなりました。</span>
			</xsl:if>
		</li>
	</xsl:if>
	</xsl:for-each>
	</ul>
</xsl:template>

</xsl:stylesheet>
