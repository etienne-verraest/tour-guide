package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.domain.UserPreferences;
import tourGuide.domain.response.NearbyAttractionsResponse;
import tourGuide.domain.response.UserLocationResponse;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TourGuideServiceTests {

	@InjectMocks
	private TourGuideService tourGuideServiceMock;

	@Mock
	private RewardsService rewardsServiceMock;

	@Mock
	private UserService userServiceMock;

	private GpsUtil gpsUtil = new GpsUtil();

	private TripPricer tripPricer = new TripPricer();

	@Value("${tripPricer.api.key}")
	public String tripPricerApiKey;

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
	public void testGetTripDeals_ShouldReturn_ListOfProviders() {

		// ARRANGE
		// Users information
		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setNumberOfAdults(1);
		userPreferences.setNumberOfChildren(0);
		userPreferences.setTripDuration(7);

		// Currency preferences
		CurrencyUnit currency = Monetary.getCurrency("USD");
		userPreferences.setCurrency(currency);
		userPreferences.setLowerPricePoint(Money.of(0, currency));
		userPreferences.setHighPricePoint(Money.of(Integer.MAX_VALUE, currency));

		// ACT
		mockUser.setUserPreferences(userPreferences);
		List<Provider> providers = tourGuideServiceMock.getTripDeals(mockUser);

		// ASSERT
		assertThat(providers).isNotNull();

	}

	@Test
	public void testGetUserLocation_ShouldReturn_VisitedLocation() throws InterruptedException, ExecutionException {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);
		VisitedLocation visitedLocation = new VisitedLocation(mockUser.getUserId(), attraction, new Date());
		mockUser.addToVisitedLocations(visitedLocation);

		// ACT
		VisitedLocation userLocation = tourGuideServiceMock.getUserLocation(mockUser);

		// ASSERT
		assertThat(visitedLocation).isEqualTo(userLocation);

	}

	@Test
	public void testTrackUserLocation_ShouldReturn_NonNullResponse() throws InterruptedException, ExecutionException {

		// ACT
		VisitedLocation response = tourGuideServiceMock.trackUserLocation(mockUser).get();

		// ASSERT
		assertThat(response).isNotNull();

	}

	@Test
	public void testGetNearByAttractions_ShouldReturn_5_Attractions() throws InterruptedException, ExecutionException {

		// ACT
		NearbyAttractionsResponse response = tourGuideServiceMock.getNearByAttractions(mockUser);

		// ASSERT
		assertThat(response.getNearbyAttractions()).hasSize(5);
	}

	@Test
	public void testGetAllUsersLocation_ShouldReturn_2_User() throws InterruptedException, ExecutionException {

		// ARRANGE
		User secondUser = new User(UUID.randomUUID(), "Bravo", "000", "bravo@tourGuide.com");
		listOfUserMock.add(secondUser);
		when(userServiceMock.getAllUsers()).thenReturn(listOfUserMock);

		// ACT
		List<UserLocationResponse> response = tourGuideServiceMock.getAllUsersLocation();

		// ASSERT
		assertThat(response).hasSize(2);

	}

}
