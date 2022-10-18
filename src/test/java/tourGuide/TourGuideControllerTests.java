package tourGuide;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.controller.TourGuideController;
import tourGuide.domain.User;
import tourGuide.domain.dto.UserPreferencesDto;
import tourGuide.domain.response.AttractionInformation;
import tourGuide.domain.response.NearbyAttractionsResponse;
import tourGuide.domain.response.UserLocationResponse;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.tracker.TrackerThread;

@RunWith(SpringRunner.class)
@WebMvcTest(TourGuideController.class)
public class TourGuideControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private TrackerThread trackerThread; // Had to define this bean to load ApplicationContext correctly

	@MockBean
	private TourGuideService tourGuideServiceMock;

	@MockBean
	private RewardsService rewardsServiceMock;

	@MockBean
	private UserService userServiceMock;

	private GpsUtil gpsUtil = new GpsUtil();

	private User mockUser;

	private static final String USERNAME = "Alpha";

	@Before
	public void setUp() throws Exception {
		// This call is important in order to reset the list when calling getAllUsers()
		userServiceMock.initializeInternalUsers(0);

		// Initializing users
		mockUser = new User(UUID.randomUUID(), USERNAME, "000", "alpha@tourGuide.com");

		// Adding users to the service
		userServiceMock.addUser(mockUser);
	}

	@Test
	public void testDefaultUrl_ShouldReturn_StatusOk() throws Exception {

		// ACT AND ASSERT
		mockMvc.perform(get("/")).andExpect(status().isOk());
	}

	@Test
	public void testGetLocation_ShouldReturn_StatusOk() throws Exception {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);
		VisitedLocation visitedLocation = new VisitedLocation(mockUser.getUserId(), attraction, new Date());
		when(userServiceMock.getUser(anyString())).thenReturn(mockUser);
		when(tourGuideServiceMock.getUserLocation(any(User.class))).thenReturn(visitedLocation);

		// ACT AND ASSERT
		mockMvc.perform(get("/getLocation").param("userName", USERNAME)).andExpect(status().isOk())
				.andExpect(jsonPath("$.location.attractionName").value("Disneyland"));
	}

	@Test
	public void testGetNearbyAttractions_ShouldReturn_StatusOk() throws Exception {

		// ARRANGE
		Attraction attraction = gpsUtil.getAttractions().get(0);
		NearbyAttractionsResponse response = new NearbyAttractionsResponse();
		Location location = new Location(40.689930310941605, -74.04536481320433);
		response.setUserLocation(location);

		List<AttractionInformation> attractions = new ArrayList<>();
		AttractionInformation attractionInformation = new AttractionInformation();
		attractionInformation.setAttractionLocation(new Location(attraction.longitude, attraction.latitude));
		attractionInformation.setAttractionRewardPoints(100);
		attractionInformation.setNameOfAttraction(attraction.attractionName);
		attractionInformation.setDistanceToAttraction(100.0);
		attractions.add(attractionInformation);
		response.setNearbyAttractions(attractions);

		when(userServiceMock.getUser(anyString())).thenReturn(mockUser);
		when(tourGuideServiceMock.getNearByAttractions(any(User.class))).thenReturn(response);

		// ACT AND ASSERT
		mockMvc.perform(get("/getNearbyAttractions").param("userName", USERNAME)).andExpect(status().isOk())
				.andExpect(jsonPath("$.nearbyAttractions[0].nameOfAttraction").value("Disneyland"));
	}

	@Test
	public void testGetAllCurrentLocations_ShouldReturn_StatusOk() throws Exception {

		// ARRANGE
		UserLocationResponse userLocationResponse = new UserLocationResponse();
		userLocationResponse.setUserId(mockUser.getUserId());
		userLocationResponse.setUserLocation(new Location(40.689930310941605, -74.04536481320433));

		List<UserLocationResponse> response = new ArrayList<>();
		response.add(userLocationResponse);

		when(userServiceMock.getUser(anyString())).thenReturn(mockUser);
		when(tourGuideServiceMock.getAllUsersLocation()).thenReturn(response);

		// ACT AND ASSERT
		mockMvc.perform(get("/getAllCurrentLocations")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].userLocation.longitude").value("-74.04536481320433"));

	}

	@Test
	public void testGetTripDeals_ShouldReturn_StatusOk() throws Exception {

		// ARRANGE
		when(userServiceMock.getUser(anyString())).thenReturn(mockUser);

		// ACT AND ASSERT
		mockMvc.perform(get("/getTripDeals").param("userName", USERNAME)).andExpect(status().isOk());
	}

	@Test
	public void postSetUserPreferences_ShouldReturn_StatusOk() throws Exception {

		// ARRANGE
		ObjectMapper objectMapper = new ObjectMapper();
		UserPreferencesDto userPreferencesDto = new UserPreferencesDto();

		userPreferencesDto.setAttractionProximity(100);
		userPreferencesDto.setCurrency("USD");
		userPreferencesDto.setHighPricePoint(Integer.MAX_VALUE);
		userPreferencesDto.setLowerPricePoint(0);
		userPreferencesDto.setNumberOfAdults(1);
		userPreferencesDto.setNumberOfChildren(0);
		userPreferencesDto.setTripDuration(7);

		String json = objectMapper.writeValueAsString(userPreferencesDto);
		when(userServiceMock.getUser(anyString())).thenReturn(mockUser);

		// ACT AND ASSERT
		mockMvc.perform(post("/setUserPreferences").param("userName", USERNAME).content(json)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.currency.currencyCode").value("USD"));
	}

}
