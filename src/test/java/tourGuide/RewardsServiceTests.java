package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.domain.User;
import tourGuide.domain.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class RewardsServiceTests {

	@InjectMocks
	private RewardsService rewardsServiceMock;

	@Mock
	private UserService userServiceMock;

	@Mock
	private TourGuideService tourGuideServiceMock;

	private GpsUtil gpsUtil = new GpsUtil();

	private RewardCentral rewardsCentral = new RewardCentral();

	private User mockUser;

	private List<User> listOfUserMock = new ArrayList<>();

	@Before
	public void initUser() {
		// This call is important in order to reset the list when calling getAllUsers()
		userServiceMock.initializeInternalUsers(0);

		// Initializing users
		mockUser = new User(UUID.randomUUID(), "Alpha", "000", "alpha@tourGuide.com");
		listOfUserMock.add(mockUser);

		// Adding users to the service
		userServiceMock.addUser(mockUser);
	}

	@Test
	public void userGetRewardsTest_ShouldReturn_RewardsList() throws InterruptedException, ExecutionException {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);
		VisitedLocation visitedLocation = new VisitedLocation(mockUser.getUserId(), attraction, new Date());
		CompletableFuture<VisitedLocation> visitedLocationFuture = CompletableFuture.completedFuture(visitedLocation);
		when(tourGuideServiceMock.trackUserLocation(any(User.class))).thenReturn(visitedLocationFuture);

		UserReward userReward = new UserReward(visitedLocation, attraction,
				rewardsServiceMock.getRewardPoints(attraction, mockUser));
		mockUser.addUserReward(userReward);

		// ACT
		mockUser.addToVisitedLocations(visitedLocation);
		tourGuideServiceMock.trackUserLocation(mockUser).get();

		// ASSERT
		List<UserReward> userRewards = mockUser.getUserRewards();
		assertThat(userRewards).hasSize(1);
	}

	@Test
	public void isWithinAttractionProximityTest_ShouldReturn_True() {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);
		VisitedLocation visitedLocation = new VisitedLocation(mockUser.getUserId(), attraction, new Date());

		// ASSERT
		assertTrue(rewardsServiceMock.isWithinAttractionProximity(attraction, visitedLocation.location));
	}

	@Test
	public void isNearAttractionTest_ShouldReturn_True() {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);
		VisitedLocation visitedLocation = new VisitedLocation(mockUser.getUserId(), attraction, new Date());

		// ACT
		assertTrue(rewardsServiceMock.nearAttraction(visitedLocation, attraction));
	}

	@Test
	public void getRewardsPointsTest_ShouldReturn_PositiveResult() {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);

		// ACT
		int points = rewardsCentral.getAttractionRewardPoints(attraction.attractionId, mockUser.getUserId());

		// ASSERT
		assertThat(points).isNotNegative();

	}

	@Test
	public void getDistanceTest_ShouldBeLessThan_100Miles() {

		// ARRANGE
		Location location1 = new Location(40.689930310941605, -74.04536481320433);
		Location location2 = new Location(41.31882087521971, -72.92197916341115);

		// ACT
		double distance = rewardsServiceMock.getDistance(location1, location2);

		// ASSERT
		assertThat(distance).isLessThan(100);
	}

}
