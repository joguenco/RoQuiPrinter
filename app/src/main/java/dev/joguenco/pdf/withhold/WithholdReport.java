package dev.joguenco.pdf.withhold;

import dev.joguenco.serialize.Withhold;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.withhold.v200.ComprobanteRetencion;
import ec.gob.sri.withhold.v200.InfoTributaria;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WithholdReport {
  String pathXmlFile;
  String reportFolder;
  String pathLogo;
  String pdfOutFolder;

  public WithholdReport(
      String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
    this.pathXmlFile = pathXmlFile;
    this.reportFolder = reportFolder;
    this.pathLogo = pathLogo;
    this.pdfOutFolder = pdfOutFolder;
  }

  public Boolean pdf(String authorization, String authorizationDate) {
    var withhold = new Withhold(pathXmlFile);

    var fr = new WithholdTemplate(withhold.xmlToObject());
    return generateReport(fr, authorization, authorizationDate);
  }

  private Boolean generateReport(WithholdTemplate rep, String numAut, String dateAut) {
    return generateReport(
        this.reportFolder + File.separator + "comprobanteRetencion.jasper", rep, numAut, dateAut);
  }

  private Boolean generateReport(
      String urlReport, WithholdTemplate rep, String numAut, String dateAut) {
    FileInputStream is = null;
    try {
      JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
      is = new FileInputStream(urlReport);
      JasperPrint reportView =
          JasperFillManager.fillReport(
              is,
              obtenerMapaParametrosReportes(
                  getParametersInfoTriobutaria(
                      rep.getRetencion().getInfoTributaria(), numAut, dateAut),
                  getInfoRetencion(rep.getRetencion().getInfoCompRetencion())),
              dataSource);
      ReportUtil.savePdfReport(reportView, rep.getRetencion().getInfoTributaria().getClaveAcceso(), pdfOutFolder);
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
    param.put("NUM_FACT",
        infoTributaria.getEstab() + "-" + infoTributaria.getPtoEmi() + "-" + infoTributaria.getSecuencial());
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

  private Map<String, Object> getInfoRetencion(ComprobanteRetencion.InfoCompRetencion infoRetencion) {
    Map<String, Object> param = new HashMap<>();
    param.put("DIR_SUCURSAL", infoRetencion.getDirEstablecimiento());
    param.put("CONT_ESPECIAL", infoRetencion.getContribuyenteEspecial());
    param.put("LLEVA_CONTABILIDAD", infoRetencion.getObligadoContabilidad().toString());
    param.put("RS_COMPRADOR", infoRetencion.getRazonSocialSujetoRetenido());
    param.put("RUC_COMPRADOR", infoRetencion.getIdentificacionSujetoRetenido());
    param.put("FECHA_EMISION", infoRetencion.getFechaEmision());
    param.put("EJERCICIO_FISCAL", infoRetencion.getPeriodoFiscal());

    return param;
  }
}
