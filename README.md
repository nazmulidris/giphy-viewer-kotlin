# Design

- **MyApplication.kt** is the custom Application class that acts as a
  dependency injection component that provides various objects that are needed
  in various places throughout the app.

- **State.kt** contains sealed classes that represent various events and state
  representations that are used throughout the app.

- **NetworkService.kt** contains the classes that connect to web services to
  load data over the network (GiphyClient SDK).

- **Unit tests (test/)** test classes in State.kt and some functions in
  NetworkService (using Roboelectric).

- **Integration tests (androidTest/)** test classes in NetworkService.kt over
  the network.

# References

## ViewModel testing, MockK, Espresso (AndroidJUnit4)
- https://mockk.io/ANDROID.html
- https://medium.com/@marco_cattaneo/unit-testing-with-mockito-on-kotlin-android-project-with-architecture-components-2059eb637912
- https://blog.kotlin-academy.com/mocking-is-not-rocket-science-mockk-features-e5d55d735a98
- https://medium.com/mindorks/android-testing-part-1-espresso-basics-7219b86c862b

## Kotlin object expressions, declarations, constructors, constants

- https://kotlinlang.org/docs/reference/object-declarations.html#object-expressions
- https://stackoverflow.com/a/34624907/2085356
- https://blog.egorand.me/where-do-i-put-my-constants-in-kotlin/

## Anko logging

- https://github.com/Kotlin/anko/wiki/Anko-Commons-%E2%80%93-Logging

## Android testing overview (w/ Kotlin)

- https://fernandocejas.com/2017/02/03/android-testing-with-kotlin

## Roboelectric (and unit tests)

- http://robolectric.org/getting-started
- http://robolectric.org/migrating
- https://stackoverflow.com/a/52923630/2085356

## Testing async code in tests

- https://stackoverflow.com/a/3802487/2085356

## Removing deprecations from androidTestImplementation

- https://stackoverflow.com/a/52776938/2085356
- https://stackoverflow.com/a/52932361/2085356

## AssertJ

- https://www.lordcodes.com/posts/testing-on-android-using-junit-5
