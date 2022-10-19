package tourGuide.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import rewardCentral.RewardCentral;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;

@Slf4j
@Service
public class RewardsService {

	private GpsUtil gpsUtil = new GpsUtil();

	private RewardCentral rewardsCentral = new RewardCentral();

	private int defaultProximityBuffer = 10;

	@Setter
	private int proximityBuffer = defaultProximityBuffer;

	private int attractionProximityRange = 200;

	// Number of threads to run the rewards calculation
	private ExecutorService executorService = Executors.newFixedThreadPool(100);

	/**
	 * Calculate rewards for a given user, only if the user is near an unvisited attraction
	 * The method doesn't return anything, but add the rewards to the user
	 * It uses CompletableFuture runAsync() method to make non-blocking calls when executing the tracker
	 *
	 * @param user									User : The user we want to calculate the rewards of
	 *
	 */
	public void calculateRewards(User user) {

		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		CompletableFuture.runAsync(() -> {

			for (VisitedLocation visitedLocation : userLocations) {
				for (Attraction attraction : attractions) {

					// Using parallel streams to count faster on the filter method
					if (user.getUserRewards().stream()
							.filter(r -> r.getAttraction().attractionName.equals(attraction.attractionName))
							.count() == 0) {
						if (nearAttraction(visitedLocation, attraction)) {
							log.debug("[Rewards] Rewards calculcated for user {}", user.getUserName());
							user.addUserReward(
									new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			}
		}, executorService);
	}

	/**
	 * Get rewards list for a given user
	 *
	 * @param user									User : The User we want to fetch rewards
	 * @return										List<UserReward> : A list of rewards for the given user
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	/**
	 * Check if a location has proximity with a given attraction
	 *
	 * @param attraction							Attraction : The attraction we want to check
	 * @param location								Location : The location we want to calculate the proximity of
	 * @return										True if the distance calculated is in the attractionProximityRange
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	/**
	 * Check if a user's location is near a given attraction
	 *
	 * @param visitedLocation						VisitedLocation : The current location of a user
	 * @param attraction							Attraction : The attraction we want to check
	 * @return										True if the distance calculate is in the proximityBuffer
	 */
	public boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	/**
	 * Calculate the number of rewards points for a given attraction and a given user.
	 * Points may vary between users because of their respective locations
	 *
	 * @param attraction							Attraction : The given attraction
	 * @param user									User : the given user
	 * @return										Integer : the total number of points awarded
	 */
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Calculates the distance in miles between two locations
	 *
	 * @param loc1									Location : The first location
	 * @param loc2									Location : The second location
	 * @return										Double : The distance in miles between the two locations
	 */
	public double getDistance(Location loc1, Location loc2) {

		final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);

		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}
