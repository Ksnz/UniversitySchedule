package com.github.index.schedule.controllers;

import com.github.index.schedule.data.dao.FacultyDAO;
import com.github.index.schedule.data.dao.GroupDAO;
import com.github.index.schedule.data.entity.Faculty;
import com.github.index.schedule.data.entity.Group;
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

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;


@WebServlet(
        name = "GroupServlet",
        urlPatterns = "/view/groups")
public class GroupServlet extends HttpServlet {

    //@PersistenceUnit(unitName = "SchedulePersistenceUnit")
    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("SchedulePersistenceUnit");
    private static Logger LOGGER = Logger.getLogger(GroupServlet.class);

    private int pageNumber = 1;
    private static final int PER_PAGE = 5;
    private int pageCount = 1;


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        GroupDAO dao = new GroupDAO(entityManager);
        long count = dao.count();
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
            RequestDispatcher view = request.getRequestDispatcher("group.jsp");
            view.forward(request, response);
        } else {
            request.setAttribute("groups", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("groups.jsp");
            view.forward(request, response);
        }
        entityManager.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        GroupDAO dao = new GroupDAO(entityManager);
        FacultyDAO facultyDao = new FacultyDAO(entityManager);
        String action = request.getParameter("action");
        String path = "groups.jsp";
        if (action != null) {
            Optional<Integer> groupId = getParameterIfPresent(request, "groupId", Integer.class);
            Optional<Character> facultyId = getParameterIfPresent(request, "facultyId", Character.class);
            if (action.equalsIgnoreCase("delete")) {
                groupId.ifPresent(character -> dao.find(character).ifPresent(dao::deleteGroup));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                groupId.ifPresent(character -> dao.find(character).ifPresent(group -> {
                    request.setAttribute("group", group);
                }));
                path = "group.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (groupId.isPresent() && facultyId.isPresent()) {
                    Optional<Group> group = dao.find(groupId.get());
                    Optional<Faculty> faculty = facultyDao.find(facultyId.get());
                    if (group.isPresent() && faculty.isPresent()) {
                        dao.updateGroup(group.get(), groupId.get(), faculty.get());
                    } else {
                        if (!faculty.isPresent()) {
                            request.setAttribute("message", "Несуществующий код факультета: " + facultyId.get());
                            path = "error.jsp";
                        } else {
                            dao.createGroup(groupId.get(), faculty.get());
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код группы: " + request.getParameter("groupId") + " или факультета: " + request.getParameter("facultyId"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (groupId.isPresent()) {
                    Optional<Group> groupOptional = dao.find(groupId.get());
                    groupOptional.ifPresent(group1 -> {
                        String filename = "group" + group1.getGroupId();
                        marshalEntity(response, group1, filename);
                    });
                }
            } else if (action.equalsIgnoreCase("upload")) {
                try {
                    ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                    List<FileItem> fileItems = upload.parseRequest(request);
                    JAXBContext jaxbContext = JAXBContext.newInstance(Group.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    for (FileItem fileItem : fileItems) {
                        Group group;
                        try (InputStream inputStream = fileItem.getInputStream()) {
                            group = (Group) jaxbUnmarshaller.unmarshal(inputStream);
                        }
                        Optional<Group> old = dao.find(group.getGroupId());
                        if (old.isPresent()) {
                            dao.update(group);
                        } else {
                            dao.put(group);
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
        if (path.equals("groups.jsp")) {
            request.setAttribute("groups", dao.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }

        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
        entityManager.close();
    }
}