package dev.joguenco.pdf.withhold;

import dev.joguenco.pdf.*;
import dev.joguenco.util.ReportUtil;
import ec.gob.sri.withhold.v200.ComprobanteRetencion;
import ec.gob.sri.withhold.v200.DocSustento;
import ec.gob.sri.withhold.v200.Retencion;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class WithholdTemplate {

    @Getter private ComprobanteRetencion retencion;
    private List<AdditionalInformation> infoAdicional;

    private static final String IVA = "IVA";
    private static final String RENTA = "RENTA";
    private static final String ICE = "ICE";
    private static final String ISD = "IMPUESTO A LA SALIDA DE DIVISAS";

    public WithholdTemplate(ComprobanteRetencion retencion) {
        this.retencion = retencion;
    }

    public List<DetailsReport> getDetallesAdiciones() {
        List<DetailsReport> detallesAdiciones = new ArrayList<>();

        for (DocSustento sus : retencion.getDocsSustento().getDocSustento()) {
            for (Retencion ret : sus.getRetenciones().getRetencion()) {

                DetailsReport detAd = new DetailsReport();

                detAd.setDescripcion(ret.getCodigoRetencion().toString());
                detAd.setBaseImponible(ret.getBaseImponible().toString());
                detAd.setPorcentajeRetener(ret.getPorcentajeRetener().toString());
                detAd.setValorRetenido(ret.getValorRetenido().toString());
                detAd.setNombreImpuesto(obtenerImpuestoDecripcion(ret.getCodigo()));
                detAd.setInfoAdicional(getInfoAdicional());
                detAd.setNumeroComprobante(sus.getNumDocSustento());
                detAd.setNombreComprobante(ReportUtil.getNameOfDocument(sus.getCodDocSustento()));
                detAd.setFechaEmisionCcompModificado(sus.getFechaEmisionDocSustento());
                detallesAdiciones.add(detAd);
            }
        }

        return detallesAdiciones;
    }

    public List<AdditionalInformation> getInfoAdicional() {
        if (getRetencion().getInfoAdicional() != null) {
            this.infoAdicional = new ArrayList();
            if ((getRetencion().getInfoAdicional().getCampoAdicional() != null)
                    && (!this.retencion.getInfoAdicional().getCampoAdicional().isEmpty())) {
                for (ComprobanteRetencion.InfoAdicional.CampoAdicional ca :
                        getRetencion().getInfoAdicional().getCampoAdicional()) {
                    this.infoAdicional.add(
                            new AdditionalInformation(ca.getValue(), ca.getNombre()));
                }
            }
        }
        return this.infoAdicional;
    }

    private String obtenerImpuestoDecripcion(String codigoImpuesto) {
        if (codigoImpuesto.equals("1")) {
            return RENTA;
        }
        if (codigoImpuesto.equals("2")) {
            return IVA;
        }
        if (codigoImpuesto.equals("3")) {
            return ICE;
        }
        if (codigoImpuesto.equals("6")) {
            return ISD;
        }
        return null;
    }
}
