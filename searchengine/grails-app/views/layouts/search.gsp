<!DOCTYPE html>
<html>

  <head>
    <link rel="shortcut icon" href="images/favicon.ico" >
    <title><g:layoutTitle default="Prestige : Engine" /></title>
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'search.css')}" type="text/css">
		<g:javascript library="jquery" />
		<r:layoutResources />
  </head>

	<body>

		<%-- wrapper div for gradient effect --%>
		<div class="wrapper">

			<%-- main div for page content --%>
			<div class="content">

    		<tmpl:/nav />

				<%--<tmpl:/header />--%>
				
				<%-- dynamic page content --%>				
    		<g:layoutBody />

				<tmpl:/footer />				

			</div>

		</div>

  </body>

</html>