# RoQuiPrinter
Library to generate PDF for SRI documents electronics of Ecuador

## Requirements
- Java 21
- Gradle 8.14.3
- Jaspersoft Studio 6.21

## Upgrade Gradle
```
gradle wrapper --gradle-version 8.14.3
```

## Check and format Java Code
- Check
```
gradle spotlessCheck
```
- Format
```
gradle spotlessApply
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
### GNU/Linux or MacOS
```
mvn install:install-file -Dfile=./app/build/libs/RoQuiPrinter-1.1.0.jar -DgroupId=dev.joguenco.printer -DartifactId=RoQuiPrinter -Dversion=1.1.0 -Dpackaging=jar
```
### Windows
In CMD terminal, not in PowerShell
```
mvn install:install-file -Dfile=.\app\build\libs\RoQuiPrinter-1.1.0.jar -DgroupId=dev.joguenco.printer -DartifactId=RoQuiPrinter -Dversion=1.1.0 -Dpackaging=jar
```
## Jaspersoft Studio
- Open Jaspersoft Studio
- File -> Import Project -> General -> Existing Projects into Workspace
- Select root directory: RoQuiPrinter/jasperproject/RoQuiPrinter/sri
### Edit report
When editing a report, you need copy and paste the .jrxml and .jasper files to the resources folder in test project.
### Test
```
gradle test
```
The generated PDF will be in:
app/build/classes/java/test/pdf