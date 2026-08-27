package dev.joguenco.pdf.note.debit;

import dev.joguenco.pdf.*;
import ec.gob.sri.note.debit.v100.NotaDebito;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class DebitNoteTemplate {

    @Getter private NotaDebito notaDebito;
    private List<AdditionalInformation> infoAdicional;
    private List<PayMethod> payMethod;

    public DebitNoteTemplate(NotaDebito notaDebito) {
        this.notaDebito = notaDebito;
    }

    public List<DetailsReport> getDetallesAdiciones() {
        List<DetailsReport> detallesAdiciones = new ArrayList<>();
        for (NotaDebito.Motivos.Motivo motivo : this.notaDebito.getMotivos().getMotivo()) {
            DetailsReport detAd = new DetailsReport();
            detAd.setRazonModificacion(motivo.getRazon());
            detAd.setValorModificacion(motivo.getValor().toString());
            detAd.setInfoAdicional(getInfoAdicional());
            if (getFormasPago() != null) detAd.setFormasPago(getFormasPago());
            detAd.setTotalesComprobante(getTotalesComprobante());
            detallesAdiciones.add(detAd);
        }
        return detallesAdiciones;
    }

    public List<AdditionalInformation> getInfoAdicional() {
        if (this.notaDebito.getInfoAdicional() != null
                && !this.notaDebito.getInfoAdicional().getCampoAdicional().isEmpty()) {
            this.infoAdicional = new ArrayList<>();
            for (NotaDebito.InfoAdicional.CampoAdicional info :
                    this.notaDebito.getInfoAdicional().getCampoAdicional()) {
                AdditionalInformation ia =
                        new AdditionalInformation(info.getValue(), info.getNombre());
                this.infoAdicional.add(ia);
            }
        }
        return this.infoAdicional;
    }

    public List<PayMethod> getFormasPago() {
        if (getNotaDebito().getInfoNotaDebito().getPagos() != null) {
            this.payMethod = new ArrayList<>();
            if (getNotaDebito().getInfoNotaDebito().getPagos().getFirst().getPago() != null
                    && !getNotaDebito()
                            .getInfoNotaDebito()
                            .getPagos()
                            .getFirst()
                            .getPago()
                            .isEmpty())
                for (var pa : getNotaDebito().getInfoNotaDebito().getPagos().getFirst().getPago())
                    this.payMethod.add(
                            new PayMethod(
                                    getNamePayMethod(pa.getFormaPago()),
                                    pa.getTotal().setScale(2).toString()));
        }
        return this.payMethod;
    }

    public List<TotalReceipts> getTotalesComprobante() {
        List<TotalReceipts> totalesComprobante = new ArrayList<>();
        BigDecimal importeTotal = BigDecimal.ZERO.setScale(2);
        BigDecimal compensaciones = BigDecimal.ZERO.setScale(2);
        TotalReceipt tc = getTotalesND(this.notaDebito.getInfoNotaDebito());
        for (var iva : tc.getIvaDistintoCero()) {
            if (iva.getSubtotal().compareTo(BigDecimal.ZERO) > 0)
                totalesComprobante.add(
                        new TotalReceipts(
                                "SUBTOTAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
        }

        if (tc.getSubtotal0().compareTo(BigDecimal.ZERO) > 0)
            totalesComprobante.add(new TotalReceipts("SUBTOTAL IVA 0%", tc.getSubtotal0(), false));
        if (tc.getSubtotalNoSujetoIva().compareTo(BigDecimal.ZERO) > 0)
            totalesComprobante.add(
                    new TotalReceipts(
                            "SUBTOTAL NO OBJETO IVA", tc.getSubtotalNoSujetoIva(), false));
        if (tc.getSubtotalExentoIVA().compareTo(BigDecimal.ZERO) > 0)
            totalesComprobante.add(
                    new TotalReceipts("SUBTOTAL EXENTO IVA", tc.getSubtotalExentoIVA(), false));
        totalesComprobante.add(
                new TotalReceipts(
                        "SUBTOTAL SIN IMPUESTOS",
                        this.notaDebito.getInfoNotaDebito().getTotalSinImpuestos(),
                        false));
        if (tc.getTotalIce().compareTo(BigDecimal.ZERO) > 0)
            totalesComprobante.add(new TotalReceipts("ICE", tc.getTotalIce(), false));
        for (var iva : tc.getIvaDistintoCero())
            totalesComprobante.add(
                    new TotalReceipts("IVA " + iva.getTarifa() + "%", iva.getValor(), false));
        if (tc.getTotalIRBPNR().compareTo(BigDecimal.ZERO) > 0)
            totalesComprobante.add(new TotalReceipts("IRBPNR", tc.getTotalIRBPNR(), false));

        if (!compensaciones.equals(BigDecimal.ZERO.setScale(2))) {
            totalesComprobante.add(new TotalReceipts("VALOR TOTAL", importeTotal, false));

            totalesComprobante.add(
                    new TotalReceipts(
                            "VALOR A PAGAR",
                            this.notaDebito.getInfoNotaDebito().getValorTotal(),
                            false));
        } else {
            totalesComprobante.add(
                    new TotalReceipts(
                            "VALOR TOTAL",
                            this.notaDebito.getInfoNotaDebito().getValorTotal(),
                            false));
        }
        return totalesComprobante;
    }

    private TotalReceipt getTotalesND(NotaDebito.InfoNotaDebito infoNotaDebito) {
        List<TaxIvaNotZero> ivaDiferenteCero = new ArrayList<>();

        BigDecimal totalIva = new BigDecimal(0.0D);
        BigDecimal totalIva0 = new BigDecimal(0.0D);
        BigDecimal totalICE = new BigDecimal(0.0D);
        BigDecimal totalIRBPNR = new BigDecimal(0.0D);
        BigDecimal totalExentoIVA = new BigDecimal(0.0D);
        BigDecimal totalSinImpuesto = new BigDecimal(0.0D);
        TotalReceipt tc = new TotalReceipt();
        for (var ti : infoNotaDebito.getImpuestos().getImpuesto()) {
            Integer cod = Integer.valueOf(ti.getCodigo());
            if (TypeTaxEnum.IVA.getCode() == cod.intValue() && ti.getValor().doubleValue() > 0.0D)
                if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_DIFERENCIADO.getCode())) {
                    TaxIvaNotZero iva =
                            new TaxIvaNotZero(
                                    ti.getBaseImponible(), ti.getCodigoPorcentaje(), ti.getValor());
                    ivaDiferenteCero.add(iva);
                } else {
                    String codigoPorcentaje = "e";
                    TaxIvaNotZero iva =
                            new TaxIvaNotZero(
                                    ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
                    ivaDiferenteCero.add(iva);
                }
            if (TypeTaxEnum.IVA.getCode() == cod.intValue()
                    && TypeTaxIvaEnum.IVA_VENTA_0.getCode().equals(ti.getCodigoPorcentaje()))
                totalIva0 = totalIva0.add(ti.getBaseImponible());
            if (TypeTaxEnum.IVA.getCode() == cod.intValue()
                    && TypeTaxIvaEnum.IVA_NO_OBJETO.getCode().equals(ti.getCodigoPorcentaje()))
                totalSinImpuesto = totalSinImpuesto.add(ti.getBaseImponible());
            if (TypeTaxEnum.IVA.getCode() == cod.intValue()
                    && TypeTaxIvaEnum.IVA_EXCENTO.getCode().equals(ti.getCodigoPorcentaje()))
                totalExentoIVA = totalExentoIVA.add(ti.getBaseImponible());
            if (TypeTaxEnum.ICE.getCode() == cod.intValue()) totalICE = totalICE.add(ti.getValor());
            if (TypeTaxEnum.IRBPNR.getCode() == cod.intValue())
                totalIRBPNR = totalIRBPNR.add(ti.getValor());
        }
        tc.setSubtotal0(totalIva0);
        tc.setSubtotal(
                totalIva.add(totalIva0).add(totalExentoIVA).add(totalSinImpuesto).setScale(2));
        tc.setTotalIce(totalICE);
        tc.setTotalIRBPNR(totalIRBPNR);
        if (ivaDiferenteCero.isEmpty()) ivaDiferenteCero.add(LlenaIvaDiferenteCero());
        tc.setIvaDistintoCero(ivaDiferenteCero);
        tc.setSubtotalExentoIVA(totalExentoIVA.setScale(2));
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

    private String getNamePayMethod(String code) {
        return switch (code) {
            case "01" -> "Sin utilización del sistema financiero";
            case "15" -> "Compensación de deudas";
            case "16" -> "Tarjeta de débito";
            case "17" -> "Dinero electrónico";
            case "18" -> "Tarjeta prepago";
            case "19" -> "Tarjeta de crédito";
            case "20" -> "Otros con utilización del sistema financiero";
            case "21" -> "Endoso de títulos";
            default -> code + " No definido";
        };
    }
}
