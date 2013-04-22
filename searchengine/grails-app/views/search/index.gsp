<!DOCTYPE html>
<html>

<head>
	<meta name="layout" content="search"/>
</head>

<body>
	
	<%-- jQuery --%>
	<script type='text/javascript'>

	</script>
		
	<div id="querybox">
		
		<div class="header">
			<h1 id="bigTitle">Prestige</h1>
			<h3 id="subTitle">sfu professor ranklist.</h3>
		</div>
				
		<%-- ajax form to send query to searchcontroller.myquery --%>
		<g:formRemote name="myForm" update="results" url="[controller:'Search', action:'mainQuery']">		

			<%-- search field --%>
			<g:textField name="address" placeholder="start here" />

			<%-- submit query to map --%>
			<g:actionSubmit value="Tarot" />
			
		</g:formRemote>
					
	</div>

	<%-- populate div with query results --%>
	<div id="results">
		<p>The Prestige Vertical Search Engine works with faculty data from <a href='http://www.cs.sfu.ca/people.html' target='_blank'>http://www.cs.sfu.ca</a>.</p>
		<p>Some Examples:<br />
			-- sfu lecturer<br />
			-- stella pearce<br />
			-- macm 101<br />
			-- india<br />
		</p>
		<p>It accommodates stopword and synonym lists to provide the most flexible user search experience.</p>
		<p>The engine will search across the following professor attributes: name, classification, courses taught, degrees held, schools attended, years attended, and country.</p>
		<p>Pairwise similarity matching is performed on the results to generate relationships on courses taught and schools attended.</p>
		<p></p>
	</div>
	
</body>

</html>