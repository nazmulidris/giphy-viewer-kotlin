# Giphy Viewer Android app written in Kotlin

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Design](#design)
  - [Video of app in action](#video-of-app-in-action)
- [References](#references)
  - [ViewModel testing, MockK, Espresso (AndroidJUnit4)](#viewmodel-testing-mockk-espresso-androidjunit4)
  - [Kotlin object expressions, declarations, constructors, constants, coroutines](#kotlin-object-expressions-declarations-constructors-constants-coroutines)
  - [Anko logging](#anko-logging)
  - [Android testing overview (w/ Kotlin)](#android-testing-overview-w-kotlin)
  - [Roboelectric (and unit tests)](#roboelectric-and-unit-tests)
  - [Testing async code in tests](#testing-async-code-in-tests)
  - [Removing deprecations from androidTestImplementation](#removing-deprecations-from-androidtestimplementation)
  - [AssertJ](#assertj)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

You can find an in-depth exploration of this project in [this blog post on developerlife.com](https://developerlife.com/2019/05/11/kotlin-vs-java/).

# Design

- **MyApplication.kt** is the custom Application class that acts as a
  dependency injection component that provides various objects that are needed
  in various places throughout the app.

- **State.kt** contains sealed classes that represent various events and state
  representations that are used throughout the app.

- **Util.kt** contains a set of extension function expressions and typedefs that
  are used throughout the app. Coroutine functions are included here as well.

- **MyViewModel.kt** contains the AndroidViewModel that holds the data that's
  loaded from the network service, and also exposes the network service end points
  to the rest of the app.

- **NetworkService.kt** contains the integration w/ the Giphy Android SDK. Calls
  from the ViewModel are passed on the methods of GiphyClient, which ends up
  making calls to the Giphy Android API.

- **RecyclerViewManager.kt** contains the RecyclerView data adapter,
  RowViewModel implementation, and configuration w/ the
  StaggeredGridLayoutManager. It also hooks into the ViewModel's observable to
  react to changes in the underlying data (as a result of network service request
  being made from various parts of the app).

- **NetworkServiceTest.kt** contains the classes that connect to web services to
  load data over the network (GiphyClient SDK).

- **Unit tests (test/)** test classes in State.kt and some functions in
  NetworkService (using Roboelectric).

- **Instrumented tests (androidTest/)** test classes in NetworkServiceTest.kt over
  the network.

<img src="https://raw.githubusercontent.com/nazmulidris/giphy-viewer-kotlin/main/files/arch-diagram.png" style="width:100%;"/>

## Video of app in action

<img src="https://github.com/nazmulidris/giphy-viewer-kotlin/blob/main/files/giphy.gif?raw=true" style="width:50%;"/>

# References

## ViewModel testing, MockK, Espresso (AndroidJUnit4)

- https://mockk.io/ANDROID.html
- https://medium.com/@marco_cattaneo/unit-testing-with-mockito-on-kotlin-android-project-with-architecture-components-2059eb637912
- https://blog.kotlin-academy.com/mocking-is-not-rocket-science-mockk-features-e5d55d735a98
- https://medium.com/mindorks/android-testing-part-1-espresso-basics-7219b86c862b

## Kotlin object expressions, declarations, constructors, constants, coroutines

- https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html
- https://geoffreymetais.github.io/code/coroutines/#
- https://codelabs.developers.google.com/codelabs/kotlin-coroutines
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

# Change master to main

The
[Internet Engineering Task Force (IETF) points out](https://tools.ietf.org/id/draft-knodel-terminology-00.html#rfc.section.1.1.1)
that "Master-slave is an oppressive metaphor that will and should never become fully detached from history" as well as
"In addition to being inappropriate and arcane, the
[master-slave metaphor](https://github.com/bitkeeper-scm/bitkeeper/blob/master/doc/HOWTO.ask?WT.mc_id=-blog-scottha#L231-L232)
is both technically and historically inaccurate." There's lots of more accurate options depending on context and it
costs me nothing to change my vocabulary, especially if it is one less little speed bump to getting a new person excited
about tech.

You might say, "I'm all for not using master in master-slave technical relationships, but this is clearly an instance of
master-copy, not master-slave"
[but that may not be the case](https://mail.gnome.org/archives/desktop-devel-list/2019-May/msg00066.html). Turns out the
original usage of master in Git very likely came from another version control system (BitKeeper) that explicitly had a
notion of slave branches.

- https://dev.to/lukeocodes/change-git-s-default-branch-from-master-19le
- https://www.hanselman.com/blog/EasilyRenameYourGitDefaultBranchFromMasterToMain.aspx

[#blacklivesmatter](https://blacklivesmatter.com/)
