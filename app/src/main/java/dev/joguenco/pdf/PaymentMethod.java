package dev.joguenco.pdf;

import lombok.Getter;
import lombok.Setter;

public class PaymentMethod {
    @Getter @Setter private String valor;

    @Getter @Setter private String formaPago;

    public PaymentMethod(String formaPago, String valor) {
        this.valor = valor;
        this.formaPago = formaPago;
    }

    public static String getName(String code) {
        return switch (code) {
            case "01" -> "Sin utilización del sistema financiero";
            case "15" -> "Compensación de deudas";
            case "16" -> "Tarjeta de débito";
            case "17" -> "Dinero electrónico";
            case "18" -> "Tarjeta prepago";
            case "19" -> "Tarjeta de crédito";
            case "20" -> "Otros con utilización del sistema financiero";
            case "21" -> "Endoso de títulos";
            default -> code + " No definido";
        };
    }
}
