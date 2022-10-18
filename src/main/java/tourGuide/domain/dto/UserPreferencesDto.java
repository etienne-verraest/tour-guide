package tourGuide.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPreferencesDto {

	private int attractionProximity;
	private String currency;
	private int lowerPricePoint;
	private int highPricePoint;
	private int tripDuration;
	private int numberOfAdults;
	private int numberOfChildren;

}
