package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.FacultyDAO;
import com.github.index.schedule.data.entity.Faculty;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
import static com.github.index.schedule.utils.StringUtils.isNullOrEmpty;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;


@WebServlet(
        name = "FacultyServlet",
        urlPatterns = "/view/faculties")
public class FacultyServlet extends HttpServlet {


    private static Logger LOGGER = Logger.getLogger(FacultyServlet.class);

    private int pageNumber = 1;
    private int pageCount = 1;
    private static final int PER_PAGE = 5;

    @Inject
    FacultyDAO facultyDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long count = facultyDAO.count();
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
            RequestDispatcher view = request.getRequestDispatcher("faculty.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("faculties", facultyDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("faculties.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        //PrintWriter output = response.getWriter();
        String path = "faculties.jsp";
        if (action != null) {
            Optional<Character> characterId = getParameterIfPresent(request, "facultyId", Character.class);
            if (action.equalsIgnoreCase("delete")) {
                characterId.ifPresent(character -> facultyDAO.find(character).ifPresent(facultyDAO::deleteFaculty));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                characterId.ifPresent(character -> facultyDAO.find(character).ifPresent(faculty -> {
                    request.setAttribute("faculty", faculty);
                }));
                path = "faculty.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (characterId.isPresent()) {
                    String shortName = request.getParameter("shortName");
                    String fullName = request.getParameter("fullName");
                    if (isNullOrEmpty(shortName) || isNullOrEmpty(fullName)) {
                        request.setAttribute("message", "Пустое полное название факультета: " + shortName + " или его аббревитатура: " + fullName);
                        path = "error.jsp";
                    } else {
                        Optional<Faculty> faculty = facultyDAO.find(characterId.get());
                        if (faculty.isPresent()) {
                            facultyDAO.updateFaculty(faculty.get(), characterId.get(), shortName, fullName);
                        } else {
                            facultyDAO.createFaculty(characterId.get(), shortName, fullName);
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код факультета: " + request.getParameter("facultyId"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (characterId.isPresent()) {
                    Optional<Faculty> facultyOptional = facultyDAO.find(characterId.get());
                    facultyOptional.ifPresent(faculty1 -> {
                        String filename = "faculty" + faculty1.getId();
                        marshalEntity(response, faculty1, filename);
                    });
                }
            } else if (action.equalsIgnoreCase("upload")) {
                try {
                    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                    List<FileItem> fileItems = upload.parseRequest(request);
                    JAXBContext jaxbContext = JAXBContext.newInstance(Faculty.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Faculty faculty;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            faculty = (Faculty) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Faculty> old = facultyDAO.find(faculty.getId());
                        if (old.isPresent()) {
                            facultyDAO.update(faculty);
                        } else {
                            facultyDAO.put(faculty);
                        }
                    }
                } catch (JAXBException | FileUploadException e) {
                    //output.println("Ошибка загрузки файла: " + e.getLocalizedMessage());
                    LOGGER.warn("Ошибка чтения загруженного файла", e);
                    request.setAttribute("message", "Ошибка чтения файла: " + e.getLocalizedMessage());
                    path = "error.jsp";
                }
            }
        }
        if (path.equals("faculties.jsp")) {
            request.setAttribute("faculties", facultyDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }
        if (!path.isEmpty()) {
            RequestDispatcher view = request.getRequestDispatcher(path);
            view.forward(request, response);
        }
    }
}