  var geocoder;
  var map;
	var nameArray = [];
	var markersArray = [];
	var latLngArray = [];
	
  function initialize() {
    geocoder = new google.maps.Geocoder();
    var latlng = new google.maps.LatLng(49.2505, -123.1119);
    var mapOptions = {
      zoom: 12,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
  }

  function codeAddress(address) {
		nameArray = (address.split(","));
		for (var i = 0; i < nameArray.length; i++) {
	    geocoder.geocode( { 'address': nameArray[i]}, function(results, status) {
	      if (status == google.maps.GeocoderStatus.OK) {
	        map.setCenter(results[0].geometry.location);
					addMarker(results[0].geometry.location);
	      }
	    });
		}
		var bounds = new google.maps.LatLngBounds();
		for (var i = 0; i < latLngArray.length; i++) {
			bounds.extend (latLngArray[i]);
		}
		map.fitBounds(bounds);
  }

	function addMarker(location) {
	  marker = new google.maps.Marker({
	    position: location,
	    map: map
	  });
		latLngArray.push(location);
	  markersArray.push(marker);
	}
	
	function deleteOverlays() {
	  if (markersArray) {
	    for (i in markersArray) {
	      markersArray[i].setMap(null);
	    }
	    markersArray.length = 0;
	  }
	}