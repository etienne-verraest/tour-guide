package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@Slf4j
public class Tracker extends Thread {

	private TourGuideService tourGuideService;

	private UserService userService;

	private boolean testMode = true;

	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService, UserService userService) {
		this.tourGuideService = tourGuideService;
		this.userService = userService;

		if (testMode) {
			userService.initializeInternalUsers();
		}

		// Executing the run method of the Tracker Thread
		executorService.submit(this);
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				log.debug("Tracker stopping");
				break;
			}

			List<User> users = userService.getAllUsers();
			log.debug("Begin Tracker. Tracking {} users.", users.size());

			stopWatch.start();

			for (User u : users) {
				tourGuideService.trackUserLocation(u);
			}

			stopWatch.stop();

			log.debug("Tracker Time Elapsed: {} seconds.", TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
			stopWatch.reset();

			try {
				log.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopTracking();
			}
		});
	}
}
