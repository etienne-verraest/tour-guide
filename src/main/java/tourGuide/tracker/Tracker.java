package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@Slf4j
@Component
public class Tracker {

	@Value("${testMode.enabled}")
	public boolean testMode;

	@Autowired
	private TourGuideService tourGuideService;

	@Autowired
	private UserService userService;

	public Tracker(TourGuideService tourGuideService, UserService userService) {
		this.tourGuideService = tourGuideService;
		this.userService = userService;
	}

	/**
	 * When tracker has been created, we check if the testMode is enabled
	 */
	@PostConstruct
	public void initializeTracker() {
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
		log.debug("[Tracker] Number of cores available : {}",
				System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism"));
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

		// Output performances and resetting the stop watch for further executions
		log.debug("[Tracker] Tracking Over. Total Execution Time : {} ms", stopWatch.getTime());
		stopWatch.reset();

	}
}
