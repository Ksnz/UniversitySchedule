package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.AuditoriumDAO;
import com.github.index.schedule.data.entity.Auditorium;
import com.github.index.schedule.data.entity.AuditoriumKey;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;

@WebServlet(
        name = "AuditoriumServlet",
        urlPatterns = "/view/auditoriums")
public class AuditoriumServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    //private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    private static Logger LOGGER = Logger.getLogger(AuditoriumServlet.class);

    private int pageNumber = 1;
    private int pageCount;
    private static final int PER_PAGE = 5;

    @Inject
    AuditoriumDAO auditoriumDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long count = auditoriumDAO.count();
        Optional<Integer> pageParameter = getParameterIfPresent(request, "page", Integer.class);
        if (count > 0) {
            count--;
        }
        pageCount = (int) (count / PER_PAGE + 1);
        pageParameter.ifPresent(integer -> pageNumber = integer);
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
            request.setAttribute("auditoriums", auditoriumDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("auditoriums.jsp");
            view.forward(request, response);
        }
        //entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        AuditoriumDAO auditoriumDAO = new AuditoriumDAO(entityManager);
        String action = request.getParameter("action");
        String path = "auditoriums.jsp";
        if (action != null) {
            Optional<Integer> auditoriumRoom = getParameterIfPresent(request, "auditoriumIdRoom", Integer.class);
            Optional<Integer> auditoriumHousing = getParameterIfPresent(request, "auditoriumIdHousing", Integer.class);
            if (action.equalsIgnoreCase("delete")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    auditoriumDAO.find(key).ifPresent(auditoriumDAO::deleteAuditorium);
                }
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    auditoriumDAO.find(key).ifPresent(auditorium -> {
                        request.setAttribute("auditorium", auditorium);
                    });
                    path = "auditorium.jsp";
                }
            } else if (action.equalsIgnoreCase("create")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    int capacity = getParameterIfPresent(request, "capacity", Integer.class).orElse(-1);
                    if (capacity > 0) {
                        Optional<Auditorium> auditorium = auditoriumDAO.find(key);
                        if (auditorium.isPresent()) {
                            auditoriumDAO.updateAuditorium(auditorium.get(), capacity);
                        } else {
                            auditoriumDAO.createAuditorium(key.getRoom(), key.getHousing(), capacity);
                        }
                    } else {
                        request.setAttribute("message", "Неправильное значение вместимости: " + request.getParameter("capacity"));
                        path = "error.jsp";
                    }
                } else {
                    request.setAttribute("message", "Неправильное значение номера аудитории: " + request.getParameter("auditoriumIdRoom") + " или корпуса: " + request.getParameter("auditoriumIdHousing"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (auditoriumRoom.isPresent() && auditoriumHousing.isPresent()) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(auditoriumRoom.get());
                    key.setHousing(auditoriumHousing.get());
                    Optional<Auditorium> auditoriumOptional = auditoriumDAO.find(key);
                    auditoriumOptional.ifPresent(auditorium1 -> {
                        String filename = "auditorium" + auditorium1.getRoom() + "-" + auditorium1.getHousing();
                        marshalEntity(response, auditorium1, filename);
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
                        Optional<Auditorium> old = auditoriumDAO.find(key);
                        if (old.isPresent()) {
                            auditoriumDAO.update(auditorium);
                        } else {
                            auditoriumDAO.put(auditorium);
                        }
                    }
                } catch (JAXBException | FileUploadException e) {
                    LOGGER.warn("Ошибка чтения загруженного файла", e);
                    request.setAttribute("message", "Ошибка чтения файла: " + e.getLocalizedMessage());
                    path = "error.jsp";
                }
            }
        }
        if (path.equals("auditoriums.jsp")) {
            request.setAttribute("auditoriums", auditoriumDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }
        if (!path.isEmpty()) {
            RequestDispatcher view = request.getRequestDispatcher(path);
            view.forward(request, response);
        }
        //entityManager.close();
    }
}