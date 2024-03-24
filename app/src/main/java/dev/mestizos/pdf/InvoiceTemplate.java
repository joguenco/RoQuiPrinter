package dev.mestizos.pdf;

import ec.gob.sri.invoice.v210.Factura;
import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;


public class InvoiceTemplate {

    @Getter
    private Factura factura;
    private String detalle1;
    private String detalle2;
    private String detalle3;
    private List<DetailsReport> detallesAdiciones;
    private List<AdditionalInformation> infoAdicional;
    private List<PayMethod> formasPago;
    private List<TotalReceipts> totalesComprobante;

    public InvoiceTemplate(Factura factura) {
        this.factura = factura;
    }

    public List<DetailsReport> getDetallesAdiciones() {
        this.detallesAdiciones = new ArrayList<>();

        for (Factura.Detalles.Detalle det : getFactura().getDetalles().getDetalle()) {
            DetailsReport detAd = new DetailsReport();
            detAd.setCodigoPrincipal(det.getCodigoPrincipal());
            detAd.setCodigoAuxiliar(det.getCodigoAuxiliar());
            detAd.setDescripcion(det.getDescripcion());
            detAd.setCantidad(det.getCantidad().toPlainString());
            detAd.setPrecioTotalSinImpuesto(det.getPrecioTotalSinImpuesto().toString());
            detAd.setPrecioUnitario(det.getPrecioUnitario());
            detAd.setPrecioSinSubsidio(det.getPrecioSinSubsidio());
            if (det.getDescuento() != null) {
                detAd.setDescuento(det.getDescuento().toString());
            }
            int i = 0;
            if (det.getDetallesAdicionales() != null && det.getDetallesAdicionales().getDetAdicional() != null && !det.getDetallesAdicionales().getDetAdicional().isEmpty()) {
                for (Factura.Detalles.Detalle.DetallesAdicionales.DetAdicional detAdicional : det.getDetallesAdicionales().getDetAdicional()) {
                    if (i == 0) {
                        detAd.setDetalle1(detAdicional.getNombre());
                    }
                    if (i == 1) {
                        detAd.setDetalle2(detAdicional.getNombre());
                    }
                    if (i == 2) {
                        detAd.setDetalle3(detAdicional.getNombre());
                    }
                    i++;
                }
            }
            detAd.setInfoAdicional(getInfoAdicional());


            if (getFormasPago() != null) {
                detAd.setFormasPago(getFormasPago());
            }
            detAd.setTotalesComprobante(getTotalesComprobante());
            this.detallesAdiciones.add(detAd);
        }
        return this.detallesAdiciones;
    }

    public List<TotalReceipts> getTotalesComprobante() {
        this.totalesComprobante = new ArrayList<>();
        BigDecimal importeTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal compensaciones = BigDecimal.ZERO.setScale(2);
        BigDecimal oneHundred = new BigDecimal(100);
        TotalReceipt tc = getTotales(this.factura.getInfoFactura());

        for (TaxIvaNotZero iva : tc.getIvaDistintoCero()) {
            var ivaValue = "";
            if (iva.getValor().compareTo(BigDecimal.ZERO) > 0) {
                ivaValue = iva.getValor().multiply(oneHundred).setScale(0).toString();
            } else {
                ivaValue = iva.getTarifa();
            }

            this.totalesComprobante.add(new TotalReceipts("SUBTOTAL " +
                    ivaValue
                    + "%", iva.getSubtotal(), false));
        }

        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL IVA 0%", tc.getSubtotal0(), false));
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL NO OBJETO IVA", tc.getSubtotalNoSujetoIva(), false));
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL EXENTO IVA", tc.getSubtotalExentoIVA(), false));
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL SIN IMPUESTOS", this.factura.getInfoFactura().getTotalSinImpuestos(), false));
        this.totalesComprobante.add(new TotalReceipts("DESCUENTO", this.factura.getInfoFactura().getTotalDescuento(), false));
        this.totalesComprobante.add(new TotalReceipts("ICE", tc.getTotalIce(), false));

        for (TaxIvaNotZero iva : tc.getIvaDistintoCero()) {
            if (iva.getValor().compareTo(BigDecimal.ZERO) > 0) {
                this.totalesComprobante.add(new TotalReceipts("IVA " +
                        iva.getValor().multiply(oneHundred).setScale(0)
                        + "%", iva.getValor(), false));
            }else {
                this.totalesComprobante.add(new TotalReceipts("IVA " , iva.getValor(), false));
            }
        }

        this.totalesComprobante.add(new TotalReceipts("PROPINA", this.factura.getInfoFactura().getPropina(), false));
        if (this.factura.getInfoFactura().getCompensaciones() != null) {
            for (var compensacion : this.factura.getInfoFactura().getCompensaciones().getCompensacion()) {
                compensaciones = compensaciones.add(compensacion.getValor());
            }
            importeTotal = this.factura.getInfoFactura().getImporteTotal().add(compensaciones);
        }
        if (!compensaciones.equals(BigDecimal.ZERO.setScale(2))) {
            this.totalesComprobante.add(new TotalReceipts("VALOR TOTAL", importeTotal, false));
            for (var compensacion : this.factura.getInfoFactura().getCompensaciones().getCompensacion()) {
                if (!compensacion.getValor().equals(BigDecimal.ZERO.setScale(2))) {
                    String detalleCompensacion = "";
                    this.totalesComprobante.add(new TotalReceipts("(-) " + detalleCompensacion, compensacion.getValor(), true));
                }
            }
            this.totalesComprobante.add(new TotalReceipts("VALOR A PAGAR", this.factura.getInfoFactura().getImporteTotal(), false));
        } else {
            this.totalesComprobante.add(new TotalReceipts("VALOR TOTAL", this.factura.getInfoFactura().getImporteTotal(), false));
        }
        return this.totalesComprobante;
    }

    public void setDetallesAdiciones(List<DetailsReport> detallesAdiciones) {
        this.detallesAdiciones = detallesAdiciones;
    }

    public List<AdditionalInformation> getInfoAdicional() {
//        System.out.println("--->" + getFactura());
        if (getFactura().getInfoAdicional() != null) {
            this.infoAdicional = new ArrayList();
            if ((getFactura().getInfoAdicional().getCampoAdicional() != null) && (!this.factura.getInfoAdicional().getCampoAdicional().isEmpty())) {
                for (Factura.InfoAdicional.CampoAdicional ca : getFactura().getInfoAdicional().getCampoAdicional()) {
                    this.infoAdicional.add(new AdditionalInformation(ca.getValue(), ca.getNombre()));
                }
            }
        }
        return this.infoAdicional;
    }

    private TotalReceipt getTotales(Factura.InfoFactura infoFactura) {
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
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
                else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_12.getCode())) {
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
                else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_13.getCode())) {
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
                else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_15.getCode())) {
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
                else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_5.getCode())) {
                    TaxIvaNotZero iva = new TaxIvaNotZero(ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
                else {
                    String codigoPorcentaje = "e";
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
            ivaDiferenteCero.add(LlenaIvaDiferenteCero());
        }
        tc.setIvaDistintoCero(ivaDiferenteCero);
        tc.setSubtotal0(totalIva0);
        tc.setTotalIce(totalICE);
        tc.setSubtotal(totalIva0.add(totalIva).add(totalSinImpuesto).add(totalExentoIVA));
        tc.setSubtotalExentoIVA(totalExentoIVA);
        tc.setTotalIRBPNR(totalIRBPNR);
        tc.setSubtotalNoSujetoIva(totalSinImpuesto);
        return tc;
    }

    private TaxIvaNotZero LlenaIvaDiferenteCero() {
        BigDecimal valor = BigDecimal.ZERO.setScale(2);
        String percentageIva = defaultIVA();
        return new TaxIvaNotZero(valor, percentageIva, valor);
    }

    private String defaultIVA() {
        return "IVA";
    }

    public void setInfoAdicional(List<AdditionalInformation> infoAdicional) {
        this.infoAdicional = infoAdicional;
    }

    public List<PayMethod> getFormasPago() {
//        System.out.println("--->" + getFactura());
        if (getFactura().getInfoFactura().getPagos() != null) {
            this.formasPago = new ArrayList();
            if ((getFactura().getInfoFactura().getPagos().getPago() != null) && (!this.factura.getInfoFactura().getPagos().getPago().isEmpty())) {
                for (var pa : getFactura().getInfoFactura().getPagos().getPago()) {
                    this.formasPago.add(new PayMethod(obtenerDetalleFormaPago(pa.getFormaPago()), pa.getTotal().setScale(2).toString()));
                }
            }
        }
        return this.formasPago;
    }

    private String obtenerDetalleFormaPago(String codigo) {
        return codigo;
    }
}

