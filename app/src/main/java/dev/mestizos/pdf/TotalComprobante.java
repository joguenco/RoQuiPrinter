package dev.mestizos.pdf;

import java.math.BigDecimal;
import java.util.List;


public class TotalComprobante
{
    private BigDecimal subtotal0;
    private BigDecimal subtotalNoSujetoIva;
    private BigDecimal subtotal;
    private List<IvaDiferenteCeroReporte> ivaDistintoCero;
    private List<IvaDiferenteCeroReporte> ivaDiferenciado;
    private BigDecimal totalIce;
    private BigDecimal totalIRBPNR;
    private BigDecimal subtotalExentoIVA;

    public List<IvaDiferenteCeroReporte> getIvaDistintoCero() {
        return this.ivaDistintoCero;
    }




    public void setIvaDistintoCero(List<IvaDiferenteCeroReporte> ivaDistintoCero) {
        this.ivaDistintoCero = ivaDistintoCero;
    }




    public BigDecimal getSubtotal0() {
        return this.subtotal0;
    }




    public void setSubtotal0(BigDecimal subtotal0) {
        this.subtotal0 = subtotal0;
    }




    public BigDecimal getSubtotal() {
        return this.subtotal;
    }




    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }








    public BigDecimal getTotalIce() {
        return this.totalIce;
    }




    public void setTotalIce(BigDecimal totalIce) {
        this.totalIce = totalIce;
    }




    public BigDecimal getSubtotalNoSujetoIva() {
        return this.subtotalNoSujetoIva;
    }




    public void setSubtotalNoSujetoIva(BigDecimal subtotalNoSujetoIva) {
        this.subtotalNoSujetoIva = subtotalNoSujetoIva;
    }

    public BigDecimal getTotalIRBPNR() {
        return this.totalIRBPNR;
    }

    public void setTotalIRBPNR(BigDecimal totalIRBPNR) {
        this.totalIRBPNR = totalIRBPNR;
    }

    public BigDecimal getSubtotalExentoIVA() {
        return this.subtotalExentoIVA;
    }

    public void setSubtotalExentoIVA(BigDecimal subtotalExentoIVA) {
        this.subtotalExentoIVA = subtotalExentoIVA;
    }

    public List<IvaDiferenteCeroReporte> getIvaDiferenciado() {
        return this.ivaDiferenciado;
    }

    public void setIvaDiferenciado(List<IvaDiferenteCeroReporte> ivaDiferenciado) {
        this.ivaDiferenciado = ivaDiferenciado;
    }
}
