package dev.joguenco.pdf.liquidation;

import dev.joguenco.serialize.Liquidation;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.liquidation.v110.LiquidacionCompra;
import ec.gob.sri.liquidation.v110.InfoTributaria;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LiquidationReport {
  String pathXmlFile;
  String reportFolder;
  String pathLogo;
  String pdfOutFolder;

  public LiquidationReport(
      String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
    this.pathXmlFile = pathXmlFile;
    this.reportFolder = reportFolder;
    this.pathLogo = pathLogo;
    this.pdfOutFolder = pdfOutFolder;
  }

  public Boolean pdf(String authorization, String authorizationDate) {
    var liquidation = new Liquidation(pathXmlFile);

    LiquidationTemplate fr = new LiquidationTemplate(liquidation.xmlToObject());
    return generateReport(fr, authorization, authorizationDate);
  }

  private Boolean generateReport(LiquidationTemplate rep, String numAut, String dateAut) {
    return generateReport(
        this.reportFolder + File.separator + "liquidacion.jasper", rep, numAut, dateAut);
  }

  private Boolean generateReport(
      String urlReport, LiquidationTemplate rep, String numAut, String dateAut) {
    FileInputStream is = null;
    try {
      JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
      is = new FileInputStream(urlReport);
      JasperPrint reportView =
          JasperFillManager.fillReport(
              is,
              obtenerMapaParametrosReportes(
                  getParametersInfoTriobutaria(
                      rep.getLiquidacion().getInfoTributaria(), numAut, dateAut),
                  getInfoLiquidation(rep.getLiquidacion().getInfoLiquidacionCompra())),
              dataSource);
      ReportUtil.savePdfReport(reportView, rep.getLiquidacion().getInfoTributaria().getClaveAcceso(), pdfOutFolder);
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

  private Map<String, Object> getInfoLiquidation(LiquidacionCompra.InfoLiquidacionCompra infoLiquidacion) {
    Map<String, Object> param = new HashMap<>();
    param.put("DIR_SUCURSAL", infoLiquidacion.getDirEstablecimiento());
    param.put("CONT_ESPECIAL", infoLiquidacion.getContribuyenteEspecial());
    param.put("LLEVA_CONTABILIDAD", infoLiquidacion.getObligadoContabilidad().toString());
    param.put("RS_PROVEEDOR", infoLiquidacion.getRazonSocialProveedor());
    param.put("RUC_PROVEEDOR", infoLiquidacion.getIdentificacionProveedor());
    param.put("DIRECCION_PROVEEDOR", infoLiquidacion.getDireccionProveedor());
    param.put("FECHA_EMISION", infoLiquidacion.getFechaEmision());

    return param;
  }
}
