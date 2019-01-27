package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.*;
import com.github.index.schedule.data.entity.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
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
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.StringUtils.isNullOrEmpty;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;

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

    @Inject
    ScheduleEntryDAO scheduleEntryDAO;
    @Inject
    AuditoriumDAO auditoriumDAO;
    @Inject
    CourseDAO courseDAO;
    @Inject
    GroupDAO groupDAO;
    @Inject
    LecturerDAO lecturerDAO;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long count = scheduleEntryDAO.count();
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
            RequestDispatcher view = request.getRequestDispatcher("scheduleentry.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("scheduleentries", scheduleEntryDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("scheduleentries.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String path = "scheduleentries.jsp";
        if (action != null) {
            Optional<Integer> scheduleId = getParameterIfPresent(request, "scheduleentryId", Integer.class);
            Optional<Integer> lecturerId = getParameterIfPresent(request, "lecturerId", Integer.class);
            Optional<AuditoriumKey> auditoriumKey;
            Optional<Integer> auditoriumIdRoom = getParameterIfPresent(request, "auditoriumIdRoom", Integer.class);
            Optional<Integer> auditoriumIdHousing = getParameterIfPresent(request, "auditoriumIdHousing", Integer.class);
            if (auditoriumIdRoom.isPresent() && auditoriumIdHousing.isPresent()) {
                AuditoriumKey key = new AuditoriumKey();
                key.setRoom(auditoriumIdRoom.get());
                key.setHousing(auditoriumIdHousing.get());
                auditoriumKey = Optional.of(key);
            } else {
                auditoriumKey = Optional.empty();
            }
            Optional<Integer> courseId = getParameterIfPresent(request, "courseId", Integer.class);
            if (action.equalsIgnoreCase("delete")) {
                scheduleId.ifPresent(entry -> scheduleEntryDAO.find(entry).ifPresent(scheduleEntryDAO::deleteScheduleEntry));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                scheduleId.ifPresent(entry -> scheduleEntryDAO.find(entry).ifPresent(scheduleentry -> {
                    request.setAttribute("scheduleentry", scheduleentry);
                }));
                path = "scheduleentry.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (lecturerId.isPresent() && courseId.isPresent() && auditoriumKey.isPresent()) {
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
                        return;
                    }
                    if (scheduleId.isPresent()) {
                        if (auditorium.isPresent() && course.isPresent() && lecturer.isPresent()) {
                            Optional<ScheduleEntry> scheduleEntry = scheduleEntryDAO.find(scheduleId.get());
                            if (scheduleEntry.isPresent()) {
                                scheduleEntryDAO.updateScheduleEntry(scheduleEntry.get(), auditorium.get(), course.get(), dayOfWeek, startTime, endTime, weekNumber, groupSet, lecturer.get());
                            } else {
                                scheduleEntryDAO.createScheduleEntry(auditorium.get(), course.get(), dayOfWeek, startTime, endTime, weekNumber, groupSet, lecturer.get());
                            }
                        }
                    } else {
                        if (auditorium.isPresent() && course.isPresent() && lecturer.isPresent()) {
                            scheduleEntryDAO.createScheduleEntry(auditorium.get(), course.get(), dayOfWeek, startTime, endTime, weekNumber, groupSet, lecturer.get());
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
                    request.setAttribute("message", "Неправильный код аудитории: " + request.getParameter("auditoriumIdRoom") + " корпуса: " + request.getParameter("auditoriumIdHousing") + " преподавателя: " + request.getParameter("lecturerId") + " или предмета: " + request.getParameter("courseId"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (scheduleId.isPresent()) {
                    Optional<ScheduleEntry> scheduleentryOptional = scheduleEntryDAO.find(scheduleId.get());
                    scheduleentryOptional.ifPresent(scheduleentry1 -> {
                        String filename = "scheduleentry" + scheduleentry1.getId();
                        marshalEntity(response, scheduleentry1, filename);
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
                        Optional<ScheduleEntry> old = scheduleEntryDAO.find(scheduleEntry.getId());
                        if (old.isPresent()) {
                            scheduleEntryDAO.update(scheduleEntry);
                        } else {
                            scheduleEntryDAO.put(scheduleEntry);
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
            request.setAttribute("scheduleentries", scheduleEntryDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }

        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
    }
}