package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.LecturerDAO;
import com.github.index.schedule.data.entity.Lecturer;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.StringUtils.isNullOrEmpty;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;


@WebServlet(
        name = "LecturerServlet",
        urlPatterns = "/view/lecturers")
public class LecturerServlet extends HttpServlet {

    private static Logger LOGGER = Logger.getLogger(LecturerServlet.class);

    private int pageNumber = 1;
    private int pageCount = 1;
    private static final int PER_PAGE = 5;

    @Inject
    LecturerDAO lecturerDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long count = lecturerDAO.count();
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
            RequestDispatcher view = request.getRequestDispatcher("lecturer.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("lecturers", lecturerDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("lecturers.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String path = "lecturers.jsp";
        if (action != null) {
            Optional<Integer> lecturerId = getParameterIfPresent(request, "lecturerId", Integer.class);
            if (action.equalsIgnoreCase("delete")) {
                lecturerId.ifPresent(id -> lecturerDAO.find(id).ifPresent(lecturerDAO::delete));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                lecturerId.ifPresent(id -> lecturerDAO.find(id).ifPresent(lecturer -> {
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
                            Optional<Lecturer> lecturer = lecturerDAO.find(lecturerId.get());
                            if (lecturer.isPresent()) {
                                lecturerDAO.updateLecturer(lecturer.get(), lecturerId.get(), firstName, lastName, patronymic, birthDay);
                            } else {
                                lecturerDAO.createLecturer(firstName, lastName, patronymic, birthDay);
                            }
                        } else {
                            lecturerDAO.createLecturer(firstName, lastName, patronymic, birthDay);
                        }
                    } catch (DateTimeParseException e) {
                        request.setAttribute("message", "Некорректное значение даты рождения: " + birthDayValue);
                        path = "error.jsp";
                    }
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (lecturerId.isPresent()) {
                    Optional<Lecturer> lecturerOptional = lecturerDAO.find(lecturerId.get());
                    lecturerOptional.ifPresent(lecturer1 -> {
                        String filename = "lecturer" + lecturer1.getLecturerId();
                        marshalEntity(response, lecturer1, filename);
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
                        Optional<Lecturer> old = lecturerDAO.find(lecturer.getLecturerId());
                        if (old.isPresent()) {
                            lecturerDAO.update(lecturer);
                        } else {
                            lecturerDAO.put(lecturer);
                        }
                    }
                } catch (JAXBException | FileUploadException e) {
                    LOGGER.warn("Ошибка чтения загруженного файла", e);
                    request.setAttribute("message", "Ошибка чтения файла: " + e.getLocalizedMessage());
                    path = "error.jsp";
                }
            }
        }
        if (path.equals("lecturers.jsp")) {
            request.setAttribute("lecturers", lecturerDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }
        if (!path.isEmpty()) {
            RequestDispatcher view = request.getRequestDispatcher(path);
            view.forward(request, response);
        }
    }
}