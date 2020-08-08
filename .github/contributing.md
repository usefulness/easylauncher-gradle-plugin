## Issues
Feel free to file any bug you found or idea you came up with. I'll try to address them whenever I find some time.
It would be great if you provided your project's configuration (flavors & buildTypes) together with library config.

## Pull request
The goal is to have all cases tested. There are 3 types of tests:
1. Unit tests - useful during development
2. Gradle TestKit tests - they make sure interaction with plugin (in users gradle scripts) works as expected
3. Functional ones - each `sample` project serves as a test case.
Together with [Screenshot](https://github.com/facebook/screenshot-tests-for-android/) tests they ensure proper behavior on everyone's project.

It would be much appreciated if you added tests reproducing the issue (i.e. by adding new sample project)
