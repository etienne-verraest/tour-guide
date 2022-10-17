package tourGuide.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
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
	 * @return							String is returned if the connection is succesful
	 */
	@GetMapping("/")
	public ResponseEntity<String> defaultUrl() {
		return new ResponseEntity<>("Greetings from TourGuide!", HttpStatus.OK);
	}

	/**
	 * Get location of a given user
	 *
	 * @param userName					String : the name of the user to fetch location
	 * @return							Location with longitude and latitude
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
	 * @param userName					String : the name of the user to fetch attractions
	 * @return							5 closest attractions with related datas
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
	 * @return							A List with every user id and their location
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

	@GetMapping("/getTripDeals")
	public String getTripDeals(@RequestParam String userName) {
		List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
		return JsonStream.serialize(providers);
	}

	private User getUser(String userName) {
		return userService.getUser(userName);
	}

}