package roquiprinter;

import dev.mestizos.pdf.InvoiceReport;
import dev.mestizos.serialize.Invoice;

public class App {
    public String getGreeting() {
        return "Hello RoquiPrinter !!!";
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
    }
}
