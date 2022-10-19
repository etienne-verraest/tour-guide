# Tour Guide
## What is Tour Guide ?
TourGuide is an application from TripMaster company. The application allows users to get information about nearby tourist attractions (or monuments).
Reward points are awarded for each visit and are redeemable for travel discounts.

# Inner workings of the application

## Technical Stack
TourGuide is made with the following tech stack :
- Java 8 and Spring Framework
- Gradle for the management of the application lifecycle and its dependencies
- GitLab CI/CD for continuous integration

## Running the live test mode
TourGuide service is made with a live test mode, which will generate users and rewards randomly. The application allows to test the execution time of the different services according to the number of users.

To enable this mode, please edit the `application.properties` file in `src/main/resources`. There are 2 properties to edit : 
1. `internal.liveTestMode.enabled` : which takes a boolean and allow live mode running
2. `internal.userNumber` : which takes an integer and modify the number of simulated users.

**It is important to disable the live test mode while running jUnit tests**.

## Testing the different endpoints
TourGuide service comes up with pre-created **postman collections**, which can be found in `src/main/resources` folder.

| Type        |   Endpoint          | Purpose                                          |
|-------------|---------------------|--------------------------------------------------|
| GET         | getLocation         | Get location for a given user                    |
| GET         | getNearbyAttractions | Get 5 closest attractions for a given user      |
| GET         | getAllCurrentLocations | Get current location of every user            |
| POST        | setUserPreferences  | Set User Preferences for personalized trip deals |
| GET         | getTripDeals        | Get Trip Deals based on user preferences         |
| GET         | getRewards          | Get rewards for a given user                     |

