package dev.mestizos.pdf;

import dev.mestizos.serialize.Invoice;
import ec.gob.sri.invoice.v210.Factura;
import ec.gob.sri.invoice.v210.Impuesto;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceReport {
    String rutaArchivo;
    String directorioReportes;
    String directorioLogo;
    String directorioDestino;

    public InvoiceReport(String rutaArchivo, String directorioReportes, String directorioLogo, String directorioDestino) {
        this.rutaArchivo = rutaArchivo;
        this.directorioReportes = directorioReportes;
        this.directorioLogo = directorioLogo;
        this.directorioDestino = directorioDestino;
    }

    public Boolean pdf() {
        var invoice = new Invoice(rutaArchivo);

        FacturaReporte fr = new FacturaReporte(invoice.xmlToObject());
        generarReporte(fr, "numeroAutorizacion", "fechaAutorizacion");
        return true;
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
        BigDecimal TotalSinSubsidio = BigDecimal.ZERO;
        BigDecimal TotalSinDescuento = BigDecimal.ZERO;
        BigDecimal TotalSubsidio = BigDecimal.ZERO;
        Map<String, Object> param = new HashMap<>();
        param.put("DIR_SUCURSAL", infoFactura.getDirEstablecimiento());
        param.put("CONT_ESPECIAL", infoFactura.getContribuyenteEspecial());
        param.put("LLEVA_CONTABILIDAD", infoFactura.getObligadoContabilidad().toString());
        param.put("RS_COMPRADOR", infoFactura.getRazonSocialComprador());
        param.put("RUC_COMPRADOR", infoFactura.getIdentificacionComprador());
        param.put("DIRECCION_CLIENTE", infoFactura.getDireccionComprador());
        param.put("FECHA_EMISION", infoFactura.getFechaEmision());
        param.put("GUIA", infoFactura.getGuiaRemision());
        TotalComprobante tc = getTotales(infoFactura);
        if (infoFactura.getTotalSubsidio() != null) {
            TotalSinSubsidio = obtenerTotalSinSubsidio(fact);
            TotalSinDescuento = obtenerTotalSinDescuento(fact);
            TotalSubsidio = TotalSinSubsidio.subtract(TotalSinDescuento).setScale(2, RoundingMode.UP);
            if (Double.valueOf(tc.getTotalIRBPNR().toString()).doubleValue() < 0.0D) {
                TotalSinSubsidio = TotalSinSubsidio.add(tc.getTotalIRBPNR());
            }
            if (infoFactura.getPropina() != null) {
                TotalSinSubsidio = TotalSinSubsidio.add(infoFactura.getPropina());
            }
        }
        param.put("TOTAL_SIN_SUBSIDIO", TotalSinSubsidio.setScale(2, RoundingMode.UP));
        param.put("AHORRO_POR_SUBSIDIO", TotalSubsidio.setScale(2, RoundingMode.UP));


        return param;
    }

    private BigDecimal obtenerTotalSinSubsidio(FacturaReporte fact) {
        BigDecimal totalSinSubsidio = BigDecimal.ZERO;
        BigDecimal ivaDistintoCero = BigDecimal.ZERO;
        BigDecimal iva0 = BigDecimal.ZERO;
        double iva = 0.0D;
        List<Factura.Detalles.Detalle> detalles = fact.getFactura().getDetalles().getDetalle();
        for (int i = 0; i < detalles.size(); i++) {
            BigDecimal sinSubsidio = BigDecimal.ZERO.setScale(2, RoundingMode.UP);
            if (((Factura.Detalles.Detalle) detalles.get(i)).getPrecioSinSubsidio() != null) {
                sinSubsidio = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getPrecioSinSubsidio().toString()).doubleValue());
            }
            BigDecimal cantidad = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getCantidad().toString()).doubleValue());
            if (Double.valueOf(sinSubsidio.toString()).doubleValue() <= 0.0D) {
                sinSubsidio = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getPrecioUnitario().toString()).doubleValue());
            }
            List<Impuesto> impuesto = ((Factura.Detalles.Detalle) detalles.get(i)).getImpuestos().getImpuesto();
            double iva1 = 0.0D;
            for (int c = 0; c < impuesto.size(); c++) {
                if (((Impuesto) impuesto.get(c)).getCodigo().equals(String.valueOf(TipoImpuestoEnum.IVA.getCode())) && !((Impuesto) impuesto.get(c)).getTarifa().equals(BigDecimal.ZERO)) {
                    iva = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                    iva1 = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                }
            }
            if (iva1 > 0.0D) {
                ivaDistintoCero = ivaDistintoCero.add(sinSubsidio.multiply(cantidad));
            } else {

                iva0 = iva0.add(sinSubsidio.multiply(cantidad));
            }
        }
        if (iva > 0.0D) {
            iva = iva / 100.0D + 1.0D;
            ivaDistintoCero = ivaDistintoCero.multiply(BigDecimal.valueOf(iva));
        }
        totalSinSubsidio = totalSinSubsidio.add(ivaDistintoCero).add(iva0);
        return totalSinSubsidio.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerTotalSinDescuento(FacturaReporte fact) {
        BigDecimal totalConSubsidio = BigDecimal.ZERO;
        BigDecimal ivaDistintoCero = BigDecimal.ZERO;
        BigDecimal iva0 = BigDecimal.ZERO;
        double iva = 0.0D;
        List<Factura.Detalles.Detalle> detalles = fact.getFactura().getDetalles().getDetalle();
        for (int i = 0; i < detalles.size(); i++) {
            BigDecimal sinSubsidio = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getPrecioUnitario().toString()).doubleValue());
            BigDecimal cantidad = BigDecimal.valueOf(Double.valueOf(((Factura.Detalles.Detalle) detalles.get(i)).getCantidad().toString()).doubleValue());
            List<Impuesto> impuesto = ((Factura.Detalles.Detalle) detalles.get(i)).getImpuestos().getImpuesto();
            double iva1 = 0.0D;
            for (int c = 0; c < impuesto.size(); c++) {
                if (((Impuesto) impuesto.get(c)).getCodigo().equals(String.valueOf(TipoImpuestoEnum.IVA.getCode())) && !((Impuesto) impuesto.get(c)).getTarifa().equals(BigDecimal.ZERO)) {
                    iva = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                    iva1 = Double.valueOf(((Impuesto) impuesto.get(c)).getTarifa().toString()).doubleValue();
                }
            }
            if (iva1 > 0.0D) {
                ivaDistintoCero = ivaDistintoCero.add(sinSubsidio.multiply(cantidad));
            } else {

                iva0 = iva0.add(sinSubsidio.multiply(cantidad));
            }
        }
        if (iva > 0.0D) {
            iva = iva / 100.0D + 1.0D;
            ivaDistintoCero = ivaDistintoCero.multiply(BigDecimal.valueOf(iva));
        }
        totalConSubsidio = totalConSubsidio.add(ivaDistintoCero).add(iva0);
        return totalConSubsidio.setScale(2, RoundingMode.HALF_UP);
    }

    private TotalComprobante getTotales(Factura.InfoFactura infoFactura) {
        List<IvaDiferenteCeroReporte> ivaDiferenteCero = new ArrayList<>();

        BigDecimal totalIva = new BigDecimal(0.0D);
        BigDecimal totalIva0 = new BigDecimal(0.0D);
        BigDecimal totalExentoIVA = new BigDecimal(0.0D);
        BigDecimal totalICE = new BigDecimal(0.0D);
        BigDecimal totalIRBPNR = new BigDecimal(0.0D);
        BigDecimal totalSinImpuesto = new BigDecimal(0.0D);
        TotalComprobante tc = new TotalComprobante();
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto ti : infoFactura.getTotalConImpuestos().getTotalImpuesto()) {
            Integer cod = Integer.valueOf(ti.getCodigo());

            if (TipoImpuestoEnum.IVA.getCode() == cod.intValue() && ti.getValor().doubleValue() > 0.0D) {
                if (ti.getCodigoPorcentaje().equals(TipoImpuestoIvaEnum.IVA_DIFERENCIADO.getCode())) {
                    String codigoPorcentaje = "TARIFA ESPECIAL " + ti.getCodigoPorcentaje();
                    IvaDiferenteCeroReporte iva = new IvaDiferenteCeroReporte(ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
                    ivaDiferenteCero.add(iva);
                } else {
                    String codigoPorcentaje = obtenerPorcentajeIvaVigente(ti.getCodigoPorcentaje());
                    IvaDiferenteCeroReporte iva = new IvaDiferenteCeroReporte(ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
            }

            if (TipoImpuestoEnum.IVA.getCode() == cod.intValue() && TipoImpuestoIvaEnum.IVA_VENTA_0.getCode().equals(ti.getCodigoPorcentaje())) {
                totalIva0 = totalIva0.add(ti.getBaseImponible());
            }
            if (TipoImpuestoEnum.IVA.getCode() == cod.intValue() && TipoImpuestoIvaEnum.IVA_NO_OBJETO.getCode().equals(ti.getCodigoPorcentaje())) {
                totalSinImpuesto = totalSinImpuesto.add(ti.getBaseImponible());
            }
            if (TipoImpuestoEnum.IVA.getCode() == cod.intValue() && TipoImpuestoIvaEnum.IVA_EXCENTO.getCode().equals(ti.getCodigoPorcentaje())) {
                totalExentoIVA = totalExentoIVA.add(ti.getBaseImponible());
            }
            if (TipoImpuestoEnum.ICE.getCode() == cod.intValue()) {
                totalICE = totalICE.add(ti.getValor());
            }
            if (TipoImpuestoEnum.IRBPNR.getCode() == cod.intValue()) {
                totalIRBPNR = totalIRBPNR.add(ti.getValor());
            }
        }
        if (ivaDiferenteCero.isEmpty()) {
            ivaDiferenteCero.add(LlenaIvaDiferenteCero(infoFactura));
        }
        tc.setIvaDistintoCero(ivaDiferenteCero);

        tc.setSubtotal0(totalIva0);
        tc.setTotalIce(totalICE);
        tc.setSubtotal(totalIva0.add(totalIva));
        tc.setSubtotalExentoIVA(totalExentoIVA);
        tc.setTotalIRBPNR(totalIRBPNR);
        tc.setSubtotalNoSujetoIva(totalSinImpuesto);
        return tc;
    }

    private String obtenerPorcentajeIvaVigente(String cod) {
        return "12 %";
    }

    private String obtenerPorcentajeIvaVigente(Date fechaEmision) {
        return "12 %";
    }

    private IvaDiferenteCeroReporte LlenaIvaDiferenteCero(Factura.InfoFactura infoFactura) {
        BigDecimal valor = BigDecimal.ZERO.setScale(2);
        String porcentajeIva = obtenerPorcentajeIvaVigente(DeStringADate(infoFactura.getFechaEmision()));
        return new IvaDiferenteCeroReporte(valor, porcentajeIva, valor);
    }

    public Date DeStringADate(String fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String strFecha = fecha;
        Date fechaDate = null;

        try {
            fechaDate = formato.parse(strFecha);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return fechaDate;

    }
}
