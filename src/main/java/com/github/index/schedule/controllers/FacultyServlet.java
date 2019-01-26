package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.FacultyDAO;
import com.github.index.schedule.data.entity.Faculty;
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
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.data.utils.StringUtils.isNullOrEmpty;


@WebServlet(
        name = "FacultyServlet",
        urlPatterns = "/view/faculties")
public class FacultyServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    static Logger LOGGER = Logger.getLogger(FacultyServlet.class);

    private int pageNumber = 1;
    private int pageCount = 1;
    private static final int PER_PAGE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        FacultyDAO dao = new FacultyDAO(entityManager);
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
            RequestDispatcher view = request.getRequestDispatcher("faculty.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("faculties", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("faculties.jsp");
            view.forward(request, response);
        }
        entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        FacultyDAO dao = new FacultyDAO(entityManager);
        String action = request.getParameter("action");
        //PrintWriter output = response.getWriter();
        String path = "faculties.jsp";
        if (action != null) {
            Optional<Character> characterId;
            String facultyId = request.getParameter("facultyId");
            if (facultyId != null && !facultyId.isEmpty()) {
                characterId = Optional.of(facultyId.charAt(0));
            } else {
                characterId = Optional.empty();
            }
            if (action.equalsIgnoreCase("delete")) {
                characterId.ifPresent(character -> dao.find(character).ifPresent(dao::deleteFaculty));
            } else if (action.equalsIgnoreCase("edit")) {
                characterId.ifPresent(character -> dao.find(character).ifPresent(faculty -> {
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
                        Optional<Faculty> faculty = dao.find(characterId.get());
                        if (faculty.isPresent()) {
                            dao.updateFaculty(faculty.get(), characterId.get(), shortName, fullName);
                        } else {
                            dao.createFaculty(characterId.get(), shortName, fullName);
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код факультета: " + facultyId);
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (characterId.isPresent()) {
                    Optional<Faculty> facultyOptional = dao.find(characterId.get());
                    facultyOptional.ifPresent(faculty1 -> {
                        try (ServletOutputStream out = response.getOutputStream()) {

                            JAXBContext jaxbContext = JAXBContext.newInstance(Faculty.class);

                            Marshaller marshaller = jaxbContext.createMarshaller();
                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            marshaller.marshal(faculty1, out);
                            response.setContentType("application/xml");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + "faculty" + facultyId + ".xml");
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
                    JAXBContext jaxbContext = JAXBContext.newInstance(Faculty.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Faculty faculty;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            faculty = (Faculty) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Faculty> old = dao.find(faculty.getId());
                        if (old.isPresent()) {
                            dao.update(faculty);
                        } else {
                            dao.put(faculty);
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
            request.setAttribute("faculties", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
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