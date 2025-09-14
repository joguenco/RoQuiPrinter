package dev.joguenco.pdf;

import lombok.Getter;
import lombok.Setter;

public class PayMethod {
  @Getter @Setter private String valor;

  @Getter @Setter private String formaPago;

  public PayMethod(String formaPago, String valor) {
    this.valor = valor;
    this.formaPago = formaPago;
  }
}
