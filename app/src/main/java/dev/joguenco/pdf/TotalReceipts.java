package dev.joguenco.pdf;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class TotalReceipts
{
    @Getter
    @Setter
    private String descripcion;

    @Getter
    @Setter
    private BigDecimal valor;

    @Getter
    @Setter
    private boolean esNegativo;

    public TotalReceipts(String descripcion, BigDecimal valor, boolean esNegativo) {
        this.descripcion = descripcion;
        this.valor = valor;
        this.esNegativo = esNegativo;
    }
}