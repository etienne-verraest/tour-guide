package tourGuide.tracker;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@Slf4j
public class Tracker {

	private TourGuideService tourGuideService;

	private UserService userService;

	private boolean testMode = true;

	public Tracker(TourGuideService tourGuideService, UserService userService) {
		this.tourGuideService = tourGuideService;
		this.userService = userService;

		if (testMode) {
			userService.initializeInternalUsers();
		}
	}

	public void startTrackingUsers() {

		// Creating the StopWatch to analyze the performance results
		StopWatch stopWatch = new StopWatch();

		// Get the list of every users
		List<User> users = userService.getAllUsers();
		log.debug("[Tracker] Tracking {} users.", users.size());

		// Track every user in the list
		stopWatch.start();
		users.forEach(u -> tourGuideService.trackUserLocation(u));
		log.debug("[Tracker] Finished tracking users");
		stopWatch.stop();

		// Output performances
		log.debug("[Tracker] Total Execution Time : {} ms", stopWatch.getTime());

	}
}
