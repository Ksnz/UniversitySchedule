package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.*;
import com.github.index.schedule.data.entity.*;
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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.index.schedule.data.utils.StringUtils.isNullOrEmpty;

@WebServlet(
        name = "ScheduleEntryServlet",
        urlPatterns = "/view/scheduleentries")
public class ScheduleEntryServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    private static Logger LOGGER = Logger.getLogger(ScheduleEntryServlet.class);
    private int pageCount = 1;
    private int pageNumber = 1;
    private static final int PER_PAGE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ScheduleEntryDAO dao = new ScheduleEntryDAO(entityManager);
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
            RequestDispatcher view = request.getRequestDispatcher("scheduleentry.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("scheduleentries", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("scheduleentries.jsp");
            view.forward(request, response);
        }
        entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ScheduleEntryDAO dao = new ScheduleEntryDAO(entityManager);
        String action = request.getParameter("action");
        String path = "scheduleentries.jsp";
        if (action != null) {
            Optional<Integer> scheduleId;
            String scheduleentryIdvalue = request.getParameter("scheduleentryId");
            if (scheduleentryIdvalue != null && !scheduleentryIdvalue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(scheduleentryIdvalue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид расписания", e);
                }
                scheduleId = Optional.ofNullable(id);
            } else {
                scheduleId = Optional.empty();
            }
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
            Optional<AuditoriumKey> auditoriumKey;
            String auditoriumIdRoomvalue = request.getParameter("auditoriumIdRoom");
            String auditoriumIdHousingvalue = request.getParameter("auditoriumIdHousing");
            if (auditoriumIdRoomvalue != null && !auditoriumIdRoomvalue.isEmpty() && auditoriumIdHousingvalue != null && !auditoriumIdHousingvalue.isEmpty()) {
                Integer id1 = null;
                Integer id2 = null;
                try {
                    id1 = Integer.parseInt(auditoriumIdRoomvalue);
                    id2 = Integer.parseInt(auditoriumIdHousingvalue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид аудитории", e);
                }
                if (id1 != null && id2 != null) {
                    AuditoriumKey key = new AuditoriumKey();
                    key.setRoom(id1);
                    key.setHousing(id2);
                    auditoriumKey = Optional.of(key);
                } else {
                    auditoriumKey = Optional.empty();
                }
            } else {
                auditoriumKey = Optional.empty();
            }
            Optional<Integer> courseId;
            String courseIdvalue = request.getParameter("courseId");
            if (courseIdvalue != null && !courseIdvalue.isEmpty()) {
                Integer id = null;
                try {
                    id = Integer.parseInt(courseIdvalue);
                } catch (Exception e) {
                    LOGGER.warn("Ошибка парсинга ид предмета", e);
                }
                courseId = Optional.ofNullable(id);
            } else {
                courseId = Optional.empty();
            }
            if (action.equalsIgnoreCase("delete")) {
                scheduleId.ifPresent(entry -> dao.find(entry).ifPresent(dao::deleteScheduleEntry));
            } else if (action.equalsIgnoreCase("edit")) {
                scheduleId.ifPresent(entry -> dao.find(entry).ifPresent(scheduleentry -> {
                    request.setAttribute("scheduleentry", scheduleentry);
                }));
                path = "scheduleentry.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (lecturerId.isPresent() && courseId.isPresent() && auditoriumKey.isPresent()) {
                    AuditoriumDAO auditoriumDAO = new AuditoriumDAO(entityManager);
                    CourseDAO courseDAO = new CourseDAO(entityManager);
                    GroupDAO groupDAO = new GroupDAO(entityManager);
                    LecturerDAO lecturerDAO = new LecturerDAO(entityManager);
                    Optional<Auditorium> auditorium = auditoriumDAO.find(auditoriumKey.get());
                    Optional<Course> course = courseDAO.find(courseId.get());
                    Optional<Lecturer> lecturer = lecturerDAO.find(lecturerId.get());
                    String groups = request.getParameter("groups");
                    Set<Group> groupSet = new HashSet<>();
                    if (!isNullOrEmpty(groups)) {
                        String[] split = groups.split(",");
                        groupSet.addAll(Arrays.stream(split).map(String::trim).filter(s -> !s.isEmpty()).mapToInt(value -> {
                            try {
                                return Integer.parseInt(value);
                            } catch (Exception e) {
                                LOGGER.warn("Ошибка парсинга группы:" + value, e);
                            }
                            return -1;
                        }).filter(value -> value > 0).mapToObj(groupDAO::find).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet()));
                    }
                    DayOfWeek dayOfWeek;
                    LocalTime startTime;
                    LocalTime endTime;
                    byte weekNumber;
                    String dayOfWeekValue = request.getParameter("dayOfWeek");
                    String startTimeValue = request.getParameter("startTime");
                    String endTimeValue = request.getParameter("endTime");
                    String weekNumberValue = request.getParameter("weekNumber");
                    try {
                        dayOfWeek = DayOfWeek.valueOf(dayOfWeekValue);
                        startTime = LocalTime.parse(startTimeValue);
                        endTime = LocalTime.parse(endTimeValue);
                        weekNumber = Byte.parseByte(weekNumberValue);
                    } catch (Exception e) {
                        request.setAttribute("message", "Неправильное значение дня недели: " + dayOfWeekValue + " времени начала: " + startTimeValue + " времени конца: " + endTimeValue + " или недели:" + weekNumberValue + ". В общем ошибка: " + e.getLocalizedMessage());
                        path = "error.jsp";
                        RequestDispatcher view = request.getRequestDispatcher(path);
                        view.forward(request, response);
                        entityManager.close();
                        return;
                    }
                    if (scheduleId.isPresent()) {
                        if (auditorium.isPresent() && course.isPresent() && lecturer.isPresent()) {
                            Optional<ScheduleEntry> scheduleEntry = dao.find(scheduleId.get());
                            if (scheduleEntry.isPresent()) {
                                dao.updateScheduleEntry(scheduleEntry.get(), auditorium.get(), course.get(), dayOfWeek, startTime, endTime, weekNumber, groupSet, lecturer.get());
                            } else {
                                dao.createScheduleEntry(auditorium.get(), course.get(), dayOfWeek, startTime, endTime, weekNumber, groupSet, lecturer.get());
                            }
                        }
                    } else {
                        if (auditorium.isPresent() && course.isPresent() && lecturer.isPresent()) {
                            dao.createScheduleEntry(auditorium.get(), course.get(), dayOfWeek, startTime, endTime, weekNumber, groupSet, lecturer.get());
                        } else if (!auditorium.isPresent()) {
                            request.setAttribute("message", "Нет такой аудитории: " + auditoriumKey.get());
                            path = "error.jsp";
                        } else if (!course.isPresent()) {
                            request.setAttribute("message", "Нет такого предмета: " + courseId.get());
                            path = "error.jsp";
                        } else {
                            request.setAttribute("message", "Нет такого преподавателя: " + lecturerId.get());
                            path = "error.jsp";
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код аудитории: " + auditoriumIdRoomvalue + " корпуса: " + auditoriumIdHousingvalue + " преподавателя: " + lecturerIdvalue + " или предмета: " + courseIdvalue);
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (scheduleId.isPresent()) {
                    Optional<ScheduleEntry> scheduleentryOptional = dao.find(scheduleId.get());
                    scheduleentryOptional.ifPresent(scheduleentry1 -> {
                        try (ServletOutputStream out = response.getOutputStream()) {

                            JAXBContext jaxbContext = JAXBContext.newInstance(ScheduleEntry.class);

                            Marshaller marshaller = jaxbContext.createMarshaller();
                            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                            marshaller.marshal(scheduleentry1, out);
                            response.setContentType("application/xml");
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + "schedule" + scheduleId.get() + ".xml");
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
                    JAXBContext jaxbContext = JAXBContext.newInstance(ScheduleEntry.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        ScheduleEntry scheduleEntry;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            scheduleEntry = (ScheduleEntry) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<ScheduleEntry> old = dao.find(scheduleEntry.getId());
                        if (old.isPresent()) {
                            dao.update(scheduleEntry);
                        } else {
                            dao.put(scheduleEntry);
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
        if (path.equals("scheduleentries.jsp")) {
            request.setAttribute("scheduleentries", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }

        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
        entityManager.close();
    }
}