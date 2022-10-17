package tourGuide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private TourGuideService tourGuideService;

	@Autowired
	private UserService userService;

	@Value("${cores.number}")
	public String numberOfCores;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		/**
		 * To have the same result output for each user, we are setting the parallelstream
		 * pool to 10 cores (the value can be changed in application.properties)
		 */
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", numberOfCores);

		// Starting the application tracker
		Tracker tracker = new Tracker(tourGuideService, userService);
		tracker.startTrackingUsers();
	}

}
