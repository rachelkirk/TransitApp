let map;
function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        center: { lat: parseFloat(personLocation.lat), lng: parseFloat(personLocation.lng) },
        zoom: 15,
        scrollwheel: false
    });
	
	let image = {
        url:'/img/bus-clipart.png', 
        scaledSize: new google.maps.Size(50,50)};
        
    let image2 = {
        url: '/img/person.png',
        scaledSize: new google.maps.Size(50,50)};
			
    for (i=0; i<busLocations.length; i++){
        let marker = new google.maps.Marker({
            position: { lat: parseFloat(busLocations[i].LATITUDE), lng: parseFloat(busLocations[i].LONGITUDE) },
            map: map,
            icon: image
        });
        
    let personMarker = new google.maps.Marker({
    	position: {lat: parseFloat(personLocation.lat), lng: parseFloat(personLocation.lng) },
    	map: map,
    	icon: image2,
    	});
    	
    var contentString = '<h2>' + VEHICLE + '</h2>';

 		var infowindow = new google.maps.InfoWindow({
   		content: contentString
 		});

 		google.maps.event.addListener(marker, 'click', function() {
    	infowindow.open(map,marker);
 			});
    }
}

