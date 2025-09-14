package dev.joguenco.pdf;

import dev.joguenco.serialize.Invoice;
import ec.gob.sri.invoice.v210.Factura;
import ec.gob.sri.invoice.v210.InfoTributaria;
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
    generateReport(fr, authorization, authorizationDate);
    return true;
  }

  private void generateReport(InvoiceTemplate xml, String numAut, String dateAut) {
    generateReport(this.reportFolder + File.separator + "factura.jasper", xml, numAut, dateAut);
  }

  private void generateReport(
      String urlReport, InvoiceTemplate fact, String numAut, String dateAut) {
    FileInputStream is = null;
    try {
      JRDataSource dataSource = new JRBeanCollectionDataSource(fact.getDetallesAdiciones());
      is = new FileInputStream(urlReport);
      JasperPrint reportView =
          JasperFillManager.fillReport(
              is,
              obtenerMapaParametrosReportes(
                  getParametersInfoTriobutaria(
                      fact.getFactura().getInfoTributaria(), numAut, dateAut),
                  getInfoFactura(fact.getFactura().getInfoFactura())),
              dataSource);
      savePdfReport(reportView, fact.getFactura().getInfoTributaria().getClaveAcceso());
    } catch (FileNotFoundException | JRException ex) {
      System.out.println(ex.getMessage());
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException ex) {
        System.out.println("Error");
      }
    }
  }

  private void savePdfReport(JasperPrint jp, String pdfName) {
    try {
      OutputStream output =
          new FileOutputStream(new File(this.pdfOutFolder + File.separatorChar + pdfName + ".pdf"));
      JasperExportManager.exportReportToPdfStream(jp, output);
      output.close();
      System.out.println(
          "PDF: Saved in " + this.pdfOutFolder + File.separatorChar + pdfName + ".pdf");
    } catch (JRException | IOException ex) {
      System.out.println("Error " + ex.getMessage());
    }
  }

  public Map<String, Object> getParametersInfoTriobutaria(
      InfoTributaria infoTributaria, String numAut, String fechaAut) {
    Map param = new HashMap();
    param.put("RUC", infoTributaria.getRuc());
    param.put("CLAVE_ACC", infoTributaria.getClaveAcceso());
    param.put("RAZON_SOCIAL", infoTributaria.getRazonSocial());
    param.put("DIR_MATRIZ", infoTributaria.getDirMatriz());
    param.put("AGENTE_RETENCION", infoTributaria.getAgenteRetencion());
    param.put("REGIMEN_RIMPE", infoTributaria.getContribuyenteRimpe());
    try {
      param.put("LOGO", new FileInputStream(pathLogo));
    } catch (FileNotFoundException ex) {
      System.out.println("Error " + ex.getMessage());
    }
    //        param.put("SUBREPORT_DIR", "./resources/reportes/");

    param.put("SUBREPORT_DIR", reportFolder + File.separator);
    param.put("SUBREPORT_PAGOS", reportFolder + File.separator);
    param.put("SUBREPORT_TOTALES", reportFolder + File.separator);
    if (infoTributaria.getTipoEmision().equals("1")) {
      param.put("TIPO_EMISION", "Normal");
    } else {
      param.put("TIPO_EMISION", "Indisponibilidad del Sistema");
    }
    param.put("NUM_AUT", numAut);
    param.put("FECHA_AUT", fechaAut);
    param.put("MARCA_AGUA", "");
    param.put(
        "NUM_FACT",
        infoTributaria.getEstab()
            + "-"
            + infoTributaria.getPtoEmi()
            + "-"
            + infoTributaria.getSecuencial());
    if (infoTributaria.getAmbiente().equals("1")) {
      param.put("AMBIENTE", "Pruebas");
    } else {
      param.put("AMBIENTE", "Producción");
    }
    param.put("NOM_COMERCIAL", infoTributaria.getNombreComercial());
    return param;
  }

  private Map<String, Object> obtenerMapaParametrosReportes(
      Map<String, Object> mapa1, Map<String, Object> mapa2) {
    mapa1.putAll(mapa2);
    return mapa1;
  }

  private Map<String, Object> getInfoFactura(Factura.InfoFactura infoFactura) {
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
