package tourGuide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.Getter;
import lombok.Setter;
import tourGuide.tracker.TrackerThread;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private TrackerThread trackerThread;

	@Value("${cores.number}")
	private String numberOfCores;

	@Setter
	@Getter
	@Value("${internal.liveTestMode.enabled}")
	private boolean liveTestMode;

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

		// We only start the tracker thread if the mode has been set to live testing
		if (isLiveTestMode()) {
			trackerThread.start();
		}
	}

}
