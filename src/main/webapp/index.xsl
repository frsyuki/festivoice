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
				<p><strong>festivoice</strong>（フェスティボイス）を使うと手軽に多人数の音声チャットを始められます。</p>
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
				</ul>
			</div>
			<ul id="links">
				<li><a href="http://www.partty.org/">Partty!.org</a></li>
			</ul>
		</div>
		<div id="content" class="clearfix">
			<h2>チャンネル一覧</h2>
			<xsl:for-each select="channels/channel">
			<div class="channel">
				<h3><a>
					<xsl:attribute name="href">/start/<xsl:value-of select="name"/></xsl:attribute>
					<xsl:value-of select="name"/>
				</a></h3>
				<xsl:apply-templates select="users"/>
			</div>
			</xsl:for-each>
			<form method="get" action="#" name="startf" id="startf">
				<input type="text" name="starti" id="starti" value="ここにチャンネル名を入力（英数字）" onblur="if(document.startf.starti.value=='') document.startf.starti.value='ここにチャンネル名を入力（英数字）'" onfocus="if(document.startf.starti.value=='ここにチャンネル名を入力（英数字）' || document.startf.starti.value=='ダウンロードしたファイルを実行してください') document.startf.starti.value='';"/>
				<input type="submit" value="新しく始める" id="startb" onclick="var name=document.startf.starti.value; if(name=='' || name=='ここにチャンネル名を入力（英数字）' || name=='ダウンロードしたファイルを実行してください') name='-'; document.startf.starti.value='ダウンロードしたファイルを実行してください'; location.href='/start/'+name; return false;" />
			</form>
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
	</body>
</html>
</xsl:template>
<xsl:template match="users">
<ul>
	<xsl:for-each select="user">
	<li><xsl:value-of select="name"/></li>
	</xsl:for-each>
</ul>
</xsl:template>
</xsl:stylesheet>
