package tourGuide.domain.response;

import java.util.List;

import gpsUtil.location.Location;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NearbyAttractionsResponse {

	private Location userLocation;

	private List<AttractionInformation> nearbyAttractions;

}