package dev.mestizos.pdf;

import ec.gob.sri.invoice.v210.Factura;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;


public class FacturaReporte {

    private Factura factura;
    private String detalle1;
    private String detalle2;
    private String detalle3;
    private List<DetallesAdicionalesReporte> detallesAdiciones;
    private List<InformacionAdicional> infoAdicional;
    private List<FormasPago> formasPago;
    private List<TotalesComprobante> totalesComprobante;

    public void setFormasPago(List<FormasPago> formasPago) {
        this.formasPago = formasPago;
    }

    public FacturaReporte(Factura factura) {
        this.factura = factura;
    }

    public Factura getFactura() {
        return this.factura;
    }

    public void setFactura(Factura factura) {
        this.factura = factura;
    }

    public String getDetalle1() {
        return this.detalle1;
    }

    public void setDetalle1(String detalle1) {
        this.detalle1 = detalle1;
    }

    public String getDetalle2() {
        return this.detalle2;
    }

    public void setDetalle2(String detalle2) {
        this.detalle2 = detalle2;
    }

    public String getDetalle3() {
        return this.detalle3;
    }

    public void setDetalle3(String detalle3) {
        this.detalle3 = detalle3;
    }

    public List<DetallesAdicionalesReporte> getDetallesAdiciones() {
        this.detallesAdiciones = new ArrayList<>();

        for (Factura.Detalles.Detalle det : getFactura().getDetalles().getDetalle()) {
            DetallesAdicionalesReporte detAd = new DetallesAdicionalesReporte();
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

    public List<TotalesComprobante> getTotalesComprobante() {
        this.totalesComprobante = new ArrayList<>();
        BigDecimal importeTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal compensaciones = BigDecimal.ZERO.setScale(2);
        TotalComprobante tc = getTotales(this.factura.getInfoFactura());
        for (IvaDiferenteCeroReporte iva : tc.getIvaDistintoCero()) {
            this.totalesComprobante.add(new TotalesComprobante("SUBTOTAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
        }
        for (IvaDiferenteCeroReporte iva : tc.getIvaDiferenciado()) {
            this.totalesComprobante.add(new TotalesComprobante("SUBTOTAL TARIFA ESPECIAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
        }
        this.totalesComprobante.add(new TotalesComprobante("SUBTOTAL IVA 0%", tc.getSubtotal0(), false));
        this.totalesComprobante.add(new TotalesComprobante("SUBTOTAL NO OBJETO IVA", tc.getSubtotalNoSujetoIva(), false));
        this.totalesComprobante.add(new TotalesComprobante("SUBTOTAL EXENTO IVA", tc.getSubtotalExentoIVA(), false));
        this.totalesComprobante.add(new TotalesComprobante("SUBTOTAL SIN IMPUESTOS", this.factura.getInfoFactura().getTotalSinImpuestos(), false));
        this.totalesComprobante.add(new TotalesComprobante("DESCUENTO", this.factura.getInfoFactura().getTotalDescuento(), false));
        this.totalesComprobante.add(new TotalesComprobante("ICE", tc.getTotalIce(), false));
        for (IvaDiferenteCeroReporte iva : tc.getIvaDistintoCero()) {
            this.totalesComprobante.add(new TotalesComprobante("IVA " + iva.getTarifa() + "%", iva.getValor(), false));
        }
        for (IvaDiferenteCeroReporte iva : tc.getIvaDiferenciado()) {
            this.totalesComprobante.add(new TotalesComprobante("IVA " + iva.getTarifa() + "%", iva.getValor(), false));
        }
        this.totalesComprobante.add(new TotalesComprobante("IRBPNR", tc.getTotalIRBPNR(), false));
        this.totalesComprobante.add(new TotalesComprobante("PROPINA", this.factura.getInfoFactura().getPropina(), false));
        if (this.factura.getInfoFactura().getCompensaciones() != null) {
            for (var compensacion : this.factura.getInfoFactura().getCompensaciones().getCompensacion()) {
                compensaciones = compensaciones.add(compensacion.getValor());
            }
            importeTotal = this.factura.getInfoFactura().getImporteTotal().add(compensaciones);
        }
        if (!compensaciones.equals(BigDecimal.ZERO.setScale(2))) {
            this.totalesComprobante.add(new TotalesComprobante("VALOR TOTAL", importeTotal, false));
            for (var compensacion : this.factura.getInfoFactura().getCompensaciones().getCompensacion()) {
                if (!compensacion.getValor().equals(BigDecimal.ZERO.setScale(2))) {
                    String detalleCompensacion = "";
                    this.totalesComprobante.add(new TotalesComprobante("(-) " + detalleCompensacion, compensacion.getValor(), true));
                }
            }
            this.totalesComprobante.add(new TotalesComprobante("VALOR A PAGAR", this.factura.getInfoFactura().getImporteTotal(), false));
        } else {
            this.totalesComprobante.add(new TotalesComprobante("VALOR TOTAL", this.factura.getInfoFactura().getImporteTotal(), false));
        }
        return this.totalesComprobante;
    }

    public void setDetallesAdiciones(List<DetallesAdicionalesReporte> detallesAdiciones) {
        this.detallesAdiciones = detallesAdiciones;
    }

    public List<InformacionAdicional> getInfoAdicional() {
//        System.out.println("--->" + getFactura());
        if (getFactura().getInfoAdicional() != null) {
            this.infoAdicional = new ArrayList();
            if ((getFactura().getInfoAdicional().getCampoAdicional() != null) && (!this.factura.getInfoAdicional().getCampoAdicional().isEmpty())) {
                for (Factura.InfoAdicional.CampoAdicional ca : getFactura().getInfoAdicional().getCampoAdicional()) {
                    this.infoAdicional.add(new InformacionAdicional(ca.getValue(), ca.getNombre()));
                }
            }
        }
        return this.infoAdicional;
    }

    private TotalComprobante getTotales(Factura.InfoFactura infoFactura) {
        List<IvaDiferenteCeroReporte> ivaDiferenteCero = new ArrayList<>();
        List<IvaDiferenteCeroReporte> ivaDiferenciado = new ArrayList<>();

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
                    IvaDiferenteCeroReporte iva = new IvaDiferenteCeroReporte(ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenciado.add(iva);
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

    private IvaDiferenteCeroReporte LlenaIvaDiferenteCero() {
        BigDecimal valor = BigDecimal.ZERO.setScale(2);
        String porcentajeIva = ObtieneIvaRideFactura(this.factura.getInfoFactura().getTotalConImpuestos(), DeStringADate(this.factura.getInfoFactura().getFechaEmision()));
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

    private String ObtieneIvaRideFactura(Factura.InfoFactura.TotalConImpuestos impuestos, Date fecha) {
        for (Factura.InfoFactura.TotalConImpuestos.TotalImpuesto impuesto : impuestos.getTotalImpuesto()) {
            Integer cod = Integer.valueOf(impuesto.getCodigo());
            if (TipoImpuestoEnum.IVA.getCode() == cod.intValue() && impuesto.getValor().doubleValue() > 0.0D) {
                return obtenerPorcentajeIvaVigente(TipoImpuestoIvaEnum.IVA_VENTA_12.getCode());
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

    public void setInfoAdicional(List<InformacionAdicional> infoAdicional) {
        this.infoAdicional = infoAdicional;
    }

    public List<FormasPago> getFormasPago() {
//        System.out.println("--->" + getFactura());
        if (getFactura().getInfoFactura().getPagos() != null) {
            this.formasPago = new ArrayList();
            if ((getFactura().getInfoFactura().getPagos().getPago() != null) && (!this.factura.getInfoFactura().getPagos().getPago().isEmpty())) {
                for (var pa : getFactura().getInfoFactura().getPagos().getPago()) {
                    this.formasPago.add(new FormasPago(obtenerDetalleFormaPago(pa.getFormaPago()), pa.getTotal().setScale(2).toString()));
                }
            }
        }
        return this.formasPago;
    }

    private String obtenerDetalleFormaPago(String codigo) {
        return codigo;
    }
}

