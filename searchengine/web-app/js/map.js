  var geocoder;
  var map;
	var nameArray = [];
	var markersArray = [];
	
  function initialize(address) {
		/* Defaults if prof doesn't have matching school data */
    geocoder = new google.maps.Geocoder();
    var latlng = new google.maps.LatLng(49.2505, -123.1119);
    var mapOptions = {
      zoom: 12,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);

		nameArray = (address.split(","));
		alert(nameArray);

		var latLngArray = [];
		
		/* use callback lat lng values to connect universities */
//		getLatLng(geocoder, nameArray, function plot(addr){
//			latLngArray.push(addr);
//			alert(latLngArray);
/*			var linePlot = new google.maps.Polyline({
				path: latLngArray,
				strokeColor: "#FF0000",
				strokeOpacity: 1.0,
				strokeWeight: 5
			});
			linePlot.setMap(map);
		});
*/		
	}

	/* wrapper function for geocoder so we can work with the callback lat lng values */	
	function getLatLng(geocoder, nameArray, callback) {
		for (var i = 0; i < nameArray.length; i++) {		
			geocoder.geocode( { 'address': nameArray[i]}, function process(results, status) {
	      if (status == google.maps.GeocoderStatus.OK) {
					addMarker(results[0].geometry.location);
					if (callback) {
						callback(results[0].geometry.location);
					}
	      }	
	  	});
		}
	}

	function addMarker(location) {
	  marker = new google.maps.Marker({
	    position: location,
	    map: map
	  });
	  markersArray.push(marker);
	}