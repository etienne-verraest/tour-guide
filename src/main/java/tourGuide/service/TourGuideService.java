package tourGuide.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;
import tourGuide.domain.response.AttractionInformation;
import tourGuide.domain.response.NearbyAttractionsResponse;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {

	@Value("${tripPricer.api.key}")
	private static String tripPricerApiKey;

	@Autowired
	private RewardsService rewardsService;

	private GpsUtil gpsUtil = new GpsUtil();

	private TripPricer tripPricer;

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation() : trackUserLocation(user);
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
}
