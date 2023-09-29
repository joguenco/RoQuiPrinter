package dev.mestizos.pdf;

import dev.mestizos.serialize.Invoice;
import ec.gob.sri.invoice.v210.Factura;
import ec.gob.sri.invoice.v210.Impuesto;
import ec.gob.sri.invoice.v210.InfoTributaria;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceReport {
    String pathXmlFile;
    String reportFolder;
    String pathLogo;
    String pdfFolder;


    public InvoiceReport(String rutaArchivo, String directorioReportes, String directorioLogo, String directorioDestino) {
        this.pathXmlFile = rutaArchivo;
        this.reportFolder = directorioReportes;
        this.pathLogo = directorioLogo;
        this.pdfFolder = directorioDestino;
    }

    public Boolean pdf(String authorization, String authorizationDate) {
        var invoice = new Invoice(pathXmlFile);

        InvoiceTemplate fr = new InvoiceTemplate(invoice.xmlToObject());
        generarReporte(fr, authorization, authorizationDate);
        return true;
    }

    private void generarReporte(InvoiceTemplate xml, String numAut, String fechaAut) {
        generarReporte(this.reportFolder + File.separator + "factura.jasper", xml, numAut, fechaAut);
    }

    private void generarReporte(String urlReporte, InvoiceTemplate fact, String numAut, String fechaAut) {
        FileInputStream is = null;
        try {
            JRDataSource dataSource = new JRBeanCollectionDataSource(fact.getDetallesAdiciones());
            is = new FileInputStream(urlReporte);
            JasperPrint reporte_view = JasperFillManager.fillReport(is,
                    obtenerMapaParametrosReportes(
                            obtenerParametrosInfoTriobutaria(fact.getFactura().getInfoTributaria(),
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
            OutputStream output = new FileOutputStream(new File(this.pdfFolder + File.separatorChar + nombrePDF + ".pdf"));
            JasperExportManager.exportReportToPdfStream(jp, output);
            output.close();
            System.out.println("PDF: Guardado en " + this.pdfFolder + File.separatorChar + nombrePDF + ".pdf");
        } catch (JRException | FileNotFoundException ex) {
            System.out.println("Error");
        } catch (IOException ex) {
            System.out.println("Error");
        }
    }

    public Map<String, Object> obtenerParametrosInfoTriobutaria(InfoTributaria infoTributaria, String numAut, String fechaAut) {
        Map param = new HashMap();
        param.put("RUC", infoTributaria.getRuc());
        param.put("CLAVE_ACC", infoTributaria.getClaveAcceso());
        param.put("RAZON_SOCIAL", infoTributaria.getRazonSocial());
        param.put("DIR_MATRIZ", infoTributaria.getDirMatriz());
        param.put("AGENTE_RETENCION", infoTributaria.getAgenteRetencion());
        param.put("REGIMEN_RIMPE", infoTributaria.getContribuyenteRimpe());
        try {
            param.put("LOGO", new FileInputStream(pathLogo));
//            param.put("LOGO", new FileInputStream("resources/images/logo.jpeg"));

        } catch (FileNotFoundException ex) {
            System.out.println("Error");
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
        param.put("NUM_FACT", infoTributaria.getEstab() + "-" + infoTributaria.getPtoEmi() + "-" + infoTributaria.getSecuencial());
        if (infoTributaria.getAmbiente().equals("1")) {
            param.put("AMBIENTE", "Pruebas");
        } else {
            param.put("AMBIENTE", "Producción");
        }
        param.put("NOM_COMERCIAL", infoTributaria.getNombreComercial());
        return param;
    }

    private Map<String, Object> obtenerMapaParametrosReportes(Map<String, Object> mapa1, Map<String, Object> mapa2) {
        mapa1.putAll(mapa2);
        return mapa1;
    }

    private Map<String, Object> obtenerInfoFactura(Factura.InfoFactura infoFactura, InvoiceTemplate fact) {
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
        TotalReceipt tc = getTotals(infoFactura);
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

    private BigDecimal obtenerTotalSinSubsidio(InvoiceTemplate fact) {
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
                if (((Impuesto) impuesto.get(c)).getCodigo().equals(String.valueOf(TypeTaxEnum.IVA.getCode())) && !((Impuesto) impuesto.get(c)).getTarifa().equals(BigDecimal.ZERO)) {
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

    private BigDecimal obtenerTotalSinDescuento(InvoiceTemplate fact) {
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
                if (((Impuesto) impuesto.get(c)).getCodigo().equals(String.valueOf(TypeTaxEnum.IVA.getCode())) && !((Impuesto) impuesto.get(c)).getTarifa().equals(BigDecimal.ZERO)) {
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

    private TotalReceipt getTotals(Factura.InfoFactura infoFactura) {
        List<TaxIvaNotZero> ivaDiferenteCero = new ArrayList<>();

        BigDecimal totalIva = new BigDecimal(0.0D);
        BigDecimal totalIva0 = new BigDecimal(0.0D);
        BigDecimal totalExentoIVA = new BigDecimal(0.0D);
        BigDecimal totalICE = new BigDecimal(0.0D);
        BigDecimal totalIRBPNR = new BigDecimal(0.0D);
        BigDecimal totalSinImpuesto = new BigDecimal(0.0D);
        TotalReceipt tc = new TotalReceipt();
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto ti : infoFactura.getTotalConImpuestos().getTotalImpuesto()) {
            Integer cod = Integer.valueOf(ti.getCodigo());

            if (TypeTaxEnum.IVA.getCode() == cod.intValue() && ti.getValor().doubleValue() > 0.0D) {
                if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_DIFERENCIADO.getCode())) {
                    String codigoPorcentaje = "TARIFA ESPECIAL " + ti.getCodigoPorcentaje();
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
                    ivaDiferenteCero.add(iva);
                } else {
                    String codigoPorcentaje = obtenerPorcentajeIvaVigente(ti.getCodigoPorcentaje());
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
            }

            if (TypeTaxEnum.IVA.getCode() == cod.intValue() && TypeTaxIvaEnum.IVA_VENTA_0.getCode().equals(ti.getCodigoPorcentaje())) {
                totalIva0 = totalIva0.add(ti.getBaseImponible());
            }
            if (TypeTaxEnum.IVA.getCode() == cod.intValue() && TypeTaxIvaEnum.IVA_NO_OBJETO.getCode().equals(ti.getCodigoPorcentaje())) {
                totalSinImpuesto = totalSinImpuesto.add(ti.getBaseImponible());
            }
            if (TypeTaxEnum.IVA.getCode() == cod.intValue() && TypeTaxIvaEnum.IVA_EXCENTO.getCode().equals(ti.getCodigoPorcentaje())) {
                totalExentoIVA = totalExentoIVA.add(ti.getBaseImponible());
            }
            if (TypeTaxEnum.ICE.getCode() == cod.intValue()) {
                totalICE = totalICE.add(ti.getValor());
            }
            if (TypeTaxEnum.IRBPNR.getCode() == cod.intValue()) {
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
        return "12";
    }

    private String obtenerPorcentajeIvaVigente(Date fechaEmision) {
        return "12";
    }

    private TaxIvaNotZero LlenaIvaDiferenteCero(Factura.InfoFactura infoFactura) {
        BigDecimal valor = BigDecimal.ZERO.setScale(2);
        String porcentajeIva = obtenerPorcentajeIvaVigente(DeStringADate(infoFactura.getFechaEmision()));
        return new TaxIvaNotZero(valor, porcentajeIva, valor);
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
