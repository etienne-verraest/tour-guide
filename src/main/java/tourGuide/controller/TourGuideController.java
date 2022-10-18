package tourGuide.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.domain.UserPreferences;
import tourGuide.domain.dto.UserPreferencesDto;
import tourGuide.domain.response.NearbyAttractionsResponse;
import tourGuide.domain.response.UserLocationResponse;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	UserService userService;

	@Autowired
	RewardsService rewardsService;

	/**
	 * Return a string when calling the default url (in order to test if the API is UP or DOWB)
	 *
	 * @return								String is returned if the connection is succesful
	 */
	@GetMapping("/")
	public ResponseEntity<String> defaultUrl() {
		return new ResponseEntity<>("Greetings from TourGuide!", HttpStatus.OK);
	}

	/**
	 * Get location of a given user
	 *
	 * @param userName						String : the name of the user to fetch location
	 * @return								Location with longitude and latitude
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@GetMapping("/getLocation")
	public ResponseEntity<VisitedLocation> getLocation(@RequestParam String userName)
			throws InterruptedException, ExecutionException {
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return new ResponseEntity<>(visitedLocation, HttpStatus.OK);
	}

	/**
	 * Get 5 closest attractions for a given user
	 *
	 * @param userName						String : the name of the user to fetch attractions
	 * @return								5 closest attractions with related datas
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@GetMapping("/getNearbyAttractions")
	public ResponseEntity<NearbyAttractionsResponse> getNearbyAttractions(@RequestParam String userName)
			throws InterruptedException, ExecutionException {
		return new ResponseEntity<>(tourGuideService.getNearByAttractions(getUser(userName)), HttpStatus.OK);
	}

	/**
	 * Get location of every user registered on the application
	 *
	 * @return								A List with every user id and their location
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@GetMapping("/getAllCurrentLocations")
	public ResponseEntity<List<UserLocationResponse>> getAllCurrentLocations()
			throws InterruptedException, ExecutionException {
		return new ResponseEntity<>(tourGuideService.getAllUsersLocation(), HttpStatus.OK);
	}

	@GetMapping("/getRewards")
	public String getRewards(@RequestParam String userName) {
		return JsonStream.serialize(rewardsService.getUserRewards(getUser(userName)));
	}

	/**
	 * Set the user preferences for a given user
	 * The following post request must contain this body :
	 * {
		   "attractionProximity": 1,
		   "currency": "USD",
		   "lowerPricePoint": 0,
		   "highPricePoint": 10000,
		   "tripDuration": 7,
		   "numberOfAdults": 2,
		   "numberOfChildren": 1
		}
	 *
	 * @param userName						String : the username to set preferences
	 * @param userPreferencesDto			UserPreferencesDto containg the fields to set user preferences
	 * @return								HttpStatus 200 and the User Preferences if everything is filled correctly
	 */
	@PostMapping("/setUserPreferences")
	public ResponseEntity<UserPreferences> setUserPreference(@RequestParam String userName,
			@RequestBody UserPreferencesDto userPreferencesDto) {

		User user = getUser(userName);
		if (user != null) {

			UserPreferences userPreferences = new UserPreferences();

			// Attraction proximity in miles
			userPreferences.setAttractionProximity(userPreferencesDto.getAttractionProximity());

			// Money fields
			userPreferences.setCurrency(Monetary.getCurrency(userPreferencesDto.getCurrency()));

			userPreferences.setHighPricePoint(Money.of(userPreferencesDto.getHighPricePoint(),
					Monetary.getCurrency(userPreferencesDto.getCurrency())));

			userPreferences.setLowerPricePoint(Money.of(userPreferencesDto.getLowerPricePoint(),
					Monetary.getCurrency(userPreferencesDto.getCurrency())));

			// Number of adults and children
			userPreferences.setNumberOfAdults(userPreferencesDto.getNumberOfAdults());
			userPreferences.setNumberOfChildren(userPreferencesDto.getNumberOfChildren());

			// Ticket Quantity will be calculated based on the number of adults and children
			userPreferences.setTicketQuantity(
					userPreferencesDto.getNumberOfAdults() + userPreferencesDto.getNumberOfChildren());

			// Trip Duration
			userPreferences.setTripDuration(userPreferencesDto.getTripDuration());

			user.setUserPreferences(userPreferences);
			return new ResponseEntity<>(userPreferences, HttpStatus.OK);
		}

		// If username was not found we throw a NullPointerException
		throw new NullPointerException("Username was not found");
	}

	/**
	 * Get Trip Agencies based on the user preferences
	 *
	 * @param userName						String : The username we want to fetch
	 * @return								List<Provider> containg travel agencies based on the user preferences
	 */
	@GetMapping("/getTripDeals")
	public ResponseEntity<List<Provider>> getTripDeals(@RequestParam String userName) {

		User user = getUser(userName);
		if (user != null) {
			List<Provider> providers = tourGuideService.getTripDeals(user);
			return new ResponseEntity<>(providers, HttpStatus.OK);
		}

		throw new NullPointerException("Username was not found");
	}

	// Utility method to get user
	private User getUser(String userName) {
		return userService.getUser(userName);
	}

}