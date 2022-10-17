package tourGuide.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import tourGuide.domain.User;
import tourGuide.util.LocationGeneratorUtil;

@Service
@Slf4j
public class UserService {

	/**
	 *  Database connection will be used for external users, but for testing purposes
	 *  internal users are provided and stored in memory
	 */
	private Map<String, User> internalUserMap = new HashMap<>();

	/**
	 * Internal users generation method
	 * The number of users to create is set in the application.properties file
	 * The method will only be called if the test mode is enabled
	 *
	 */
	public void initializeInternalUsers(int numberOfUsers) {
		IntStream.range(0, numberOfUsers).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			LocationGeneratorUtil.generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		log.debug("[User Service] Created {} internal test users.", numberOfUsers);
	}

	/**
	 * Get a user given its username
	 *
	 * @param userName						String : The username of the user to fetch
	 * @return								User : Return a User Object if it exists
	 */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	/**
	 * Converting the User HashMap into a List for performance purpose
	 *
	 * @return								List<User> : List containing all users
	 */
	public List<User> getAllUsers() {
		return internalUserMap.values().parallelStream().collect(Collectors.toList());
	}

	/**
	 * Add an internal user map, if the user already exists then the creation will fail
	 *
	 * @param user							User : The user to add
	 */
	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
}
