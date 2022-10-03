package tourGuide.domain.response;

import lombok.Data;

@Data
public class ClosestAttractionsResponse {

	private String nameOfAttraction;

	private Double attractionLatitude;

	private Double attractionLongitude;

	private Double userLatitude;

	private Double userLongitude;

	private Double distanceToAttraction;

	private Integer attractionRewardPoints;

}
