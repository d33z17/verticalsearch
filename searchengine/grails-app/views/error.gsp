<!DOCTYPE html>
<html>
	<head>
		<title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
		<meta name="layout" content="main">
		<g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css"></g:if>
	</head>
	<body>
		<g:if env="development">
			<g:renderException exception="${exception}" />
		</g:if>
		<g:else>
			<ul class="errors">
				<li>An error has occurred.</li>
				<li>If you are seeing this message, the most likely cause is that the Apache Solr server is down.</li>
				<li>Please text message or email Derek, the site administrator at mr.sheh@gmail.com and he will restart the Solr server.</li>
			</ul>
		</g:else>
	</body>
</html>
