<script type="text/javascript">

	$(document).ajaxSuccess(function() {
		initialize()
	});
	
	$(document).ajaxStop(function() {
		codeAddress("${sch}")
	});
	
</script>

<div id="map-canvas"></div>