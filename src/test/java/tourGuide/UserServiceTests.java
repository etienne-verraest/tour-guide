package tourGuide;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.location.VisitedLocation;
import tourGuide.domain.User;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTests {

	@InjectMocks
	TourGuideService tourGuideServiceMock;

	@Mock
	UserService userServiceMock;

	@Mock
	RewardsService rewardsServiceMock;

	private User firstUser;

	private List<User> listOfUser = new ArrayList<>();

	@Before
	public void initUsers() {

		// This call is important in order to reset the list when calling getAllUsers()
		userServiceMock.initializeInternalUsers(0);

		// Initializing users
		firstUser = new User(UUID.randomUUID(), "Alpha", "000", "alpha@tourGuide.com");
		listOfUser.add(firstUser);

		// Adding users to the service
		userServiceMock.addUser(firstUser);
	}

	@Test
	public void getUserLocation_ShouldReturn_FirstUserLocation() throws InterruptedException, ExecutionException {

		// ACT
		VisitedLocation visitedLocation = tourGuideServiceMock.trackUserLocation(firstUser).get();

		// ASSERT
		assertEquals(visitedLocation.userId, firstUser.getUserId());
	}

	@Test
	public void addUser_ShouldBeSuccessful() {

		// ARRANGE
		List<User> mockSecondList = listOfUser;

		User secondUser = new User(UUID.randomUUID(), "Bravo", "111", "bravo@tourGuide.com");
		userServiceMock.addUser(secondUser);
		mockSecondList.add(secondUser);

		when(userServiceMock.getAllUsers()).thenReturn(mockSecondList);

		// ACT
		List<User> users = userServiceMock.getAllUsers();

		// ASSERT
		assertEquals(secondUser.getUserName(), users.get(1).getUserName());
	}

	@Test
	public void getUser_ShouldReturn_FirstUser() {

		// ARRANGE
		when(userServiceMock.getUser(anyString())).thenReturn(firstUser);

		// ACT
		User retrivedUser = userServiceMock.getUser("Alpha");

		// ASSERT
		assertEquals(firstUser.getUserName(), retrivedUser.getUserName());
	}

	@Test
	public void getAllUsersTest_ShouldReturn_FirstUser() {

		// ARRANGE
		when(userServiceMock.getAllUsers()).thenReturn(listOfUser);

		// ACT
		List<User> users = userServiceMock.getAllUsers();

		// ASSERT
		assertEquals(users.get(0).getUserName(), firstUser.getUserName());
	}
}
