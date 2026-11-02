package dev.joguenco.pdf.invoice;

import dev.joguenco.pdf.Information;
import dev.joguenco.serialize.Invoice;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.invoice.v210.Factura;
import java.io.*;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class InvoiceReport {
    String pathXmlFile;
    String reportFolder;
    String pathLogo;
    String pdfOutFolder;

    public InvoiceReport(
            String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
        this.pathXmlFile = pathXmlFile;
        this.reportFolder = reportFolder;
        this.pathLogo = pathLogo;
        this.pdfOutFolder = pdfOutFolder;
    }

    public Boolean pdf(String authorization, String authorizationDate) {
        var invoice = new Invoice(pathXmlFile);

        InvoiceTemplate fr = new InvoiceTemplate(invoice.xmlToObject());
        return generateReport(fr, authorization, authorizationDate);
    }

    private Boolean generateReport(InvoiceTemplate rep, String numAut, String dateAut) {
        return generateReport(
                this.reportFolder + File.separator + "factura.jasper", rep, numAut, dateAut);
    }

    private Boolean generateReport(
            String urlReport, InvoiceTemplate rep, String numAut, String dateAut) {
        FileInputStream is = null;
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
            is = new FileInputStream(urlReport);
            JasperPrint reportView =
                    JasperFillManager.fillReport(
                            is,
                            Information.getMapParameters(
                                    Information.getParameters(
                                            rep.getFactura().getInfoTributaria(),
                                            numAut,
                                            dateAut,
                                            pathLogo,
                                            reportFolder),
                                    getInvoiceInformation(rep.getFactura().getInfoFactura())),
                            dataSource);
            ReportUtil.savePdfReport(
                    reportView,
                    rep.getFactura().getInfoTributaria().getClaveAcceso(),
                    pdfOutFolder);
        } catch (FileNotFoundException | JRException ex) {
            System.out.println(ex.getMessage());
            return false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                System.out.println("Error");
            }
        }
        return true;
    }

    private Map<String, Object> getInvoiceInformation(Factura.InfoFactura infoFactura) {
        Map<String, Object> param = new HashMap<>();
        param.put("DIR_SUCURSAL", infoFactura.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", infoFactura.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoFactura.getObligadoContabilidad().toString());
        param.put("RS_COMPRADOR", infoFactura.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", infoFactura.getIdentificacionComprador());
        param.put("DIRECCION_CLIENTE", infoFactura.getDireccionComprador());
        param.put("FECHA_EMISION", infoFactura.getFechaEmision());
        param.put("GUIA", infoFactura.getGuiaRemision());

        return param;
    }
}
