package roquiprinter;

import dev.mestizos.pdf.InvoiceReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void appHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }

    @Test
    void createInvoicePdf() {
        ClassLoader classLoader = AppTest.class.getClassLoader();

        final var xml = "0708202301100245687700110014010000000011234567818.xml";
        final var logo = "logo.jpeg";
        final var reportFolder = classLoader.getResource("./report").getPath();
        final var pdfFolder = classLoader.getResource("./pdf").getPath();

        final var pathXmlFile = classLoader.getResource(xml).getPath();
        final var pathLogo = classLoader.getResource(logo).getPath();

        InvoiceReport report = new InvoiceReport(
                pathXmlFile,
                reportFolder,
                pathLogo,
                pdfFolder
        );
        assertTrue(report.pdf());
    }
}
