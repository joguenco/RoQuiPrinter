package dev.joguenco.pdf;

import dev.joguenco.serialize.CreditNote;
import ec.gob.sri.note.credit.v110.InfoTributaria;
import ec.gob.sri.note.credit.v110.NotaCredito;
import ec.gob.sri.note.credit.v110.TotalConImpuestos;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class CreditNoteReport {
  String pathXmlFile;
  String reportFolder;
  String pathLogo;
  String pdfOutFolder;

  public CreditNoteReport(
      String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
    this.pathXmlFile = pathXmlFile;
    this.reportFolder = reportFolder;
    this.pathLogo = pathLogo;
    this.pdfOutFolder = pdfOutFolder;
  }

  public Boolean pdf(String authorization, String authorizationDate) {
    var invoice = new CreditNote(pathXmlFile);

    CreditNoteTemplate cn = new CreditNoteTemplate(invoice.xmlToObject());
    return generateReport(cn, authorization, authorizationDate);
  }

  private Boolean generateReport(CreditNoteTemplate rep, String numAut, String dateAut) {
    return generateReport(
        this.reportFolder + File.separator + "notaCredito.jasper", rep, numAut, dateAut);
  }

  public Boolean generateReport(
      String urlReporte, CreditNoteTemplate rep, String numAut, String fechaAut) {
    FileInputStream is = null;
    try {
      JRBeanCollectionDataSource jRBeanCollectionDataSource =
          new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
      is = new FileInputStream(urlReporte);
      JasperPrint reporte_view =
          JasperFillManager.fillReport(
              is,
              obtenerMapaParametrosReportes(
                  getParametersInfoTriobutaria(
                      rep.getNotaCredito().getInfoTributaria(), numAut, fechaAut),
                  getInfoCN(rep.getNotaCredito().getInfoNotaCredito(), rep)),
              (JRDataSource) jRBeanCollectionDataSource);
      savePdfReport(reporte_view, rep.getNotaCredito().getInfoTributaria().getClaveAcceso());
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

  private Map<String, Object> obtenerMapaParametrosReportes(
      Map<String, Object> mapa1, Map<String, Object> mapa2) {
    mapa1.putAll(mapa2);
    return mapa1;
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

  private Map<String, Object> getInfoCN(NotaCredito.InfoNotaCredito infoCN, CreditNoteTemplate nc) {
    Map<String, Object> param = new HashMap<>();
    param.put("DIR_SUCURSAL", infoCN.getDirEstablecimiento());
    param.put("CONT_ESPECIAL", infoCN.getContribuyenteEspecial());
    param.put("LLEVA_CONTABILIDAD", infoCN.getObligadoContabilidad().toString());
    param.put("RS_COMPRADOR", infoCN.getRazonSocialComprador());
    param.put("RUC_COMPRADOR", infoCN.getIdentificacionComprador());
    param.put("FECHA_EMISION", infoCN.getFechaEmision());
    param.put("NUM_DOC_MODIFICADO", infoCN.getNumDocModificado());
    param.put("FECHA_EMISION_DOC_SUSTENTO", infoCN.getFechaEmisionDocSustento());
    param.put("DOC_MODIFICADO", obtenerDocumentoModificado(infoCN.getCodDocModificado()));
    param.put("RAZON_MODIF", infoCN.getMotivo());
    String porcentajeIva =
        ObtieneIvaRideNC(
            nc.getNotaCredito().getInfoNotaCredito().getTotalConImpuestos(),
            DeStringADate(infoCN.getFechaEmisionDocSustento()));
    param.put("PORCENTAJE_IVA", porcentajeIva);
    return param;
  }

  public static String obtenerDocumentoModificado(String codDoc) {
    if ("01".equals(codDoc)) return "FACTURA";
    if ("04".equals(codDoc)) return "NOTA DE CRÉDITO";
    if ("05".equals(codDoc)) return "NOTA DE DÉBITO";
    if ("06".equals(codDoc)) return "GUÍA DE REMISIÓN";
    if ("07".equals(codDoc)) return "COMPROBANTE DE RETENCIÓN";
    return null;
  }

  private String ObtieneIvaRideNC(TotalConImpuestos impuestos, Date fecha) {
    for (TotalConImpuestos.TotalImpuesto impuesto : impuestos.getTotalImpuesto()) {
      Integer cod = Integer.parseInt(impuesto.getCodigo());
      if (TypeTaxEnum.IVA.getCode() == cod.intValue() && impuesto.getValor().doubleValue() > 0.0D)
        return (impuesto.getCodigoPorcentaje());
    }
    return "IVA";
  }

  public Date DeStringADate(String dateString) {
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    try {
      return format.parse(dateString);
    } catch (ParseException ex) {
      return null;
    }
  }
}
