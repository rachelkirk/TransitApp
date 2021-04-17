package com.tts.transitapp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tts.transitapp.model.Bus;
import com.tts.transitapp.model.BusComparator;
import com.tts.transitapp.model.BusRequest;
import com.tts.transitapp.model.DistanceResponse;
import com.tts.transitapp.model.GeocodingResponse;
import com.tts.transitapp.model.Location;

@Service
public class TransitService {
    @Value("${transit_url}")
    public String transitUrl;
    
    @Value("${geocoding_url}")
    public String geocodingUrl;
    
    @Value("${distance_url}")
    public String distanceUrl;
    
    @Value("${google_api_key}")
    public String googleApiKey;
    
    //rest template - create a website, make a restTemplate to visit a url (transit url here), return results as object specifies. it will see JSON, pull it down,
    //and map it to the object. it will deserialize it by translating it from JSON. maybe has to deserialize through several objects. look at MapsApp project
    //queries MARTA to get all buses
    private List<Bus> getBuses(){
        RestTemplate restTemplate = new RestTemplate();
        Bus[] buses = restTemplate.getForObject(transitUrl, Bus[].class);
        return Arrays.asList(buses);
    }
    //queries google geocoding api to get the lat and lng of a place in GA
    private Location getCoordinates(String description) {
        description = description.replace(" ", "+");
        String url = geocodingUrl + description + "+GA&key=" + googleApiKey;
        RestTemplate restTemplate = new RestTemplate();
        GeocodingResponse response = restTemplate.getForObject(url, GeocodingResponse.class);
        return response.results.get(0).geometry.location;
    }
    //queries google distance matrix api to get teh distance between two places
    private double getDistance(Location origin, Location destination) {
        String url = distanceUrl + "origins=" + origin.lat + "," + origin.lng 
        + "&destinations=" + destination.lat + "," + destination.lng + "&key=" + googleApiKey;
        
        RestTemplate restTemplate = new RestTemplate();
        DistanceResponse response = restTemplate.getForObject(url, DistanceResponse.class);
        //multiplying by constant converts meters to miles
        return response.rows.get(0).elements.get(0).distance.value * 0.000621371;
    }
    //get all nearby buses given the location in bus request
    public List<Bus> getNearbyBuses(BusRequest request, Location outputLocation){
        
    //  step 1 - get ALL THE BUSES from MARTA
        List<Bus> allBuses = this.getBuses();
        //step 2 - use the geocoding api to lookup the location (lat lng) of the request
        Location personLocation = this.getCoordinates(request.address + " " + request.city);
        outputLocation.lat = personLocation.lat;
        outputLocation.lng = personLocation.lng;
        //initialize nearbyBuses to empty ArrayList
        List<Bus>nearbyBuses = new ArrayList<>();
        //Step 3 - Loop through all the buses to find nearby buses only and add them to nearbyBuses
        for(Bus bus : allBuses) {
            Location busLocation = new Location();
            busLocation.lat = bus.LATITUDE;
            busLocation.lng = bus.LONGITUDE;
            
            //we are going to perform a FUZZY distance comparison between each buse and user
            //to pre filter out buses that are too far away
            //if we didn't do the fuzzy filter it would be way too many api calls, doing this first makes us 
            //only look at buses that are potentially matches
            double latDistance = Double.parseDouble(busLocation.lat) - Double.parseDouble(personLocation.lat);
            double lngDistance = Double.parseDouble(busLocation.lng) - Double.parseDouble(personLocation.lng);
            if (Math.abs(latDistance) <= 0.02 && Math.abs(lngDistance) <= 0.02) {
                double distance = getDistance(busLocation, personLocation);
                if (distance <= 1) {
                    bus.distance = (double) Math.round(distance * 100) /100;
                    nearbyBuses.add(bus);
                }
            }
            
        }
        //Step 4 - sort the buses
        Collections.sort(nearbyBuses, new BusComparator());
        return nearbyBuses;
        
        //three private methods we are using internally and one public method we are using internally
        //the whole point of this service is for the public method
    }
    
    
    
    
    
    
    
    
}