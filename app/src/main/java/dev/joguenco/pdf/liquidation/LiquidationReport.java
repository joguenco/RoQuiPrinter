package dev.joguenco.pdf.liquidation;

import dev.joguenco.pdf.Information;
import dev.joguenco.serialize.Liquidation;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.liquidation.v110.LiquidacionCompra;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

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
                                    Information.getParameters(
                                            rep.getLiquidacion().getInfoTributaria(),
                                            numAut,
                                            dateAut,
                                            pathLogo,
                                            reportFolder),
                                    getInfoLiquidation(
                                            rep.getLiquidacion().getInfoLiquidacionCompra())),
                            dataSource);
            ReportUtil.savePdfReport(
                    reportView,
                    rep.getLiquidacion().getInfoTributaria().getClaveAcceso(),
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

    private Map<String, Object> obtenerMapaParametrosReportes(
            Map<String, Object> mapa1, Map<String, Object> mapa2) {
        mapa1.putAll(mapa2);
        return mapa1;
    }

    private Map<String, Object> getInfoLiquidation(
            LiquidacionCompra.InfoLiquidacionCompra infoLiquidacion) {
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
