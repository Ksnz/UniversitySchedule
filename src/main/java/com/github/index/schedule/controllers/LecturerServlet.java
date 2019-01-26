package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.LecturerDAO;
import com.github.index.schedule.data.entity.Lecturer;
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
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.data.utils.StringUtils.isNullOrEmpty;


@WebServlet(
        name = "LecturerServlet",
        urlPatterns = "/view/lecturers")
public class LecturerServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    static Logger LOGGER = Logger.getLogger(LecturerServlet.class);

    private int pageNumber = 1;
    private int pageCount = 1;
    private static final int PER_PAGE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        LecturerDAO dao = new LecturerDAO(entityManager);
        String pageParameter = request.getParameter("page");
        long count = dao.count();
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
            RequestDispatcher view = request.getRequestDispatcher("lecturer.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("lecturers", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("lecturers.jsp");
            view.forward(request, response);
        }
        entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        LecturerDAO dao = new LecturerDAO(entityManager);
        String action = request.getParameter("action");
        //PrintWriter output = response.getWriter();
        String path = "lecturers.jsp";
        if (action != null) {
            Optional<Integer> lecturerId;
            String lecturerIdvalue = request.getParameter("lecturerId");
            if (lecturerIdvalue != null && !lecturerIdvalue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(lecturerIdvalue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид преподавателя", e);
                }
                lecturerId = Optional.ofNullable(id);
            } else {
                lecturerId = Optional.empty();
            }
            if (action.equalsIgnoreCase("delete")) {
                lecturerId.ifPresent(id -> dao.find(id).ifPresent(dao::deleteLecturer));
            } else if (action.equalsIgnoreCase("edit")) {
                lecturerId.ifPresent(id -> dao.find(id).ifPresent(lecturer -> {
                    request.setAttribute("lecturer", lecturer);
                }));
                path = "lecturer.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                String firstName = request.getParameter("firstName");
                String lastName = request.getParameter("lastName");
                String patronymic = request.getParameter("patronymic");
                if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(patronymic)) {
                    request.setAttribute("message", "Некорректное значение имени: " + firstName + " фамилии: " + lastName + " или отчества: " + patronymic);
                    path = "error.jsp";
                } else {
                    String birthDayValue = request.getParameter("birthDay");
                    try {
                        LocalDate birthDay = LocalDate.parse(birthDayValue);
                        if (lecturerId.isPresent()) {
                            Optional<Lecturer> lecturer = dao.find(lecturerId.get());
                            if (lecturer.isPresent()) {
                                dao.updateLecturer(lecturer.get(), lecturerId.get(), firstName, lastName, patronymic, birthDay);
                            } else {
                                dao.createLecturer(firstName, lastName, patronymic, birthDay);
                            }
                        } else {
                            dao.createLecturer(firstName, lastName, patronymic, birthDay);
                        }
                    } catch (DateTimeParseException e) {
                        request.setAttribute("message", "Некорректное значение даты рождения: " + birthDayValue);
                        path = "error.jsp";
                    }
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (lecturerId.isPresent()) {
                    Optional<Lecturer> lecturerOptional = dao.find(lecturerId.get());
                    lecturerOptional.ifPresent(lecturer1 -> {
                        try (ServletOutputStream out = response.getOutputStream()) {

                            JAXBContext jaxbContext = JAXBContext.newInstance(Lecturer.class);

                            Marshaller marshaller = jaxbContext.createMarshaller();
                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            marshaller.marshal(lecturer1, out);
                            response.setContentType("application/xml");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + "lecturer" + lecturerId.get() + ".xml");
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
                    JAXBContext jaxbContext = JAXBContext.newInstance(Lecturer.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Lecturer lecturer;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            lecturer = (Lecturer) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Lecturer> old = dao.find(lecturer.getLecturerId());
                        if (old.isPresent()) {
                            dao.update(lecturer);
                        } else {
                            dao.put(lecturer);
                        }
                    }
                } catch (JAXBException | FileUploadException e) {
                    //output.println("Ошибка загрузки файла: " + e.getLocalizedMessage());
                    LOGGER.warn("Ошибка чтения загруженного файла", e);
                    request.setAttribute("message", "Ошибка чтения файла: " + e.getLocalizedMessage());
                    path = "error.jsp";
                    //path = "";
                }
            }
        }
        if (path.equals("lecturers.jsp")) {
            request.setAttribute("lecturers", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
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