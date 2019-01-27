package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.GroupDAO;
import com.github.index.schedule.data.dao.StudentDAO;
import com.github.index.schedule.data.entity.Group;
import com.github.index.schedule.data.entity.Student;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.StringUtils.isNullOrEmpty;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;


@WebServlet(
        name = "StudentServlet",
        urlPatterns = "/view/students")
public class StudentServlet extends HttpServlet {

    private static Logger LOGGER = Logger.getLogger(StudentServlet.class);
    private int pageCount = 1;
    private int pageNumber = 1;
    private static final int PER_PAGE = 5;

    @Inject
    StudentDAO studentDAO;

    @Inject
    GroupDAO groupDao;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long count = studentDAO.count();
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
            RequestDispatcher view = request.getRequestDispatcher("student.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("students", studentDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("students.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String path = "students.jsp";
        if (action != null) {
            Optional<Integer> studentId = getParameterIfPresent(request, "studentId", Integer.class);
            Optional<Integer> groupId = getParameterIfPresent(request, "groupId", Integer.class);
            if (action.equalsIgnoreCase("delete")) {
                studentId.ifPresent(character -> studentDAO.find(character).ifPresent(studentDAO::deleteStudent));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                studentId.ifPresent(character -> studentDAO.find(character).ifPresent(student -> {
                    request.setAttribute("student", student);
                }));
                path = "student.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (studentId.isPresent() && groupId.isPresent()) {
                    Optional<Student> student = studentDAO.find(studentId.get());
                    Optional<Group> group = groupDao.find(groupId.get());
                    String firstName = request.getParameter("firstName");
                    String lastName = request.getParameter("lastName");
                    String patronymic = request.getParameter("patronymic");
                    if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(patronymic)) {
                        request.setAttribute("message", "Некорректное значение имени: " + firstName + " фамилии: " + lastName + " или отчества: " + patronymic);
                        path = "error.jsp";
                    } else {
                        String birthDayValue = request.getParameter("birthDay");
                        LocalDate birthDay;
                        try {
                            birthDay = LocalDate.parse(birthDayValue);
                            if (student.isPresent() && group.isPresent()) {
                                studentDAO.updateStudent(student.get(), studentId.get(), firstName, lastName, patronymic, birthDay, group.get());
                            } else {
                                group.ifPresent(presentedGroup -> studentDAO.createStudent(studentId.get(), firstName, lastName, patronymic, birthDay, presentedGroup));
                            }
                        } catch (DateTimeParseException e) {
                            request.setAttribute("message", "Некорректное значение даты рождения: " + birthDayValue);
                            path = "error.jsp";
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код группы: " + request.getParameter("groupId") + " или номер студенческого: " + request.getParameter("studentId"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                studentId.ifPresent(integer -> studentDAO.find(integer).ifPresent(student1 -> {
                    String filename = "student" + student1.getStudentId();
                    marshalEntity(response, student1, filename);
                }));
            } else if (action.equalsIgnoreCase("upload")) {
                try {
                    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                    List<FileItem> fileItems = upload.parseRequest(request);
                    JAXBContext jaxbContext = JAXBContext.newInstance(Student.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Student student;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            student = (Student) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Student> old = studentDAO.find(student.getStudentId());
                        if (old.isPresent()) {
                            studentDAO.update(student);
                        } else {
                            studentDAO.put(student);
                        }
                    }
                } catch (JAXBException | FileUploadException e) {
                    e.printStackTrace();
                    LOGGER.warn("Ошибка чтения загруженного файла", e);
                    request.setAttribute("message", "Ошибка чтения файла: " + e.getLocalizedMessage());
                    path = "error.jsp";
                }
            }
        }
        if (path.equals("students.jsp")) {
            request.setAttribute("students", studentDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }

        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
    }
}