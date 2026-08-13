package dev.joguenco.pdf.note.debit;

import dev.joguenco.serialize.DebitNote;
import ec.gob.sri.note.debit.v100.InfoTributaria;
import ec.gob.sri.note.debit.v100.NotaDebito;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DebitNoteReport {
    String pathXmlFile;
    String reportFolder;
    String pathLogo;
    String pdfOutFolder;

    public DebitNoteReport(String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
        this.pathXmlFile = pathXmlFile;
        this.reportFolder = reportFolder;
        this.pathLogo = pathLogo;
        this.pdfOutFolder = pdfOutFolder;
    }

    public Boolean pdf(String authorization, String authorizationDate) {
        var debitNote = new DebitNote(pathXmlFile);

        DebitNoteTemplate cn = new DebitNoteTemplate(debitNote.xmlToObject());
        return generateReport(cn, authorization, authorizationDate);
    }

    private Boolean generateReport(DebitNoteTemplate rep, String numAut, String dateAut) {
        return generateReport(
                this.reportFolder + File.separator + "notaDebito.jasper", rep, numAut, dateAut);
    }

    public Boolean generateReport(String urlReporte, DebitNoteTemplate rep, String numAut, String fechaAut) {
        FileInputStream is = null;
        try {
            JRBeanCollectionDataSource jRBeanCollectionDataSource =
                    new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);

            JasperPrint reporte_view = JasperFillManager.fillReport(is,
                    obtenerMapaParametrosReportes(getParametersInfoTriobutaria(rep.getNotaDebito().getInfoTributaria(), numAut, fechaAut), obtenerInfoND(rep.getNotaDebito().getInfoNotaDebito())), (JRDataSource)jRBeanCollectionDataSource);

            savePdfReport(reporte_view, rep.getNotaDebito().getInfoTributaria().getClaveAcceso());
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

    private Map<String, Object> obtenerInfoND(NotaDebito.InfoNotaDebito notaDebito) {
        Map<String, Object> param = new HashMap<>();
        param.put("DIR_SUCURSAL", notaDebito.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", notaDebito.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", notaDebito.getObligadoContabilidad());
        param.put("RS_COMPRADOR", notaDebito.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", notaDebito.getIdentificacionComprador());
        param.put("FECHA_EMISION", notaDebito.getFechaEmision());
        param.put("NUM_DOC_MODIFICADO", notaDebito.getNumDocModificado());
        param.put("FECHA_EMISION_DOC_SUSTENTO", notaDebito.getFechaEmisionDocSustento());
        param.put("DOC_MODIFICADO", obtenerDocumentoModificado(notaDebito.getCodDocModificado()));
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

    public static String obtenerDocumentoModificado(String codDoc) {
        if ("01".equals(codDoc)) return "FACTURA";
        if ("04".equals(codDoc)) return "NOTA DE CRÉDITO";
        if ("05".equals(codDoc)) return "NOTA DE DÉBITO";
        if ("06".equals(codDoc)) return "GUÍA DE REMISIÓN";
        if ("07".equals(codDoc)) return "COMPROBANTE DE RETENCIÓN";
        return null;
    }
}
