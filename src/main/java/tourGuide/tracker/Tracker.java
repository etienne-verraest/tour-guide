package tourGuide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.service.TourGuideService;

@Slf4j
public class Tracker extends Thread {

	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private boolean stop = false;

	public Tracker(TourGuideService tourGuideService) {
		this.tourGuideService = tourGuideService;

		executorService.submit(this);
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while (true) {
			if (Thread.currentThread().isInterrupted() || stop) {
				log.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			log.debug("Begin Tracker. Tracking {} users.", users.size());

			stopWatch.start();
			users.forEach(u -> tourGuideService.trackUserLocation(u));
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
}
