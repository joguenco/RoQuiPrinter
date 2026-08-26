package dev.joguenco.util;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ReportUtil {

    public static void savePdfReport(JasperPrint jp, String pdfName, String pdfOutFolder) {
        try {
            OutputStream output =
                    new FileOutputStream(new File(pdfOutFolder + File.separatorChar + pdfName + ".pdf"));
            JasperExportManager.exportReportToPdfStream(jp, output);
            output.close();
            System.out.println("PDF: Saved in " + pdfOutFolder + File.separatorChar + pdfName + ".pdf");
        } catch (JRException | IOException ex) {
            System.out.println("Error " + ex.getMessage());
        }
    }

    public static String getNameOfDocument(String codDoc) {
        if ("01".equals(codDoc)) return "FACTURA";
        if ("04".equals(codDoc)) return "NOTA DE CRÉDITO";
        if ("05".equals(codDoc)) return "NOTA DE DÉBITO";
        if ("06".equals(codDoc)) return "GUÍA DE REMISIÓN";
        if ("07".equals(codDoc)) return "COMPROBANTE DE RETENCIÓN";
        return null;
    }
}
