package dev.joguenco.pdf;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class DetailsReport {
  @Getter @Setter private String codigoPrincipal;

  @Getter @Setter private String codigoAuxiliar;

  @Getter @Setter private String cantidad;

  @Getter @Setter private String descripcion;

  @Getter @Setter private BigDecimal precioUnitario;

  @Getter @Setter private String precioTotalSinImpuesto;

  @Getter @Setter private String descuento;

  @Getter @Setter private String numeroComprobante;

  @Getter @Setter private String nombreComprobante;

  @Getter @Setter private String detalle1;

  @Getter @Setter private String detalle2;

  @Getter @Setter private String detalle3;

  @Getter @Setter private String fechaEmisionCcompModificado;

  @Getter @Setter private BigDecimal precioSinSubsidio;

  @Getter @Setter private List<AdditionalInformation> infoAdicional;

  @Getter @Setter private List<PayMethod> formasPago;

  @Getter @Setter private List<TotalReceipts> totalesComprobante;

  @Getter @Setter private String razonModificacion;

  @Getter @Setter private String valorModificacion;

  @Getter @Setter private String baseImponible;

  @Getter @Setter private String nombreImpuesto;

  @Getter @Setter private String porcentajeRetener;

  @Getter @Setter private String valorRetenido;
}
