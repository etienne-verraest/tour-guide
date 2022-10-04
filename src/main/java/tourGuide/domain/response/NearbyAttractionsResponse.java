package tourGuide.domain.response;

import java.util.List;

import gpsUtil.location.Location;
import lombok.Data;

@Data
public class NearbyAttractionsResponse {

	private Location userLocation;

	private List<AttractionInformation> nearbyAttractions;

}