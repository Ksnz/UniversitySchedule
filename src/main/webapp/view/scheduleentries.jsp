<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Расписания</title>
    <link type="text/css"
          href="css/jquery-ui.min.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui.min.js"></script>
</head>
<body>
<table border=1>
    <thead>
    <tr>
        <th>Код</th>
        <th>Код преподавателя</th>
        <th>Аудитория</th>
        <th>Корпус</th>
        <th>Код предмета</th>
        <th>Коды групп</th>
        <th>День недели</th>
        <th>Номер недели</th>
        <th>Начало</th>
        <th>Конец</th>
        <th colspan=3>Действие</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${scheduleentries}" var="scheduleentry">
        <tr>
            <td><c:out value="${scheduleentry.id}"/></td>
            <td><c:out value="${scheduleentry.lecturer.lecturerId}"/></td>
            <td><c:out value="${scheduleentry.auditorium.room}"/></td>
            <td><c:out value="${scheduleentry.auditorium.housing}"/></td>
            <td><c:out value="${scheduleentry.course.id}"/></td>
            <td><c:forEach var="group" items="${scheduleentry.groups}">
                <c:out value="${group.groupId} "/>
            </c:forEach></td>
            <td><c:out value="${scheduleentry.dayOfWeek}"/></td>
            <td><c:out value="${scheduleentry.weekNumber}"/></td>
            <fmt:parseDate value="${scheduleentry.startTime}" pattern="HH:mm"
                           var="parsedStartTime" type="time"/>
            <td><fmt:formatDate pattern="HH:mm" value="${parsedStartTime}" type="time"/></td>
            <fmt:parseDate value="${scheduleentry.endTime}" pattern="HH:mm"
                           var="parsedEndTime" type="time"/>
            <td><fmt:formatDate pattern="HH:mm" value="${parsedEndTime}" type="time"/></td>
            <td>
                <form action="scheduleentries?action=edit&scheduleentryId=<c:out value="${scheduleentry.id}"/>"
                      method="post"><input
                        type="submit" value="Обновить"/></form>
            </td>
            <td>
                <form action="scheduleentries?action=delete&scheduleentryId=<c:out value="${scheduleentry.id}"/>"
                      method="post">
                    <input
                            type="submit" value="Удалить"/></form>
            </td>
            <td>
                <form action="scheduleentries?action=serialize&scheduleentryId=<c:out value="${scheduleentry.id}"/>"
                      method="post">
                    <input
                            type="submit" value="В файл"/></form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
Страница:
<c:forEach var="i" begin="1" end="${pageCount}" varStatus="loop">
    <button onclick="location.href='scheduleentries?page=<c:out value="${loop.count}"/>'"
            <c:if test="${loop.count == pageNumber}">
                disabled
            </c:if>
    ><c:out value="${loop.count}"/></button>
</c:forEach>
<br>
<button onclick="location.href='scheduleentries?action=insert'">Добавить расписание</button>
<hr>
<button onclick="location.href='../'">На главную</button>
</body>
</html>