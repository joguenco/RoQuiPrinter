package dev.joguenco.serialize;

import ec.gob.sri.invoice.v210.Factura;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class Invoice {

  private final String pathXmlFile;

  public Invoice(String pathXmlFile) {
    this.pathXmlFile = pathXmlFile;
  }

  public Factura xmlToObject() {
    File file = new File(pathXmlFile);

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Factura.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      return (Factura) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
