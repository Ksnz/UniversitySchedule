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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.data.utils.StringUtils.isNullOrEmpty;


@WebServlet(
        name = "StudentServlet",
        urlPatterns = "/view/students")
public class StudentServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    private static Logger LOGGER = Logger.getLogger(StudentServlet.class);
    private int pageCount = 1;
    private int pageNumber = 1;
    private static final int PER_PAGE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        StudentDAO dao = new StudentDAO(entityManager);
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
            RequestDispatcher view = request.getRequestDispatcher("student.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("students", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("students.jsp");
            view.forward(request, response);
        }
        entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        StudentDAO dao = new StudentDAO(entityManager);
        GroupDAO groupDao = new GroupDAO(entityManager);
        String action = request.getParameter("action");
        String path = "students.jsp";
        if (action != null) {
            Optional<Integer> studentId;
            Optional<Integer> groupId;
            String studentIdvalue = request.getParameter("studentId");
            String groupIdvalue = request.getParameter("groupId");
            if (studentIdvalue != null && !studentIdvalue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(studentIdvalue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид студента", e);
                }
                studentId = Optional.ofNullable(id);
            } else {
                studentId = Optional.empty();
            }
            if (groupIdvalue != null && !groupIdvalue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(groupIdvalue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид студента", e);
                }
                groupId = Optional.ofNullable(id);
            } else {
                groupId = Optional.empty();
            }
            if (action.equalsIgnoreCase("delete")) {
                studentId.ifPresent(character -> dao.find(character).ifPresent(dao::deleteStudent));
            } else if (action.equalsIgnoreCase("edit")) {
                studentId.ifPresent(character -> dao.find(character).ifPresent(student -> {
                    request.setAttribute("student", student);
                }));
                path = "student.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (studentId.isPresent() && groupId.isPresent()) {
                    Optional<Student> student = dao.find(studentId.get());
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
                                dao.updateStudent(student.get(), studentId.get(), firstName, lastName, patronymic, birthDay, group.get());
                            } else {
                                group.ifPresent(presentedGroup -> dao.createStudent(studentId.get(), firstName, lastName, patronymic, birthDay, presentedGroup));
                            }
                        } catch (DateTimeParseException e) {
                            request.setAttribute("message", "Некорректное значение даты рождения: " + birthDayValue);
                            path = "error.jsp";
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код группы: " + groupIdvalue + " или номер студенческого: " + studentIdvalue);
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (studentId.isPresent()) {
                    Optional<Student> studentOptional = dao.find(studentId.get());
                    studentOptional.ifPresent(student1 -> {
                        try (ServletOutputStream out = response.getOutputStream()) {

                            JAXBContext jaxbContext = JAXBContext.newInstance(Student.class);

                            Marshaller marshaller = jaxbContext.createMarshaller();
                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            marshaller.marshal(student1, out);
                            response.setContentType("application/xml");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + "student" + studentId.get() + ".xml");
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
                    JAXBContext jaxbContext = JAXBContext.newInstance(Student.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Student student;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            student = (Student) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Student> old = dao.find(student.getStudentId());
                        if (old.isPresent()) {
                            dao.update(student);
                        } else {
                            dao.put(student);
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
            request.setAttribute("students", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }

        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
        entityManager.close();
    }
}