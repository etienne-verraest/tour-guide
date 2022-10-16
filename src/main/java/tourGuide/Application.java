package tourGuide;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;

@SpringBootApplication
public class Application implements CommandLineRunner {

	private TourGuideService tourGuideService = new TourGuideService();

	private UserService userService = new UserService();

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Tracker tracker = new Tracker(tourGuideService, userService);
		tracker.startTrackingUsers();
	}

}
