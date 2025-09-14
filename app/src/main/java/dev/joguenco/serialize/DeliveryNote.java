package dev.joguenco.serialize;

import ec.gob.sri.note.delivery.v110.GuiaRemision;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class DeliveryNote {

  private final String pathXmlFile;

  public DeliveryNote(String pathXmlFile) {
    this.pathXmlFile = pathXmlFile;
  }

  public GuiaRemision xmlToObject() {
    File file = new File(pathXmlFile);

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(GuiaRemision.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      return (GuiaRemision) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
