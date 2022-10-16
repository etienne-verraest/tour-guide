package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.time.StopWatch;

import gpsUtil.location.VisitedLocation;
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

	/**
	 * This method tracks user location.
	 *
	 * To improve performances, the method uses parallel streams.
	 * Parallel streams will split the list into N chunks where N is your number of cores.
	 *
	 * The time taken is outputted in milliseconds.
	 *
	 */
	public void startTrackingUsers() {

		// Creating the StopWatch to analyze the performance results
		StopWatch stopWatch = new StopWatch();

		// Get the list of every users
		List<User> users = userService.getAllUsers();
		log.debug("[Tracker] Number of cores available : {}", Runtime.getRuntime().availableProcessors());
		log.debug("[Tracker] Tracking {} users.", users.size());

		// Track every user in the list and analyze the performance
		stopWatch.start();
		users.parallelStream().forEach(u -> {
			try {
				VisitedLocation loc = tourGuideService.trackUserLocation(u).get();
				// log.debug("{}", loc.location.latitude);
			} catch (InterruptedException | ExecutionException e) {
				log.debug("[Tracker] There was an error while tracking the users");
				Thread.currentThread().interrupt();
			}
		});
		stopWatch.stop();

		// Output performances
		log.debug("[Tracker] Tracking Over. Total Execution Time : {} ms", stopWatch.getTime());

	}
}
