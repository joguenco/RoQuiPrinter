package dev.joguenco.pdf;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class TotalReceipt {
  @Getter @Setter private BigDecimal subtotal0;

  @Getter @Setter private BigDecimal subtotalNoSujetoIva;

  @Getter @Setter private BigDecimal subtotal;

  @Getter @Setter private List<TaxIvaNotZero> ivaDistintoCero;

  @Getter @Setter private BigDecimal totalIce;

  @Getter @Setter private BigDecimal totalIRBPNR;

  @Getter @Setter private BigDecimal subtotalExentoIVA;
}
