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
import java.util.List;
import java.util.Optional;

import static com.github.index.schedule.utils.OtherUtils.getParameterIfPresent;
import static com.github.index.schedule.utils.XmlUtils.marshalEntity;


@WebServlet(
        name = "GroupServlet",
        urlPatterns = "/view/groups")
public class GroupServlet extends HttpServlet {

    private static Logger LOGGER = Logger.getLogger(GroupServlet.class);

    private int pageNumber = 1;
    private static final int PER_PAGE = 5;
    private int pageCount = 1;

    @Inject
    GroupDAO groupDAO;

    @Inject
    FacultyDAO facultyDao;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long count = groupDAO.count();
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
            request.setAttribute("groups", groupDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
            RequestDispatcher view = request.getRequestDispatcher("groups.jsp");
            view.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String path = "groups.jsp";
        if (action != null) {
            Optional<Integer> groupId = getParameterIfPresent(request, "groupId", Integer.class);
            Optional<Character> facultyId = getParameterIfPresent(request, "facultyId", Character.class);
            if (action.equalsIgnoreCase("delete")) {
                groupId.ifPresent(character -> groupDAO.find(character).ifPresent(groupDAO::delete));
            } else if (action.equalsIgnoreCase("edit")) {
                request.setAttribute("edit", true);
                groupId.ifPresent(character -> groupDAO.find(character).ifPresent(group -> {
                    request.setAttribute("group", group);
                }));
                path = "group.jsp";
            } else if (action.equalsIgnoreCase("create")) {
                if (groupId.isPresent() && facultyId.isPresent()) {
                    Optional<Group> group = groupDAO.find(groupId.get());
                    Optional<Faculty> faculty = facultyDao.find(facultyId.get());
                    if (group.isPresent() && faculty.isPresent()) {
                        groupDAO.updateGroup(group.get(), groupId.get(), faculty.get());
                    } else {
                        if (!faculty.isPresent()) {
                            request.setAttribute("message", "Несуществующий код факультета: " + facultyId.get());
                            path = "error.jsp";
                        } else {
                            groupDAO.createGroup(groupId.get(), faculty.get());
                        }
                    }
                } else {
                    request.setAttribute("message", "Неправильный код группы: " + request.getParameter("groupId") + " или факультета: " + request.getParameter("facultyId"));
                    path = "error.jsp";
                }
            } else if (action.equalsIgnoreCase("serialize")) {
                if (groupId.isPresent()) {
                    Optional<Group> groupOptional = groupDAO.find(groupId.get());
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
                        Optional<Group> old = groupDAO.find(group.getGroupId());
                        if (old.isPresent()) {
                            groupDAO.update(group);
                        } else {
                            groupDAO.put(group);
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
            request.setAttribute("groups", groupDAO.findIn((pageNumber - 1) * PER_PAGE, PER_PAGE));
            request.setAttribute("pageNumber", pageNumber);
            request.setAttribute("pageCount", pageCount);
        }

        RequestDispatcher view = request.getRequestDispatcher(path);
        view.forward(request, response);
    }
}