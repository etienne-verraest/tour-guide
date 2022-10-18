package tourGuide;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
import tourGuide.domain.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.Tracker;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class RewardsServiceTests {

	@Autowired
	UserService userService;

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	RewardsService rewardsService;

	@Autowired
	Tracker tracker;

	GpsUtil gpsUtil = new GpsUtil();

	@Test
	public void userGetRewardsTest() throws InterruptedException, ExecutionException {

		// ARRANGE
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = gpsUtil.getAttractions().get(0);

		// ACT
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user).get();

		// ASSERT
		List<UserReward> userRewards = user.getUserRewards();
		log.debug("{}", userRewards);
	}

	@Test
	public void isWithinAttractionProximityTest() {
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void nearAllAttractionsTest() {

		// ARRANGE
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		userService.initializeInternalUsers(1);
		User user = userService.getAllUsers().get(0);

		// ACT
		rewardsService.calculateRewards(user);
		List<UserReward> userRewards = rewardsService.getUserRewards(user);

		log.debug("2{}", userRewards);
	}

}
