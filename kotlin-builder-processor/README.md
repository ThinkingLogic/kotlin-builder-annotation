# Debugging the builder processor

Attach a debugger in Intellij to the KotlinCompileDaemon (Run -> Attach to process).
Run the following from this directory:
```
gradle -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n" clean build
```
