package dev.mestizos.pdf;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

public class TotalReceipt {
    @Getter
    @Setter
    private BigDecimal subtotal0;

    @Getter
    @Setter
    private BigDecimal subtotalNoSujetoIva;

    @Getter
    @Setter
    private BigDecimal subtotal;

    @Getter
    @Setter
    private List<TaxIvaNotZero> ivaDistintoCero;

    @Getter
    @Setter
    private List<TaxIvaNotZero> ivaDiferenciado;

    @Getter
    @Setter
    private BigDecimal totalIce;

    @Getter
    @Setter
    private BigDecimal totalIRBPNR;

    @Getter
    @Setter
    private BigDecimal subtotalExentoIVA;
}
