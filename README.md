# kotlin-builder-annotation
A builder annotation for Kotlin interoperability with Java.
This project aims to be a minimal viable replacement for the Lombok @Builder plugin for Kotlin code.

[![Build Status](https://travis-ci.com/ThinkingLogic/kotlin-builder-annotation.svg?branch=master)](https://travis-ci.com/ThinkingLogic/kotlin-builder-annotation)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Usage
#### Import `kotlin-builder-annotation` and `kotlin-builder-processor`
And configure the [Kotlin annotation processor (kapt)](https://kotlinlang.org/docs/reference/kapt.html).
##### Gradle
```gradle
...
apply plugin: 'kotlin-kapt'
...
dependencies {
    ...
    implementation 'com.thinkinglogic.builder:kotlin-builder-annotation:1.0.3'
    kapt 'com.thinkinglogic.builder:kotlin-builder-processor:1.0.3'
}
```
##### Maven
```maven
...
<dependencies>
    <dependency>
        <groupId>com.thinkinglogic.builder</groupId>
        <artifactId>kotlin-builder-annotation</artifactId>
        <version>1.0.3</version>
    </dependency>
    ...
</dependencies>
...
<execution>
    <id>kapt</id>
    <goals>
        <goal>kapt</goal>
    </goals>
    <configuration>
        <sourceDirs>
            <sourceDir>src/main/kotlin</sourceDir>
            <sourceDir>src/main/java</sourceDir>
        </sourceDirs>
        <annotationProcessorPaths>
            <!-- Specify your annotation processors here. -->
            <annotationProcessorPath>
                <groupId>com.thinkinglogic.builder</groupId>
                <artifactId>kotlin-builder-processor</artifactId>
                <version>1.0.3</version>
            </annotationProcessorPath>
        </annotationProcessorPaths>
    </configuration>
</execution>

```

#### Annotate your class(es) with the @Builder annotation
```kotlin
import com.thinkinglogic.builder.annotation.Builder

@Builder
data class MyDataClass(
        val notNullString: String,
        val nullableString: String?
)
```
That's it! Client code can now use a builder to construct instances of your class.

Unlike Lombok there's no bytecode manipulation, so we don't expose a `MyDataClass.builder()` static method.
Instead clients create a `new MyDataClassBuilder()`, for instance:

```java
public class MyDataFactory {
    public MyDataClass create() {
        return new MyDataClassBuilder()
                .notNullString("Foo")
                .nullableString("Bar")
                .build();
    }
}
```
The builder will check for required fields, so  
 `new MyDataClassBuilder().notNullString(null);`  
 would throw an `IllegalArgumentException` and  
 `new MyDataClassBuilder().nullableString("Bar").build();`  
 would throw an `IllegalStateException` naming the required field ('notNullString' in this case), while  
 `new MyDataClassBuilder().notNullString("Foo").build();`  
 would return a new instance with a null value for 'nullableString'.

#### Default values
Kotlin doesn't retain information about default values after compilation, so it cannot be accessed during annotation processing. 
Instead we must use the `@DefaultValue` annotation to tell the builder about it: 
```kotlin
import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.DefaultValue

@Builder
data class MyDataClass(
        val notNullString: String,
        val nullableString: String?,
        @DefaultValue("myDefaultValue") val stringWithDefault: String = "myDefaultValue",
        @DefaultValue("LocalDate.MIN") val defaultDate: LocalDate = LocalDate.MIN
)
```
(The text value of `@DefaultValue` is interpreted directly as Kotlin code, but for convenience double quotes are added around a String value).

#### Collections containing nullable elements
Information about the nullability of elements in a collection is lost during compilation, so there is a `@NullableType` annotation:
```kotlin
import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.NullableType

@Builder
data class MyDataClass(
        val setOfLongs: Set<Long>,
        @NullableType val setOfNullableLongs: Set<Long?>
)
```

#### Mutable collections
Information about the mutability of collections and maps is lost during compilation, so there is an `@Mutable` annotation:
```kotlin
import com.thinkinglogic.builder.annotation.Builder
import com.thinkinglogic.builder.annotation.Mutable

@Builder
data class MyDataClass(
        val setOfLongs: Set<Long>,
        @Mutable val listOfStrings: MutableList<String>
)
```

#### Constructor parameters
The `@Builder` annotation may be placed on a constructor instead of the class - useful if you have constructor-only parameters:
```kotlin
import com.thinkinglogic.builder.annotation.Builder

class MyClass
@Builder
constructor(
        forename: String,
        surname: String,
        val nickName: String?
) {
    val fullName = "$forename $surname"
}
```  
Examples of all of the above may be found in the kotlin-builder-example-usage sub-project.
## License
This software is Licenced under the [MIT License](LICENSE.md).

