package dev.joguenco.pdf.note.delivery;

import dev.joguenco.pdf.AdditionalInformation;
import dev.joguenco.serialize.DeliveryNote;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.note.delivery.v110.GuiaRemision;
import ec.gob.sri.note.delivery.v110.InfoTributaria;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class DeliveryReport {
    String pathXmlFile;
    String reportFolder;
    String pathLogo;
    String pdfOutFolder;

    public DeliveryReport(
            String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
        this.pathXmlFile = pathXmlFile;
        this.reportFolder = reportFolder;
        this.pathLogo = pathLogo;
        this.pdfOutFolder = pdfOutFolder;
    }

    public Boolean pdf(String authorization, String authorizationDate) {
        var delivery = new DeliveryNote(pathXmlFile);

        var fr = new DeliveryTemplate(delivery.xmlToObject());
        return generateReport(fr, authorization, authorizationDate);
    }

    private Boolean generateReport(DeliveryTemplate rep, String numAut, String dateAut) {
        return generateReport(
                this.reportFolder + File.separator + "factura.jasper", rep, numAut, dateAut);
    }

    private Boolean generateReport(
            String urlReport, DeliveryTemplate rep, String numAut, String dateAut) {
        FileInputStream is = null;
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(rep.getGuiaRemisionList());
            is = new FileInputStream(urlReport);

            JasperPrint reporte_view =
                    JasperFillManager.fillReport(
                            is,
                            obtenerMapaParametrosReportes(
                                    getParametersInfoTriobutaria(
                                            rep.getGuiaRemision().getInfoTributaria(),
                                            numAut,
                                            dateAut),
                                    obtenerInfoGR(
                                            rep.getGuiaRemision().getInfoGuiaRemision(),
                                            rep.getGuiaRemision())),
                            dataSource);

            ReportUtil.savePdfReport(
                    reporte_view,
                    rep.getGuiaRemision().getInfoTributaria().getClaveAcceso(),
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

    private Map<String, Object> obtenerInfoGR(
            GuiaRemision.InfoGuiaRemision igr, GuiaRemision guiaRemision) {
        Map param = new HashMap();
        param.put("DIR_SUCURSAL", igr.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", igr.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", igr.getObligadoContabilidad());
        param.put("FECHA_INI_TRANSPORTE", igr.getFechaIniTransporte());
        param.put("FECHA_FIN_TRANSPORTE", igr.getFechaFinTransporte());
        param.put("RUC_TRANSPORTISTA", igr.getRucTransportista());
        param.put("RS_TRANSPORTISTA", igr.getRazonSocialTransportista());
        param.put("PLACA", igr.getPlaca());
        param.put("PUNTO_PARTIDA", igr.getDirPartida());
        param.put("INFO_ADICIONAL", getInfoAdicional(guiaRemision));
        return param;
    }

    public List<AdditionalInformation> getInfoAdicional(GuiaRemision guiaRemision) {
        List infoAdicional = new ArrayList();
        if (guiaRemision.getInfoAdicional() != null) {
            for (GuiaRemision.InfoAdicional.CampoAdicional ca :
                    guiaRemision.getInfoAdicional().getCampoAdicional()) {
                infoAdicional.add(new AdditionalInformation(ca.getValue(), ca.getNombre()));
            }
        }
        if ((infoAdicional != null) && (!infoAdicional.isEmpty())) {
            return infoAdicional;
        }
        return null;
    }
}
