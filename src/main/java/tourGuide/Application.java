package tourGuide;

import org.springframework.beans.factory.annotation.Autowired;
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

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Tracker tracker = new Tracker(tourGuideService, userService);
	}

}
