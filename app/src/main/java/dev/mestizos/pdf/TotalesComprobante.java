package dev.mestizos.pdf;


import java.math.BigDecimal;

public class TotalesComprobante
{
    private String descripcion;
    private BigDecimal valor;
    private boolean esNegativo;

    public TotalesComprobante(String descripcion, BigDecimal valor, boolean esNegativo) {
        this.descripcion = descripcion;
        this.valor = valor;
        this.esNegativo = esNegativo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getValor() {
        return this.valor;
    }


    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }


    public void setEsNegativo(boolean esNegativo) {
        this.esNegativo = esNegativo;
    }


    public boolean getEsNegativo() {
        return this.esNegativo;
    }
}