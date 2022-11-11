# Android Test Utilities

Contains BaseTest, Coroutine rules and live data testing methods

[![](https://jitpack.io/v/thementalgoose/android-test-utilities.svg)](https://jitpack.io/#thementalgoose/android-test-utilities) [![main](https://github.com/thementalgoose/android-test-utilities/workflows/Main/badge.svg)](https://github.com/thementalgoose/android-test-utilities/actions)

## Installation

<details>
    <summary><code>build.gradle</code></summary>

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
</details>

<details>
    <summary><code>app/build.gradle</code></summary>

    dependencies {
        testImplementation 'com.github.thementalgoose:android-test-utilities:1.2.1'
        // Use Jitpack version if newer
    }

Jitpack version: [![](https://jitpack.io/v/thementalgoose/android-test-utilities.svg)](https://jitpack.io/#thementalgoose/android-test-utilities)
</details>


## Usage

Inside your unit tests, extend from the `BaseTest` class

```kotlin
class MyViewModel: ViewModel() {
    val output: LiveData<String> = LiveData() // ....
}
internal class MyTest: BaseTest() {

    private lateinit var sut: MyViewModel
    private fun initSUT() { sut = MyViewModel() }

    @Test
    fun `test method here`() {
        // Either
        sut.output.test {
            assertValue("Hello world!")
        }

        // Or
        val observer = sut.output.testObserve()
        observer.assertValue("Hello world!")
    }
}
```

## License

```
Copyright (C) 2022 Jordan Fisher

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```