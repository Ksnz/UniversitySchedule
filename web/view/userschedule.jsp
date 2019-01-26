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
    <style>
        .field {
            clear: both;
            text-align: right;
        }

        label {
            float: left;
        }

        .main {
            float: left
        }
    </style>
</head>
<body>
<div class="main">
    <div <c:if test="${not empty schedules}">hidden</c:if>>
        <h3>Расписание по коду группы</h3>
        <form method="GET" action='userschedule'>
            <div class="field">
                <label>Номер группы:</label>
                <input required pattern="[0-9]+"
                       type="text" name="id" maxlength="8"
                       value=""/>
                <input hidden name="action" value="group"/>
            </div>
            <div class="field"><input
                    type="submit" value="Отправить"/>
            </div>
        </form>
        <h3>Расписание по имени фамилии отчеству преподавателя</h3>
        <form method="GET" action='userschedule'>
            <div class="field">
                <label>Имя :</label>
                <input required pattern="[A-Za-zА-Яа-яЁё ]+"
                       type="text" name="firstName" maxlength="16"
                       value="<c:out value="${lecturer.firstName}" />"/>
            </div>
            <div class="field">
                <label>Фамилия :</label>
                <input required pattern="[A-Za-zА-Яа-яЁё ]+"
                       type="text" name="lastName" maxlength="16"
                       value="<c:out value="${lecturer.lastName}" />"/>
            </div>
            <div class="field">
                <label>Отчество :</label>
                <input required pattern="[A-Za-zА-Яа-яЁё ]+"
                       type="text" name="patronymic" maxlength="16"
                       value="<c:out value="${lecturer.patronymic}" />"/>
            </div>
            <input hidden name="action" value="lecturer"/>
            <div class="field"><input
                    type="submit" value="Отправить"/>
            </div>
        </form>
    </div>
    <table border=1 <c:if test="${empty schedules}">hidden</c:if>>
        <thead>
        <tr>
            <th>Код</th>
            <th>Преподаватель</th>
            <th>Аудитория</th>
            <th>Корпус</th>
            <th>Предмет</th>
            <th>Коды групп</th>
            <th>День недели</th>
            <th>Номер недели</th>
            <th>Начало</th>
            <th>Конец</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${schedules}" var="scheduleentry">
            <tr>
                <td><c:out value="${scheduleentry.id}"/></td>
                <c:set var="ln" value="${scheduleentry.lecturer.lastName}"/>
                <c:set var="f" value="${fn:substring(scheduleentry.lecturer.lastName,0,1)}"/>
                <c:set var="p" value="${fn:substring(scheduleentry.lecturer.patronymic,0,1)}"/>
                <td><c:out value="${ln} ${f}. ${p}."/></td>
                <td><c:out value="${scheduleentry.auditorium.room}"/></td>
                <td><c:out value="${scheduleentry.auditorium.housing}"/></td>
                <td><c:out value="${scheduleentry.course.shortName}"/></td>
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
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <hr>
    <button onclick="location.href='../'">На главную</button>
</div>
</body>
</html>