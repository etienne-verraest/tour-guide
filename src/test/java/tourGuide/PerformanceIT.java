package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class PerformanceIT {

	@Autowired
	UserService userService;

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	RewardsService rewardsService;

	@Autowired
	Tracker tracker;

	GpsUtil gpsUtil = new GpsUtil();

	/*
	 * A note on performance improvements:
	 *
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *
	 *
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent.
	 *
	 *     These are performance metrics that we are trying to hit:
	 *
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@Test
	public void highVolumeTrackLocation() throws InterruptedException, ExecutionException {

		// ARRANGE
		userService.initializeInternalUsers(30000);
		StopWatch stopWatch = new StopWatch();

		// ACT
		stopWatch.start();
		tracker.startTracker();
		stopWatch.stop();

		// ASSERT
		long getTime = stopWatch.getTime();
		log.debug("[TEST] highVolumeTrackLocation() Total Execution Time : {} ms.", getTime);
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(getTime));
	}

	@Test
	public void highVolumeGetRewards() {

		// ARRANGE
		userService.initializeInternalUsers(30000);
		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> users = userService.getAllUsers();
		StopWatch stopWatch = new StopWatch();
		users.parallelStream()
				.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		// ACT
		stopWatch.start();
		users.parallelStream().forEach(u -> rewardsService.calculateRewards(u));
		stopWatch.stop();

		// ASSERT
		long getTime = stopWatch.getTime();
		log.debug("[TEST] highVolumeGetRewards() Total Execution Time : {} ms.", getTime);
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(getTime));
	}

}
