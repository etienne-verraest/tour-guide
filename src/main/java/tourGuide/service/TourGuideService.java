package tourGuide.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;
import tourGuide.domain.response.AttractionInformation;
import tourGuide.domain.response.NearbyAttractionsResponse;
import tourGuide.domain.response.UserLocationResponse;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {

	private static String tripPricerApiKey = "test-server-api-key";

	private RewardsService rewardsService = new RewardsService();

	private UserService userService = new UserService();

	private GpsUtil gpsUtil = new GpsUtil();

	private TripPricer tripPricer = new TripPricer();

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public NearbyAttractionsResponse getNearByAttractions(User user) {
		// Getting attractions list and user location
		List<Attraction> attractions = gpsUtil.getAttractions();
		Location currentUserLocation = getUserLocation(user).location;

		// Sorting the 5 nearest attractions for each user depending on the distance in
		// miles
		attractions = attractions.stream()
				.sorted(Comparator.comparingDouble(
						a -> rewardsService.getDistance(new Location(a.longitude, a.latitude), currentUserLocation)))
				.limit(5).collect(Collectors.toList());

		// Fetching information for the 5 nearest attractions
		List<AttractionInformation> nearbyAttractions = new ArrayList<>();
		for (Attraction a : attractions) {
			AttractionInformation attractionInformation = new AttractionInformation();
			Location attractionLocation = new Location(a.longitude, a.latitude);
			attractionInformation.setNameOfAttraction(a.attractionName);
			attractionInformation.setAttractionLocation(attractionLocation);
			attractionInformation
					.setDistanceToAttraction(rewardsService.getDistance(attractionLocation, currentUserLocation));
			attractionInformation.setAttractionRewardPoints(rewardsService.getRewardPoints(a, user));
			nearbyAttractions.add(attractionInformation);
		}

		// Setting the response data object that will be used to read collected datas
		NearbyAttractionsResponse response = new NearbyAttractionsResponse();
		response.setUserLocation(currentUserLocation);
		response.setNearbyAttractions(nearbyAttractions);

		return response;
	}

	// gathers the user's current location from their stored location
	// history.
	public List<UserLocationResponse> getAllUsersLocation() {

		List<User> users = userService.getAllUsers();
		List<UserLocationResponse> response = new ArrayList<>();
		for (User u : users) {
			UserLocationResponse userLocationResponse = new UserLocationResponse();
			userLocationResponse.setUserId(u.getUserId());
			userLocationResponse.setUserLocation(getUserLocation(u).location);
			response.add(userLocationResponse);
		}

		return response;
	}
}
