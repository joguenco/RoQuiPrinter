package dev.joguenco.pdf;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class TaxIvaNotZero
{
    @Getter
    @Setter
    private BigDecimal subtotal;

    @Getter
    @Setter
    private String tarifa;

    @Getter
    @Setter
    private BigDecimal valor;

    public TaxIvaNotZero(BigDecimal subtotal, String tarifa, BigDecimal valor) {
        this.subtotal = subtotal;
        this.tarifa = tarifa;
        this.valor = valor;
    }
}