package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.AuditoriumDAO;
import com.github.index.schedule.data.entity.Auditorium;
import com.github.index.schedule.data.entity.AuditoriumKey;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;


@WebServlet(
        name = "AuditoriumServlet",
        urlPatterns = "/view/auditoriums")
public class AuditoriumServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    static Logger LOGGER = Logger.getLogger(AuditoriumServlet.class);

    private int pageNumber = 1;
    private int pageCount;
    private static final int PER_PAGE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        AuditoriumDAO dao = new AuditoriumDAO(entityManager);
        long count = dao.count();
        String pageParameter = request.getParameter("page");
        if (count > 0) {
            count--;
        }
        pageCount = (int) (count / PER_PAGE + 1);
        if (pageParameter != null) {
            try {
                pageNumber = Integer.parseInt(pageParameter);
            } catch (Exception e) {
                LOGGER.warn("Ошибка парсинга номера страницы", e);
            }
        }
        if (pageNumber > pageCount) {
            pageNumber = pageCount;
        }
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        String action = request.getParameter("action");
        if ("insert".equalsIgnoreCase(action)) {
            RequestDispatcher view = request.getRequestDispatcher("auditorium.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("auditoriums", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("auditoriums.jsp");
            view.forward(request, response);
        }
        entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        AuditoriumDAO dao = new AuditoriumDAO(entityManager);
        String action = request.getParameter("action");
        String path = "auditoriums.jsp";
        if (action != null) {
            Optional<Integer> auditoriumRoom;
            String auditoriumIdRoomValue = request.getParameter("auditoriumIdRoom");
            if (auditoriumIdRoomValue != null && !auditoriumIdRoomValue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(auditoriumIdRoomValue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид комнаты аудитории", e);
                }
                auditoriumRoom = Optional.ofNullable(id);
            } else {
                auditoriumRoom = Optional.empty();
            }
            Optional<Integer> auditoriumHousing;
            String auditoriumIdHousingValue = request.getParameter("auditoriumIdHousing");
            if (auditoriumIdHousingValue != null && !auditoriumIdHousingValue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(auditoriumIdHousingValue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид корпуса аудитории", e);
                }
                auditoriumHousing = Optional.ofNullable(id);
            } else {
                auditoriumHousing = Optional.empty();
            }
            if (action.equalsIgnoreCase("delete")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    dao.find(key).ifPresent(dao::deleteAuditorium);
                }
            } else if (action.equalsIgnoreCase("edit")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    dao.find(key).ifPresent(auditorium -> {
                        request.setAttribute("auditorium", auditorium);
                    });
                    path = "auditorium.jsp";
                }
            } else if (action.equalsIgnoreCase("create")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    int capacity = -1;
                    try {
                        capacity = Integer.parseInt(request.getParameter("capacity"));
                    } catch (Exception e) {
                        LOGGER.warn("Ошибка парсинга вместимости аудитории", e);
                    }
                    if (capacity > 0) {
                        Optional<Auditorium> auditorium = dao.find(key);
                        if (auditorium.isPresent()) {
                            dao.updateAuditorium(auditorium.get(), capacity);
                        } else {
                            dao.createAuditorium(key.getRoom(), key.getHousing(), capacity);
                        }
                    } else {
                        request.setAttribute("message", "Неправильное значение вместимости: " + request.getParameter("capacity"));
                        path = "error.jsp";
                    }
                } else {
                    request.setAttribute("message", "Неправильное значение номера корпуса: " + auditoriumIdHousingValue + " или аудитории: " + auditoriumIdRoomValue);
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    Optional<Auditorium> auditoriumOptional = dao.find(key);
                    auditoriumOptional.ifPresent(auditorium1 -> {
                        try (ServletOutputStream out = response.getOutputStream()) {

                            JAXBContext jaxbContext = JAXBContext.newInstance(Auditorium.class);

                            Marshaller marshaller = jaxbContext.createMarshaller();
                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            marshaller.marshal(auditorium1, out);
                            response.setContentType("application/xml");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + "auditorium" + auditoriumRoom.get() + ".xml");
                            out.flush();
                        } catch (IOException | JAXBException e) {
                            LOGGER.warn("Ошибка создания файла", e);
                        }
                    });
                }
            } else if (action.equalsIgnoreCase("upload")) {
                try {
                    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                    List<FileItem> fileItems = upload.parseRequest(request);
                    JAXBContext jaxbContext = JAXBContext.newInstance(Auditorium.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Auditorium auditorium;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            auditorium = (Auditorium) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        AuditoriumKey key = new AuditoriumKey();
                        key.setRoom(auditorium.getRoom());
                        key.setHousing(auditorium.getHousing());
                        Optional<Auditorium> old = dao.find(key);
                        if (old.isPresent()) {
                            dao.update(auditorium);
                        } else {
                            dao.put(auditorium);
                        }
                    }
                } catch (JAXBException | FileUploadException e) {
                    LOGGER.warn("Ошибка чтения загруженного файла", e);
                    request.setAttribute("message", "Ошибка чтения файла: " + e.getLocalizedMessage());
                    path = "error.jsp";
                    //path = "";
                }
            }
        }
        if (path.equals("auditoriums.jsp")) {
            request.setAttribute("auditoriums", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }
        if (!path.isEmpty()) {
            RequestDispatcher view = request.getRequestDispatcher(path);
            view.forward(request, response);
        }
        entityManager.close();
    }
}