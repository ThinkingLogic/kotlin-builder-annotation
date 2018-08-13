# kotlin-builder-annotation
A builder annotation for Kotlin interoperability with Java.
This project aims to be a minimal viable replacement for the Lombok @Builder plugin for Kotlin code.

[![Build Status](https://travis-ci.com/ThinkingLogic/kotlin-builder-annotation.svg?branch=master)](https://travis-ci.com/ThinkingLogic/kotlin-builder-annotation.svg?branch=master)

Named constructor parameters with optional values mean that Kotlin code doesn't require the builder pattern any more,
_unless_ you're writing code that will be used by Java projects 
(else you consign your java clients to using unhelpful constructors with many parameters).



## License
This software is Licenced under the [MIT License](LICENSE.md).

