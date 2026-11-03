package dev.joguenco.pdf.note.debit;

import dev.joguenco.pdf.Information;
import dev.joguenco.serialize.DebitNote;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.note.debit.v100.NotaDebito;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class DebitNoteReport {
    String pathXmlFile;
    String reportFolder;
    String pathLogo;
    String pdfOutFolder;

    public DebitNoteReport(
            String pathXmlFile, String reportFolder, String pathLogo, String pdfOutFolder) {
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

    public Boolean generateReport(
            String urlReporte, DebitNoteTemplate rep, String numAut, String fechaAut) {
        FileInputStream is = null;
        try {
            JRBeanCollectionDataSource jRBeanCollectionDataSource =
                    new JRBeanCollectionDataSource(rep.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);

            JasperPrint reporte_view =
                    JasperFillManager.fillReport(
                            is,
                            obtenerMapaParametrosReportes(
                                    Information.getParameters(
                                            rep.getNotaDebito().getInfoTributaria(),
                                            numAut,
                                            fechaAut,
                                            pathLogo,
                                            reportFolder),
                                    obtenerInfoND(rep.getNotaDebito().getInfoNotaDebito())),
                            (JRDataSource) jRBeanCollectionDataSource);

            ReportUtil.savePdfReport(
                    reporte_view,
                    rep.getNotaDebito().getInfoTributaria().getClaveAcceso(),
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

    private Map<String, Object> obtenerInfoND(NotaDebito.InfoNotaDebito notaDebito) {
        Map<String, Object> param = new HashMap<>();
        param.put("DIR_SUCURSAL", notaDebito.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", notaDebito.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", notaDebito.getObligadoContabilidad().toString());
        param.put("RS_COMPRADOR", notaDebito.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", notaDebito.getIdentificacionComprador());
        param.put("FECHA_EMISION", notaDebito.getFechaEmision());
        param.put("NUM_DOC_MODIFICADO", notaDebito.getNumDocModificado());
        param.put("FECHA_EMISION_DOC_SUSTENTO", notaDebito.getFechaEmisionDocSustento());
        param.put("DOC_MODIFICADO", obtenerDocumentoModificado(notaDebito.getCodDocModificado()));
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
}
