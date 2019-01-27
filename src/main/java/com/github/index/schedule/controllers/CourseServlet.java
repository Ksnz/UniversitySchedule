package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.CourseDAO;
import com.github.index.schedule.data.entity.Course;
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
        name = "CourseServlet",
        urlPatterns = "/view/courses")
public class CourseServlet extends HttpServlet {

    private static Logger LOGGER = Logger.getLogger(CourseServlet.class);

    private int pageNumber = 1;
    private int pageCount;
    private static final int PER_PAGE = 5;

    @Inject
    CourseDAO courseDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long count = courseDAO.count();
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
            RequestDispatcher view = request.getRequestDispatcher("course.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("courses", courseDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("courses.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        //PrintWriter output = response.getWriter();
        String path = "courses.jsp";
        if (action != null) {
            Optional<Integer> courseId = getParameterIfPresent(request, "courseId", Integer.class);
            if (action.equalsIgnoreCase("delete")) {
                courseId.ifPresent(character -> courseDAO.find(character).ifPresent(courseDAO::deleteCourse));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                courseId.ifPresent(character -> courseDAO.find(character).ifPresent(course -> {
                    request.setAttribute("course", course);
                }));
                path = "course.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (courseId.isPresent()) {
                    String shortName = request.getParameter("shortName");
                    String fullName = request.getParameter("fullName");
                    if (isNullOrEmpty(shortName) || isNullOrEmpty(fullName)) {
                        request.setAttribute("message", "Пустое полное название предмета: " + shortName + " или его аббревитатура: " + fullName);
                        path = "error.jsp";
                    } else {
                        Optional<Course> course = courseDAO.find(courseId.get());
                        if (course.isPresent()) {
                            courseDAO.updateCourse(course.get(), courseId.get(), shortName, fullName);
                        } else {
                            courseDAO.createCourse(courseId.get(), shortName, fullName);
                        }
                    }
                } else {
                    request.setAttribute("message", "Указан неправильный код предмета: " + request.getParameter("courseId"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (courseId.isPresent()) {
                    Optional<Course> courseOptional = courseDAO.find(courseId.get());
                    courseOptional.ifPresent(course1 -> {
                        String filename = "course" + course1.getId();
                        marshalEntity(response, course1, filename);
                    });
                }
            } else if (action.equalsIgnoreCase("upload")) {
                try {
                    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                    List<FileItem> fileItems = upload.parseRequest(request);
                    JAXBContext jaxbContext = JAXBContext.newInstance(Course.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Course course;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            course = (Course) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Course> old = courseDAO.find(course.getId());
                        if (old.isPresent()) {
                            courseDAO.update(course);
                        } else {
                            courseDAO.put(course);
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
        if (path.equals("courses.jsp")) {
            request.setAttribute("courses", courseDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }
        if (!path.isEmpty()) {
            RequestDispatcher view = request.getRequestDispatcher(path);
            view.forward(request, response);
        }
    }
}