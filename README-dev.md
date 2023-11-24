README-DEV
=======================

## Building the lib

```
export JAVA_HOME=/path/to/jdk1.8.0_181/
./gradlew build --stacktrace
```

## Installing lib for local testing
```
mvn install:install-file -Dfile=universal/build/libs/universal-0.0.7-samourai.jar -DgroupId=io.samourai.code.whirlpool.Tor_Onion_Proxy_Library -DartifactId=universal -Dversion=0.0.7-samourai -Dpackaging=jar
mvn install:install-file -Dfile=java/build/libs/java-0.0.7-samourai.jar -DgroupId=io.samourai.code.whirlpool.Tor_Onion_Proxy_Library -DartifactId=java -Dversion=0.0.7-samourai -Dpackaging=jar
```

Tor binaries are packaged from https://dist.torproject.org/torbrowser/

