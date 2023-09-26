package dev.mestizos.pdf;

import dev.mestizos.serialize.Invoice;
import ec.gob.sri.invoice.v210.Factura;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class InvoiceReport {
    String directorioReportes;
    String directorioLogo;
    String directorioDestino;

    public InvoiceReport(String directorioReportes, String directorioLogo, String directorioDestino) {
        this.directorioReportes = directorioReportes;
        this.directorioLogo = directorioLogo;
        this.directorioDestino = directorioDestino;
    }

    public void pdf() {
        var invoice = new Invoice("/app/quijotelui/comprobante/generado/2023/8/"
                + "0708202301100245687700110014010000000011234567818.xml");


        FacturaReporte fr = new FacturaReporte(invoice.xmlToObject());
        generarReporte(fr, "numeroAutorizacion", "fechaAutorizacion");
    }

    private void generarReporte(FacturaReporte xml, String numAut, String fechaAut) {

        generarReporte(this.directorioReportes + File.separator + "factura.jasper", xml, numAut, fechaAut);

    }

    private void generarReporte(String urlReporte, FacturaReporte fact, String numAut, String fechaAut) {
        Parametros p = new Parametros(this.directorioReportes, this.directorioLogo);
        FileInputStream is = null;
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(fact.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporte_view = JasperFillManager.fillReport(is,
                    obtenerMapaParametrosReportes(
                            p.obtenerParametrosInfoTriobutaria(fact.getFactura().getInfoTributaria(),
                                    numAut,
                                    fechaAut),
                            obtenerInfoFactura(fact.getFactura().getInfoFactura(),
                                    fact)),
                    dataSource);
            savePdfReport(reporte_view, fact.getFactura().getInfoTributaria().getClaveAcceso());
        } catch (FileNotFoundException | JRException ex) {
            System.out.println(ex.toString());
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

    private void savePdfReport(JasperPrint jp, String nombrePDF) {
        try {
            OutputStream output = new FileOutputStream(new File(this.directorioDestino + File.separatorChar + nombrePDF + ".pdf"));
            JasperExportManager.exportReportToPdfStream(jp, output);
            output.close();
            System.out.println("PDF: Guardado en " + this.directorioDestino + File.separatorChar + nombrePDF + ".pdf");
        } catch (JRException | FileNotFoundException ex) {
            System.out.println("Error");
        } catch (IOException ex) {
            System.out.println("Error");
        }
    }

    private Map<String, Object> obtenerMapaParametrosReportes(Map<String, Object> mapa1, Map<String, Object> mapa2) {
        mapa1.putAll(mapa2);
        return mapa1;
    }

    private Map<String, Object> obtenerInfoFactura(Factura.InfoFactura infoFactura, FacturaReporte fact) {
        Map param = new HashMap();
        param.put("DIR_SUCURSAL", infoFactura.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", infoFactura.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoFactura.getObligadoContabilidad().toString());
        param.put("RS_COMPRADOR", infoFactura.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", infoFactura.getIdentificacionComprador());
        param.put("FECHA_EMISION", infoFactura.getFechaEmision());
        param.put("GUIA", infoFactura.getGuiaRemision());
        TotalComprobante tc = getTotales(infoFactura);
        param.put("VALOR_TOTAL", infoFactura.getImporteTotal());
        param.put("DESCUENTO", infoFactura.getTotalDescuento());
        param.put("IVA_VALOR", tc.getIvaPorcentaje());
        param.put("IVA", tc.getIva12());
        param.put("IVA_0", tc.getSubtotal0());
        param.put("IVA_12", tc.getSubtotal12());
        param.put("ICE", tc.getTotalIce());
        param.put("IRBPNR", tc.getTotalIRBPNR());
        param.put("EXENTO_IVA", tc.getSubtotalExentoIVA());
        param.put("NO_OBJETO_IVA", tc.getSubtotalNoSujetoIva());
        param.put("SUBTOTAL", infoFactura.getTotalSinImpuestos().toString());
        if (infoFactura.getPropina() != null) {
            param.put("PROPINA", infoFactura.getPropina().toString());
        }
        param.put("TOTAL_DESCUENTO", calcularDescuento(fact));
        return param;
    }

    private String calcularDescuento(FacturaReporte fact) {
        BigDecimal descuento = new BigDecimal(0);
        for (DetallesAdicionalesReporte detalle : fact.getDetallesAdiciones()) {
            descuento = descuento.add(new BigDecimal(detalle.getDescuento()));
        }
        return descuento.toString();
    }

    private TotalComprobante getTotales(Factura.InfoFactura infoFactura) {
        BigDecimal totalIvaDiferenteDe0 = new BigDecimal(0.0D);
        BigDecimal totalIva0 = new BigDecimal(0.0D);
        BigDecimal iva12 = new BigDecimal(0.0D);
        BigDecimal totalICE = new BigDecimal(0.0D);
        BigDecimal totalExentoIVA = new BigDecimal(0.0D);
        BigDecimal totalIRBPNR = new BigDecimal(0.0D);
        BigDecimal totalSinImpuesto = new BigDecimal(0.0D);
        String ivaPorcentaje = "";
        TotalComprobante tc = new TotalComprobante();
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto ti : infoFactura.getTotalConImpuestos().getTotalImpuesto()) {
            Integer cod = Integer.valueOf(ti.getCodigo());

            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) &&
                    (
                            (TipoImpuestoIvaEnum.IVA_VENTA_12.getCode().equals(ti.getCodigoPorcentaje()))
                                    || (TipoImpuestoIvaEnum.IVA_VENTA_14.getCode().equals(ti.getCodigoPorcentaje()))
                                    || (TipoImpuestoIvaEnum.IVA_DIFERENCIADO.getCode().equals(ti.getCodigoPorcentaje()))
                    )) {
                totalIvaDiferenteDe0 = totalIvaDiferenteDe0.add(ti.getBaseImponible());
                iva12 = iva12.add(ti.getValor());

                BigDecimal value = ti.getTarifa() == null ? new BigDecimal("-1") : ti.getTarifa();
                ivaPorcentaje = value.toBigInteger().toString();
            }
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (TipoImpuestoIvaEnum.IVA_VENTA_0.getCode().equals(ti.getCodigoPorcentaje()))) {
                totalIva0 = totalIva0.add(ti.getBaseImponible());
            }
            if ((TipoImpuestoEnum.IVA.getCode() == cod.intValue()) && (TipoImpuestoIvaEnum.IVA_EXCENTO.getCode().equals(ti.getCodigoPorcentaje()))) {
                totalExentoIVA = totalExentoIVA.add(ti.getBaseImponible());
            }
            if (TipoImpuestoEnum.ICE.getCode() == cod.intValue()) {
                totalICE = totalICE.add(ti.getValor());
            }
            if (TipoImpuestoEnum.IRBPNR.getCode() == cod.intValue()) {
                totalIRBPNR = totalIRBPNR.add(ti.getValor());
            }
        }
        tc.setIvaPorcentaje(ivaPorcentaje);
        tc.setIva12(iva12.toString());
        tc.setSubtotal0(totalIva0.toString());
        tc.setSubtotal12(totalIvaDiferenteDe0.toString());
        tc.setTotalIce(totalICE.toString());
        tc.setTotalIRBPNR(totalIRBPNR.toString());
        tc.setSubtotalExentoIVA(totalExentoIVA.toString());
        tc.setSubtotal(totalIva0.add(totalIvaDiferenteDe0));
        tc.setSubtotalNoSujetoIva(totalSinImpuesto.toString());
        return tc;
    }
}
