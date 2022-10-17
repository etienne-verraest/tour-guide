package tourGuide.tracker;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@Slf4j
@Component
public class TrackerThread extends Thread {

	private final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);

	@Autowired
	private TourGuideService tourGuideService;

	@Autowired
	private UserService userService;

	public TrackerThread(TourGuideService tourGuideService, UserService userService) {
		this.tourGuideService = tourGuideService;
		this.userService = userService;
	}

	@Override
	public void run() {
		Tracker tracker = new Tracker(tourGuideService, userService);
		while (true) {

			if (Thread.currentThread().isInterrupted()) {
				log.debug("[Tracker] Tracker stopped.");
				break;
			}

			tracker.startTrackingUsers();

			try {
				log.debug("[Tracker] Tracker sleeping. Next run scheduled in {} minutes.",
						trackingPollingInterval / 60);
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}

		}
	}
}
