package tourGuide.domain.response;

import gpsUtil.location.Location;
import lombok.Data;

@Data
public class AttractionInformation {

	private String nameOfAttraction;

	private Location attractionLocation;

	private Double distanceToAttraction;

	private Integer attractionRewardPoints;

}
