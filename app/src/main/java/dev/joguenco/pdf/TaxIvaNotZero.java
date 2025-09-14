package dev.joguenco.pdf;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

public class TaxIvaNotZero {
  @Getter @Setter private BigDecimal subtotal;

  @Getter @Setter private String tarifa;

  @Getter @Setter private BigDecimal valor;

  public TaxIvaNotZero(BigDecimal subtotal, String tarifa, BigDecimal valor) {
    this.subtotal = subtotal;
    this.tarifa = tarifa;
    this.valor = valor;
  }
}
