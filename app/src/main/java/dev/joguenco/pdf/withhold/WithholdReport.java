package dev.joguenco.pdf.withhold;

import dev.joguenco.pdf.Information;
import dev.joguenco.serialize.Withhold;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.withhold.v200.ComprobanteRetencion;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

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
                this.reportFolder + File.separator + "comprobanteRetencion.jasper",
                rep,
                numAut,
                dateAut);
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
                            Information.getMapParameters(
                                    Information.getParameters(
                                            rep.getRetencion().getInfoTributaria(),
                                            numAut,
                                            dateAut,
                                            pathLogo,
                                            reportFolder),
                                    getWithholdInformation(
                                            rep.getRetencion().getInfoCompRetencion())),
                            dataSource);
            ReportUtil.savePdfReport(
                    reportView,
                    rep.getRetencion().getInfoTributaria().getClaveAcceso(),
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

    private Map<String, Object> getWithholdInformation(
            ComprobanteRetencion.InfoCompRetencion infoRetencion) {
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
