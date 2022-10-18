package tourGuide.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;
import tourGuide.domain.response.AttractionInformation;
import tourGuide.domain.response.NearbyAttractionsResponse;
import tourGuide.domain.response.UserLocationResponse;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Slf4j
@Service
public class TourGuideService {

	@Autowired
	private RewardsService rewardsService;

	@Autowired
	private UserService userService;

	private GpsUtil gpsUtil = new GpsUtil();

	private TripPricer tripPricer = new TripPricer();

	@Value("${tripPricer.api.key}")
	public String tripPricerApiKey;

	// Number of threads to handle the tracker location task
	private ExecutorService executorService = Executors.newFixedThreadPool(100);

	/**
	 * Get the location for a given user
	 * It checks the user's location history beforehand and if there are no results, it call the tracker
	 *
	 * @param user									User : The user we want to get the location
	 * @return										VisitedLocation : An object containg the User ID, the location and the time visited
	 * @throws InterruptedException					Thrown if there was en error while fetching user location
	 * @throws ExecutionException					Thrown if there was en error while fetching user location
	 */
	public VisitedLocation getUserLocation(User user) throws InterruptedException, ExecutionException {
		VisitedLocation visitedLocation = (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation()
				: trackUserLocation(user).get();
		return visitedLocation;
	}

	/**
	 * Track User Location uses the CompletableFuture API to make asynchronous computation
	 * Calls return immediately and the response will be sent when available using the get() method
	 *
	 * @param user									User : The user we want to track
	 * @return										VisitedLocation : The current location of the given user
	 * @throws InterruptedException					Handled by exceptionally() if there is an error while tracking the user
	 * @throws ExecutionException                   Handled by exceptionally() if there is an error while tracking the user
	 */
	public CompletableFuture<VisitedLocation> trackUserLocation(User user)
			throws InterruptedException, ExecutionException {

		CompletableFuture<VisitedLocation> userLocationFuture = CompletableFuture
				.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorService)
				.thenApply(visitedLocation -> {
					user.addToVisitedLocations(visitedLocation);
					rewardsService.calculateRewards(user);
					return visitedLocation;
				}).exceptionally(e -> {
					log.debug("Error while tracking user : {}", e.getMessage());
					return null;
				});

		return userLocationFuture;
	}

	/**
	 * Get the five closest attractions for a given user
	 *
	 * @param user									The User we want to fetch
	 * @return										The five closest attraction from the current user location
	 * @throws InterruptedException					Thrown if there was en error while fetching user location
	 * @throws ExecutionException					Thrown if there was en error while fetching user location
	 */
	public NearbyAttractionsResponse getNearByAttractions(User user) throws InterruptedException, ExecutionException {
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

	/**
	 * This method gathers the users' current location based on their stored location history
	 *
	 * @return										UserLocationResponse containing the user id and the location
	 * @throws InterruptedException					Thrown if there was en error while fetching user location
	 * @throws ExecutionException					Thrown if there was en error while fetching user location
	 */
	public List<UserLocationResponse> getAllUsersLocation() throws InterruptedException, ExecutionException {

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

	/**
	 * Get Trip Deals based for a given user based on its preferences
	 *
	 * @param user									The User we want to fetch trip deals
	 * @return										List<Provider> containing the travel agencies offers
	 */
	public List<Provider> getTripDeals(User user) {

		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();

		// Get a list of provider based on user preferences (number of adults, children
		// and trip duration)
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);

		// Filtering providers that are in the price range of the user
		double userLowerPoint = user.getUserPreferences().getLowerPricePoint().getNumber().doubleValue();
		double userHigherPoint = user.getUserPreferences().getHighPricePoint().getNumber().doubleValue();
		providers = providers.stream()
				.filter(provider -> provider.price >= userLowerPoint && provider.price <= userHigherPoint)
				.collect(Collectors.toList());

		// Returning providers
		user.setTripDeals(providers);
		return providers;
	}
}
