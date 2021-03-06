package com.github.index.schedule.utils;

import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;

public class XmlUtils {
    private static final Logger LOGGER = Logger.getLogger(XmlUtils.class);

    private XmlUtils() {
    }

    public static void marshalEntity(HttpServletResponse response, Object entity, String filename) {
        try (ServletOutputStream out = response.getOutputStream()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(entity.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(entity, out);
            response.setContentType("application/xml");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + ".xml");
            out.flush();
        } catch (IOException | JAXBException e) {
            LOGGER.warn("Ошибка создания файла", e);
        }
    }
}
