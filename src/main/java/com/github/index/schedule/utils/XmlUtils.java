package com.github.index.schedule.utils;

import com.github.index.schedule.data.dao.AbstractDAO;
import com.github.index.schedule.data.dao.AuditoriumDAO;
import com.github.index.schedule.data.entity.Auditorium;
import com.github.index.schedule.data.entity.AuditoriumKey;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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
