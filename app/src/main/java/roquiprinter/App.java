package roquiprinter;

import dev.mestizos.Invoice;

public class App {
    public String getGreeting() {
        return "Hello RoquiPrinter !!!";
    }

    void getInvoice(){
        var invoice = new Invoice("/app/quijotelui/comprobante/generado/2023/8/"
                + "0708202301100245687700110014010000000011234567818.xml");
        invoice.xmlToObject();
    }

    public static void main(String[] args) {
        System.out.println(new App().getGreeting());
        new App().getInvoice();
    }
}
