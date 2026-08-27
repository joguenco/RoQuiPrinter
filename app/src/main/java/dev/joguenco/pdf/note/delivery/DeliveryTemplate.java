package dev.joguenco.pdf.note.delivery;

import dev.joguenco.util.ReportUtil;
import ec.gob.sri.note.delivery.v110.Destinatario;
import ec.gob.sri.note.delivery.v110.Detalle;
import ec.gob.sri.note.delivery.v110.GuiaRemision;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryTemplate {

    @Getter private GuiaRemision guiaRemision;
    private String nombreComprobante;
    private String numDocSustento;
    private String fechaEmisionSustento;
    private String numeroAutorizacion;
    private String motivoTraslado;
    private String destino;
    private String rucDestinatario;
    private String razonSocial;
    private String docAduanero;
    private String codigoEstab;
    private String ruta;
    private List<DetalleGuiaReporte> detalles;
    private List<DeliveryTemplate> guiaRemisionList;

    public DeliveryTemplate(GuiaRemision guiaRemision) {
        this.guiaRemision = guiaRemision;
    }

    public DeliveryTemplate() {}

    public List<DeliveryTemplate> getGuiaRemisionList() {
        this.guiaRemisionList = new ArrayList();
        for (Destinatario dest : this.guiaRemision.getDestinatarios().getDestinatario()) {
            DeliveryTemplate gr = new DeliveryTemplate();
            gr.setNombreComprobante(ReportUtil.getNameOfDocument(dest.getCodDocSustento()));
            gr.setNumDocSustento(dest.getNumDocSustento());
            gr.setFechaEmisionSustento(dest.getFechaEmisionDocSustento());
            gr.setNumeroAutorizacion(dest.getNumAutDocSustento());
            gr.setMotivoTraslado(dest.getMotivoTraslado());
            gr.setDestino(dest.getDirDestinatario());
            gr.setRucDestinatario(dest.getIdentificacionDestinatario());
            gr.setRazonSocial(dest.getRazonSocialDestinatario());
            gr.setDocAduanero(dest.getDocAduaneroUnico());
            gr.setCodigoEstab(dest.getCodEstabDestino());
            gr.setRuta(dest.getRuta());
            gr.setDetalles(obtenerDetalles(dest));

            this.guiaRemisionList.add(gr);
        }

        return this.guiaRemisionList;
    }

    private List<DetalleGuiaReporte> obtenerDetalles(Destinatario dest) {
        List list = new ArrayList();
        for (Detalle detalle : dest.getDetalles().getDetalle()) {
            DetalleGuiaReporte dgr = new DetalleGuiaReporte();
            dgr.setCantidad(detalle.getCantidad().toPlainString());
            dgr.setDescripcion(detalle.getDescripcion());
            dgr.setCodigoPrincipal(detalle.getCodigoInterno());
            dgr.setCodigoAuxiliar(detalle.getCodigoAdicional());
            list.add(dgr);
        }
        return list;
    }
}
