package dev.mestizos.pdf;


import java.math.BigDecimal;

public class IvaDiferenteCeroReporte
{
    private BigDecimal subtotal;
    private String tarifa;
    private BigDecimal valor;

    public IvaDiferenteCeroReporte(BigDecimal subtotal, String tarifa, BigDecimal valor) {
        this.subtotal = subtotal;
        this.tarifa = tarifa;
        this.valor = valor;
    }

    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getTarifa() {
        return this.tarifa;
    }

    public void setTarifa(String tarifa) {
        this.tarifa = tarifa;
    }

    public BigDecimal getValor() {
        return this.valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}