# Debugging the builder processor

A note to self as I keep forgetting ;)

There is a `run-gradle-for-debug.sh` script that passes the following options to the kotlin daemon:
```
-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n
```

Run this script, then attach a debugger in Intellij. 
You may have to try attaching again if the process finishes without hitting your breakpoint, 
as there's more than one kotlin process that runs. 