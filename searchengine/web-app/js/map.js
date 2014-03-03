  var geocoder;
  var map;
	var nameArray = [];
	var markersArray = [];
	
  function initialize(address) {
		/* Defaults if prof doesn't have matching school data */
    geocoder = new google.maps.Geocoder();
    var latlng = new google.maps.LatLng(34.4604181,-3.7229434);
    var mapOptions = {
      zoom: 2,
      center: latlng,
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
		
		nameArray = address.split(/\d:/);
		nameArray.shift();

		var school = [];
		var latLngArray = [];
		
		for (var i = 0; i < nameArray.length; i++) {
			school = nameArray[i].split(',');

			for (var j = 0; j < school.length; j++) {
				
				/* use callback lat lng values to connect universities */
				getLatLng(geocoder, school[j], function plot(addr){
					latLngArray.push(addr);
					var linePlot = new google.maps.Polyline({
						path: latLngArray,
						strokeColor: "#FF0000",
						strokeOpacity: 1.0,
						strokeWeight: 5
					});
					linePlot.setMap(map);
				});
			}			
		}
		alert(school);	
	}

	/* wrapper function for geocoder so we can work with the callback lat lng values */	
	function getLatLng(geocoder, schoolName, callback) {
		geocoder.geocode( { 'address': schoolName}, function process(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
				addMarker(results[0].geometry.location);
				if (callback) {
					callback(results[0].geometry.location);
				}
      }	
  	});
	}

	function addMarker(location) {
	  marker = new google.maps.Marker({
	    position: location,
	    map: map
	  });
	  markersArray.push(marker);
	}