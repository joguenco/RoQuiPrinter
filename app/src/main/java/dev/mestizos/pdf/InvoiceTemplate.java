package dev.mestizos.pdf;

import ec.gob.sri.invoice.v210.Factura;
import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;


public class invoice {

    @Getter
    private Factura factura;
    private String detalle1;
    private String detalle2;
    private String detalle3;
    private List<DetailsReport> detallesAdiciones;
    private List<AdditionalInformation> infoAdicional;
    private List<PayMethod> formasPago;
    private List<TotalReceipts> totalesComprobante;

    public invoice(Factura factura) {
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
        TotalReceipt tc = getTotales(this.factura.getInfoFactura());
        for (TaxIvaNotZero iva : tc.getIvaDistintoCero()) {
            this.totalesComprobante.add(new TotalReceipts("SUBTOTAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
        }
        for (TaxIvaNotZero iva : tc.getIvaDiferenciado()) {
            this.totalesComprobante.add(new TotalReceipts("SUBTOTAL TARIFA ESPECIAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
        }
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL IVA 0%", tc.getSubtotal0(), false));
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL NO OBJETO IVA", tc.getSubtotalNoSujetoIva(), false));
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL EXENTO IVA", tc.getSubtotalExentoIVA(), false));
        this.totalesComprobante.add(new TotalReceipts("SUBTOTAL SIN IMPUESTOS", this.factura.getInfoFactura().getTotalSinImpuestos(), false));
        this.totalesComprobante.add(new TotalReceipts("DESCUENTO", this.factura.getInfoFactura().getTotalDescuento(), false));
        this.totalesComprobante.add(new TotalReceipts("ICE", tc.getTotalIce(), false));
        for (TaxIvaNotZero iva : tc.getIvaDistintoCero()) {
            this.totalesComprobante.add(new TotalReceipts("IVA " + iva.getTarifa() + "%", iva.getValor(), false));
        }
        for (TaxIvaNotZero iva : tc.getIvaDiferenciado()) {
            this.totalesComprobante.add(new TotalReceipts("IVA " + iva.getTarifa() + "%", iva.getValor(), false));
        }
        this.totalesComprobante.add(new TotalReceipts("IRBPNR", tc.getTotalIRBPNR(), false));
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
        List<TaxIvaNotZero> ivaDiferenciado = new ArrayList<>();

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
                    ivaDiferenciado.add(iva);
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
            ivaDiferenteCero.add(LlenaIvaDiferenteCero());
        }
        tc.setIvaDistintoCero(ivaDiferenteCero);
        tc.setIvaDiferenciado(ivaDiferenciado);
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
        String porcentajeIva = ObtieneIvaRideFactura(this.factura.getInfoFactura().getTotalConImpuestos(), DeStringADate(this.factura.getInfoFactura().getFechaEmision()));
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

    private String ObtieneIvaRideFactura(Factura.InfoFactura.TotalConImpuestos impuestos, Date fecha) {
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto impuesto : impuestos.getTotalImpuesto()) {
            Integer cod = Integer.valueOf(impuesto.getCodigo());
            if (TypeTaxEnum.IVA.getCode() == cod.intValue() && impuesto.getValor().doubleValue() > 0.0D) {
                return obtenerPorcentajeIvaVigente(TypeTaxIvaEnum.IVA_VENTA_12.getCode());
            }
        }
        return obtenerPorcentajeIvaVigente(fecha).toString();
    }

    private String obtenerPorcentajeIvaVigente(Date fechaEmision) {
        return "12 %";
    }

    private String obtenerPorcentajeIvaVigente(String cod) {
        return "12 %";
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

