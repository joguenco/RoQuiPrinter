package roquiprinter;

import static org.junit.jupiter.api.Assertions.*;

import dev.joguenco.pdf.liquidation.LiquidationReport;
import dev.joguenco.pdf.note.credit.CreditNoteReport;
import dev.joguenco.pdf.note.debit.DebitNoteReport;
import dev.joguenco.pdf.invoice.InvoiceReport;
import java.io.File;

import dev.joguenco.pdf.withhold.WithholdReport;
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

  @Test
  void createCreditNotePdf() {
    ClassLoader classLoader = AppTest.class.getClassLoader();

    final var xml = "0208202504123456789000120010010000000261234567817.xml";
    final var logo = "logo.jpeg";
    final var reportFolder = classLoader.getResource("./report").getPath();
    final var pdfOutFolder = classLoader.getResource(".").getPath().concat("pdf");
    createDirectory(pdfOutFolder);

    final var pathXmlFile = classLoader.getResource(xml).getPath();
    final var pathLogo = classLoader.getResource(logo).getPath();

    CreditNoteReport report =
        new CreditNoteReport(pathXmlFile, reportFolder, pathLogo, pdfOutFolder);
    assertTrue(report.pdf("0208202504123456789000120010010000000261234567817", "1901-01-01"));
  }

  @Test
  void createDebitNotePdf() {
    ClassLoader classLoader = AppTest.class.getClassLoader();

    final var xml = "110820260512345678900110010010000000011234567810.xml";
    final var logo = "logo.jpeg";
    final var reportFolder = classLoader.getResource("./report").getPath();
    final var pdfOutFolder = classLoader.getResource(".").getPath().concat("pdf");
    createDirectory(pdfOutFolder);

    final var pathXmlFile = classLoader.getResource(xml).getPath();
    final var pathLogo = classLoader.getResource(logo).getPath();

    DebitNoteReport report =
            new DebitNoteReport(pathXmlFile, reportFolder, pathLogo, pdfOutFolder);
    assertTrue(report.pdf("110820260512345678900110010010000000011234567810", "1901-01-01"));
  }

  @Test
  void createLiquidationPdf() {
    ClassLoader classLoader = AppTest.class.getClassLoader();

    final var xml = "0308202603999999999900120010020000001451234567818.xml";
    final var logo = "logo.jpeg";
    final var reportFolder = classLoader.getResource("./report").getPath();
    final var pdfOutFolder = classLoader.getResource(".").getPath().concat("pdf");
    createDirectory(pdfOutFolder);

    final var pathXmlFile = classLoader.getResource(xml).getPath();
    final var pathLogo = classLoader.getResource(logo).getPath();

    var report = new LiquidationReport(pathXmlFile, reportFolder, pathLogo, pdfOutFolder);
    assertTrue(report.pdf("0308202603999999999900120010020000001451234567818", "1901-01-01"));
  }

  @Test
  void createWithholdPdf() {
    ClassLoader classLoader = AppTest.class.getClassLoader();

    final var xml = "3007202607999999999900120010020000145281234567816.xml";
    final var logo = "logo.jpeg";
    final var reportFolder = classLoader.getResource("./report").getPath();
    final var pdfOutFolder = classLoader.getResource(".").getPath().concat("pdf");
    createDirectory(pdfOutFolder);

    final var pathXmlFile = classLoader.getResource(xml).getPath();
    final var pathLogo = classLoader.getResource(logo).getPath();

    var report = new WithholdReport(pathXmlFile, reportFolder, pathLogo, pdfOutFolder);
    assertTrue(report.pdf("3007202607999999999900120010020000145281234567816", "1901-01-01"));
  }

  void createDirectory(String path) {
    File directory = new File(path);
    if (!directory.exists()) {
      directory.mkdir();
    }
  }
}
