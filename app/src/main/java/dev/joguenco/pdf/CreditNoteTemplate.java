package dev.joguenco.pdf;

import ec.gob.sri.note.credit.v110.NotaCredito;
import ec.gob.sri.note.credit.v110.TotalConImpuestos;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class CreditNoteTemplate {

  @Getter private NotaCredito notaCredito;
  private List<DetailsReport> detallesAdiciones;
  private List<AdditionalInformation> infoAdicional;
  private List<TotalReceipts> totalReceipts;

  public CreditNoteTemplate(NotaCredito notaCredito) {
    this.notaCredito = notaCredito;
  }

  public List<DetailsReport> getDetallesAdiciones() {
    this.detallesAdiciones = new ArrayList<>();
    for (NotaCredito.Detalles.Detalle det : getNotaCredito().getDetalles().getDetalle()) {
      DetailsReport detAd = new DetailsReport();
      detAd.setCodigoPrincipal(det.getCodigoInterno());
      detAd.setCodigoAuxiliar(det.getCodigoAdicional());
      detAd.setDescripcion(det.getDescripcion());
      detAd.setCantidad(det.getCantidad().toPlainString());
      detAd.setPrecioTotalSinImpuesto(det.getPrecioTotalSinImpuesto().toString());
      detAd.setPrecioUnitario(det.getPrecioUnitario());
      detAd.setDescuento(det.getDescuento().toString());
      int i = 0;
      if (det.getDetallesAdicionales() != null
          && det.getDetallesAdicionales().getDetAdicional() != null)
        for (NotaCredito.Detalles.Detalle.DetallesAdicionales.DetAdicional detAdicional :
            det.getDetallesAdicionales().getDetAdicional()) {
          if (i == 0) detAd.setDetalle1(detAdicional.getNombre());
          if (i == 1) detAd.setDetalle2(detAdicional.getNombre());
          if (i == 2) detAd.setDetalle3(detAdicional.getNombre());
          i++;
        }
      detAd.setInfoAdicional(getInfoAdicional());
      detAd.setTotalesComprobante(getTotalReceipts());
      this.detallesAdiciones.add(detAd);
    }
    return this.detallesAdiciones;
  }

  public List<AdditionalInformation> getInfoAdicional() {
    if (this.notaCredito.getInfoAdicional() != null) {
      this.infoAdicional = new ArrayList<>();
      if (this.notaCredito.getInfoAdicional().getCampoAdicional() != null
          && !this.notaCredito.getInfoAdicional().getCampoAdicional().isEmpty())
        for (NotaCredito.InfoAdicional.CampoAdicional ca :
            this.notaCredito.getInfoAdicional().getCampoAdicional())
          this.infoAdicional.add(new AdditionalInformation(ca.getValue(), ca.getNombre()));
    }
    return this.infoAdicional;
  }

  public List<TotalReceipts> getTotalReceipts() {
    this.totalReceipts = new ArrayList<>();
    BigDecimal importeTotal = BigDecimal.ZERO.setScale(2);
    TotalReceipt tc = getTotalesNC(this.notaCredito.getInfoNotaCredito());
    BigDecimal compensaciones = BigDecimal.ZERO.setScale(2);
    for (TaxIvaNotZero iva : tc.getIvaDistintoCero()) {
      if (iva.getSubtotal().compareTo(BigDecimal.ZERO) > 0)
        this.totalReceipts.add(
            new TotalReceipts("SUBTOTAL " + iva.getTarifa() + "%", iva.getSubtotal(), false));
    }

    if (tc.getSubtotal0().compareTo(BigDecimal.ZERO) > 0)
      this.totalReceipts.add(new TotalReceipts("SUBTOTAL IVA 0%", tc.getSubtotal0(), false));
    if (tc.getSubtotalNoSujetoIva().compareTo(BigDecimal.ZERO) > 0)
      this.totalReceipts.add(
          new TotalReceipts("SUBTOTAL NO OBJETO IVA", tc.getSubtotalNoSujetoIva(), false));
    if (tc.getSubtotalExentoIVA().compareTo(BigDecimal.ZERO) > 0)
      this.totalReceipts.add(
          new TotalReceipts("SUBTOTAL EXENTO IVA", tc.getSubtotalExentoIVA(), false));
    this.totalReceipts.add(
        new TotalReceipts(
            "SUBTOTAL SIN IMPUESTOS",
            this.notaCredito.getInfoNotaCredito().getTotalSinImpuestos(),
            false));

    this.totalReceipts.add(new TotalReceipts("DESCUENTO", getDiscount(), false));
    if (tc.getTotalIce().compareTo(BigDecimal.ZERO) > 0)
      this.totalReceipts.add(new TotalReceipts("ICE", tc.getTotalIce(), false));
    for (TaxIvaNotZero iva : tc.getIvaDistintoCero())
      this.totalReceipts.add(
          new TotalReceipts("IVA " + iva.getTarifa() + "%", iva.getValor(), false));

    if (tc.getTotalIRBPNR().compareTo(BigDecimal.ZERO) > 0)
      this.totalReceipts.add(new TotalReceipts("IRBPNR", tc.getTotalIRBPNR(), false));

    this.totalReceipts.add(
        new TotalReceipts(
            "VALOR TOTAL", this.notaCredito.getInfoNotaCredito().getValorModificacion(), false));

    return this.totalReceipts;
  }

  private TotalReceipt getTotalesNC(NotaCredito.InfoNotaCredito infoNc) {
    List<TaxIvaNotZero> ivaDiferenciado = new ArrayList<>();
    List<TaxIvaNotZero> ivaDiferenteCero = new ArrayList<>();
    BigDecimal totalIva = new BigDecimal(0.0D);
    BigDecimal totalIva0 = new BigDecimal(0.0D);
    BigDecimal totalExentoIVA = new BigDecimal(0.0D);
    BigDecimal totalICE = new BigDecimal(0.0D);
    BigDecimal totalSinImpuesto = new BigDecimal(0.0D);
    BigDecimal totalIRBPNR = new BigDecimal(0.0D);
    TotalReceipt tc = new TotalReceipt();
    for (TotalConImpuestos.TotalImpuesto ti : infoNc.getTotalConImpuestos().getTotalImpuesto()) {
      Integer cod = Integer.parseInt(ti.getCodigo());
      var tarifa = ti.getValor().divide(ti.getBaseImponible(), 2, java.math.RoundingMode.HALF_UP);
      ti.setTarifa(tarifa.multiply(new BigDecimal(100)));

      if (TypeTaxEnum.IVA.getCode() == cod.intValue() && ti.getValor().doubleValue() > 0.0D) {
        if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_DIFERENCIADO.getCode())) {
          TaxIvaNotZero iva =
              new TaxIvaNotZero(
                  ti.getBaseImponible(), ti.getTarifa().setScale(0).toString(), ti.getValor());
          ivaDiferenteCero.add(iva);
        } else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_12.getCode())) {
          TaxIvaNotZero iva =
              new TaxIvaNotZero(
                  ti.getBaseImponible(), ti.getTarifa().setScale(0).toString(), ti.getValor());
          ivaDiferenteCero.add(iva);
        } else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_13.getCode())) {
          TaxIvaNotZero iva =
              new TaxIvaNotZero(
                  ti.getBaseImponible(), ti.getTarifa().setScale(0).toString(), ti.getValor());
          ivaDiferenteCero.add(iva);
        } else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_15.getCode())) {
          TaxIvaNotZero iva =
              new TaxIvaNotZero(
                  ti.getBaseImponible(), ti.getTarifa().setScale(0).toString(), ti.getValor());
          ivaDiferenteCero.add(iva);
        } else if (ti.getCodigoPorcentaje().equals(TypeTaxIvaEnum.IVA_VENTA_5.getCode())) {
          TaxIvaNotZero iva =
              new TaxIvaNotZero(
                  ti.getBaseImponible(), ti.getTarifa().setScale(0).toString(), ti.getValor());
          ivaDiferenteCero.add(iva);
        } else {
          String codigoPorcentaje = "e";
          TaxIvaNotZero iva =
              new TaxIvaNotZero(ti.getBaseImponible(), codigoPorcentaje, ti.getValor());
          ivaDiferenteCero.add(iva);
        }
      }

      if (TypeTaxEnum.IVA.getCode() == cod.intValue()
          && TypeTaxIvaEnum.IVA_VENTA_0.getCode().equals(ti.getCodigoPorcentaje())) {
        totalIva0 = totalIva0.add(ti.getBaseImponible());
      }
      if (TypeTaxEnum.IVA.getCode() == cod.intValue()
          && TypeTaxIvaEnum.IVA_NO_OBJETO.getCode().equals(ti.getCodigoPorcentaje())) {
        totalSinImpuesto = totalSinImpuesto.add(ti.getBaseImponible());
      }
      if (TypeTaxEnum.IVA.getCode() == cod.intValue()
          && TypeTaxIvaEnum.IVA_EXCENTO.getCode().equals(ti.getCodigoPorcentaje())) {
        totalExentoIVA = totalExentoIVA.add(ti.getBaseImponible());
      }
      if (TypeTaxEnum.ICE.getCode() == cod.intValue()) {
        totalICE = totalICE.add(ti.getValor());
      }
      if (TypeTaxEnum.IRBPNR.getCode() == cod.intValue()) {
        totalIRBPNR = totalIRBPNR.add(ti.getValor());
      }
    }
    if (ivaDiferenteCero.isEmpty()) ivaDiferenteCero.add(LlenaIvaDiferenteCero());
    tc.setIvaDistintoCero(ivaDiferenteCero);
    tc.setSubtotal0(totalIva0);
    tc.setTotalIce(totalICE);
    tc.setSubtotal(totalIva0.add(totalIva).add(totalExentoIVA).add(totalSinImpuesto));
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
    return "e";
  }

  private BigDecimal getDiscount() {
    BigDecimal descuento = new BigDecimal(0);
    for (NotaCredito.Detalles.Detalle det : getNotaCredito().getDetalles().getDetalle()) {
      descuento = descuento.add(det.getDescuento());
    }
    return descuento;
  }
}
