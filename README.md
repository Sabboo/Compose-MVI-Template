# Compose-MVI-Template

This repository contains a detailed sample app that apply MVI presentation architecture and uses Coroutines, Hilt, Compose. 

---
### Build and Run

To be able to build and run the app, you must have `JDK 17` installed on your machine.
After cloning the project and syncing gradle, run the following command to compile, install and run the application.
```sh
./gradlew assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk && adb shell am start -n com.example.compose_template/.MainActivity
```

---

### Architectural Choices

##### - MVI Presentation Pattern
ViewModel handles all business logic, state management, and interactions with use cases or repositories. States are handled using Intents and State model to be updated and reflected on UI easily.

##### - Clean Architecture
Features are split into presentation, domain, and data layers:
- Presentation: Composables, ViewModels and State Handling.
- Domain: Use cases encapsulate business rules.
- Data: Repositories and API services.

##### - Kotlin Coroutines & Flow
Coroutines are used for asynchronous operations. Flow is used for exposing reactive streams from the repository/use cases to ViewModel.

##### - Jetpack Compose
Compose is used for the UI layer. Facilitate the UI design using declarative UI.

##### - Hilt for Dependency Injection
Hilt is used for dependency injection across ViewModels and repositories.

##### - Navigation
Jetpack Compose Navigation handles screen transitions.

##### - Caching & Pagination
Cached data is stored in memory in the ViewModel for offline-first feel.
Pagination logic ensures efficient network usage and smooth scrolling for long lists.

##### - Testing
ViewModels are unit-testable. Uses Turbine for testing Flow emissions. Mockito for mocking.

---

### Assumptions and Decisions
  1- The first assumption was related to search functionality and wheather we should handle pagnination for search results -> Discision was made to apply pagination for search results returned from server side.  
  2- Next, the assumption related to how the search functionality will behave in respect to normal data retreival. We will show data retrieved from server side and when the user start searching should we fetch the search results from server side only or we can make use of the already retrieved data and return an instant search results from the available snapshot -> Descision was made to apply this behavior to return an instant search results from the available snapshot until the app receive results from server side.  
  3- Another assumption related to the previous point and wheather we should store these data in memory or persistent storage -> Discision was made to cache them in memory only as it wasn't stated that we need an offline first approach.
  
---

### Next Steps
 - [ ] Add UI Tests
