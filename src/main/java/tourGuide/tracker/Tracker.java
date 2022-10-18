package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@Slf4j
@Component
public class Tracker {

	@Autowired
	private TourGuideService tourGuideService;

	@Autowired
	private UserService userService;

	@Getter
	@Value("${internal.liveTestMode.enabled}")
	public boolean liveTestMode;

	@Setter
	@Getter
	@Value("${internal.userNumber}")
	public int internalUserNumber;

	/**
	 * When tracker has been created, we check if the testMode is enabled
	 * If test mode is enabled, we initialize internal users
	 */
	@PostConstruct
	public void initializeTracker() {
		if (isLiveTestMode()) {
			userService.initializeInternalUsers(internalUserNumber);
		}
	}

	/**
	 * This method starts the tracker.
	 *
	 * To improve performances, the method uses parallel streams.
	 * Parallel streams will split the list into N chunks where N is your number of cores.
	 * To get equal response time, core are set to 10 (changeable in application.properties)
	 *
	 * The time taken is outputted in milliseconds.
	 */
	public void startTracker() {
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
				tourGuideService.trackUserLocation(u).get();
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
