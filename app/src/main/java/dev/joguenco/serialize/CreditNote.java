package dev.joguenco.serialize;

import ec.gob.sri.note.credit.v110.NotaCredito;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;

public class CreditNote {

  private final String pathXmlFile;

  public CreditNote(String pathXmlFile) {
    this.pathXmlFile = pathXmlFile;
  }

  public NotaCredito xmlToObject() {
    File file = new File(pathXmlFile);

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(NotaCredito.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      return (NotaCredito) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }
}
