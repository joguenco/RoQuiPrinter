# RoquiPrinter
Library to generate PDF for SRI documents electronics of Ecuador

## Requirements
- Java 21
- Gradle 8.5
- Jaspersoft Studio 6.20

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
mvn install:install-file -Dfile=./app/build/libs/RoquiPrinter-1.0.0.jar -DgroupId=dev.mestizos.printer -DartifactId=RoquiPrinter -Dversion=1.0.0 -Dpackaging=jar
```
