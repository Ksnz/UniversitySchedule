package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.GroupDAO;
import com.github.index.schedule.data.dao.LecturerDAO;
import com.github.index.schedule.data.dao.ScheduleEntryDAO;
import com.github.index.schedule.data.entity.ScheduleEntry;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.StringUtils.isNullOrEmpty;

@WebServlet(
        name = "UserServlet",
        urlPatterns = "/view/userschedule")
public class UserServlet extends HttpServlet {

    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    static Logger LOGGER = Logger.getLogger(UserServlet.class);


    private static final int PER_PAGE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        GroupDAO groupDAO = new GroupDAO(entityManager);
        String path = "userschedule.jsp";
        LecturerDAO lecturerDAO = new LecturerDAO(entityManager);
        ScheduleEntryDAO scheduleEntryDAO = new ScheduleEntryDAO(entityManager);
        String action = request.getParameter("action");
        Optional<Integer> id = getParameterIfPresent(request, "id", Integer.class);
        if ("group".equalsIgnoreCase(action)) {
            id.ifPresent(integer -> groupDAO.find(integer).ifPresent(groupEntry -> {
                List<ScheduleEntry> schedulesForGroup = scheduleEntryDAO.findForGroup(groupEntry);
                schedulesForGroup.sort(Comparator.comparing(ScheduleEntry::getDayOfWeek));
                request.setAttribute("schedules", schedulesForGroup);
            }));
        } else if ("lecturer".equalsIgnoreCase(action)) {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String patronymic = request.getParameter("patronymic");
            if (isNullOrEmpty(firstName) || isNullOrEmpty(lastName) || isNullOrEmpty(patronymic)) {
                request.setAttribute("message", "Некорректное значение имени: " + firstName + " фамилии: " + lastName + " или отчества: " + patronymic);
                path = "error.jsp";
            } else {
                lecturerDAO.findBy(firstName, lastName, patronymic).ifPresent(lecturer -> {
                    List<ScheduleEntry> schedulesForGroup = scheduleEntryDAO.findForLecturer(lecturer);
                    schedulesForGroup.sort(Comparator.comparing(ScheduleEntry::getDayOfWeek));
                    request.setAttribute("schedules", schedulesForGroup);
                });
            }
        }
        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
        entityManager.close();
    }

}
