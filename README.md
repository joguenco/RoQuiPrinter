# RoQuiPrinter
Library to generate PDF for SRI documents electronics of Ecuador

## Requirements
- Java 21
- Gradle 8.14.2
- Jaspersoft Studio 6.20

## Upgrade Gradle
```
gradle wrapper --gradle-version 8.14.2
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
mvn install:install-file -Dfile=./app/build/libs/RoQuiPrinter-1.1.0.jar -DgroupId=dev.joguenco.printer -DartifactId=RoQuiPrinter -Dversion=1.1.0 -Dpackaging=jar
```
