package tourGuide.domain.response;

import java.util.UUID;

import gpsUtil.location.Location;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLocationResponse {

	private UUID userId;

	private Location userLocation;
}
