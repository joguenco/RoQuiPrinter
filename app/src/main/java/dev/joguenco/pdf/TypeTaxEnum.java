package dev.joguenco.pdf;

public enum TypeTaxEnum {
  RENTA(1, "Impuesto a la Renta"),
  IVA(2, "I.V.A."),
  ICE(3, "I.C.E."),
  IRBPNR(5, "IRBPNR");

  private int code;
  private String descripcion;

  private TypeTaxEnum(int code, String descripcion) {
    this.code = code;
    this.descripcion = descripcion;
  }

  public String getDescripcion() {
    return this.descripcion;
  }

  public int getCode() {
    return this.code;
  }
}
