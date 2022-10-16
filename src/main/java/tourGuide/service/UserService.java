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
import tourGuide.util.LocationGenerator;

@Service
@Slf4j
public class UserService {

	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory

	// Internal users configuration
	public int internalUserNumber = 1000;

	// Internal users generation
	private final Map<String, User> internalUserMap = new HashMap<>();

	public void initializeInternalUsers() {
		IntStream.range(0, internalUserNumber).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			LocationGenerator.generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		log.debug("Created {} internal test users.", internalUserNumber);
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().parallelStream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
}
