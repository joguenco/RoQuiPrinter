# RoquiPrinter
Library to generate PDF for SRI documents electronics of Ecuador

## Requirements
- Java 21
- Gradle 8.13
- Jaspersoft Studio 6.20

## Upgrade Gradle
```
gradle wrapper --gradle-version 8.13
```

## Test
```
gradle test
```

## Build
```
gradle build
```

## Publish in local maven repository
```
mvn install:install-file -Dfile=./app/build/libs/RoquiPrinter-1.0.0.jar -DgroupId=dev.joguenco.printer -DartifactId=RoquiPrinter -Dversion=1.0.0 -Dpackaging=jar
```
