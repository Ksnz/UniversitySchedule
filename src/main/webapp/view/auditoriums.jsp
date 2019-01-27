<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Аудитории</title>
    <link type="text/css"
          href="css/jquery-ui.min.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui.min.js"></script>
</head>
<body>
<table border=1>
    <thead>
    <tr>
        <th>Номер аудитории</th>
        <th>Номер корпуса</th>
        <th>Вместимость</th>
        <th colspan=3>Действие</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${auditoriums}" var="auditorium">
        <tr>
            <td><c:out value="${auditorium.room}"/></td>
            <td><c:out value="${auditorium.housing}"/></td>
            <td><c:out value="${auditorium.capacity}"/></td>
            <td>
                <form action="auditoriums?action=edit&auditoriumIdRoom=<c:out value="${auditorium.room}"/>&auditoriumIdHousing=<c:out value="${auditorium.housing}"/>"
                      method="post">
                    <input
                            type="submit" value="Обновить"/></form>
            </td>
            <td>
                <form action="auditoriums?action=delete&auditoriumIdRoom=<c:out value="${auditorium.room}"/>&auditoriumIdHousing=<c:out value="${auditorium.housing}"/>"
                      method="post">
                    <input
                            type="submit" value="Удалить"/></form>
            </td>
            <td>
                <form action="auditoriums?action=serialize&auditoriumIdRoom=<c:out value="${auditorium.room}"/>&auditoriumIdHousing=<c:out value="${auditorium.housing}"/>"
                      method="post"><input
                        type="submit" value="В файл"/></form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
Страница:
<c:forEach var="i" begin="1" end="${pageCount}" varStatus="loop">
    <button onclick="location.href='auditoriums?page=<c:out value="${loop.count}"/>'"
            <c:if test="${loop.count == pageNumber}">
                disabled
            </c:if>
    ><c:out value="${loop.count}"/></button>
</c:forEach>
<br>
<button onclick="location.href='auditoriums?action=insert'">Добавить аудиторию</button>
<hr>
<button onclick="location.href='../'">На главную</button>
</body>
</html>