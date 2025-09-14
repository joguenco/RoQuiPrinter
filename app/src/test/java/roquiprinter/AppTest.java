package roquiprinter;

import static org.junit.jupiter.api.Assertions.*;

import dev.joguenco.pdf.InvoiceReport;
import java.io.File;
import org.junit.jupiter.api.Test;

class AppTest {
  @Test
  void appHasAGreeting() {
    App classUnderTest = new App();
    assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
  }

  @Test
  void createInvoicePdf() {
    ClassLoader classLoader = AppTest.class.getClassLoader();

    final var xml = "2403202401999999999900110012010000000581234567812.xml";
    final var logo = "logo.jpeg";
    final var reportFolder = classLoader.getResource("./report").getPath();
    final var pdfOutFolder = classLoader.getResource(".").getPath().concat("pdf");
    createDirectory(pdfOutFolder);

    final var pathXmlFile = classLoader.getResource(xml).getPath();
    final var pathLogo = classLoader.getResource(logo).getPath();

    InvoiceReport report = new InvoiceReport(pathXmlFile, reportFolder, pathLogo, pdfOutFolder);
    assertTrue(report.pdf("2403202401999999999900110012010000000581234567812", "1901-01-01"));
  }

  void createDirectory(String path) {
    File directory = new File(path);
    if (!directory.exists()) {
      directory.mkdir();
    }
  }
}
