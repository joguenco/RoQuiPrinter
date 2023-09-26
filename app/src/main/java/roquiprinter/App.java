package roquiprinter;

import dev.mestizos.pdf.InvoiceReport;
import dev.mestizos.serialize.Invoice;

public class App {
    public String getGreeting() {
        return "Hello RoquiPrinter !!!";
    }

    void pdf(){
        InvoiceReport report;
        report = new InvoiceReport(
                "/home/jorgeluis/JaspersoftWorkspace/RoquiPrinter/sri",
                "/home/jorgeluis/Projects/QuijoteLuiPrinter/recursos/imagenes/logo.jpeg",
                "/app/quijotelui/comprobante/pdf");

        report.pdf();
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        new App().pdf();
    }
}
