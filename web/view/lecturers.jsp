<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Преподаватели</title>
    <link type="text/css"
          href="css/jquery-ui.min.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui.min.js"></script>
</head>
<body>
<table border=1>
    <thead>
    <tr>
        <th>Код преподавателя</th>
        <th>Имя</th>
        <th>Фамилия</th>
        <th>Отчество</th>
        <th>День рождения</th>
        <th colspan=3>Действие</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${lecturers}" var="lecturer">
        <tr>
            <td><c:out value="${lecturer.lecturerId}"/></td>
            <td><c:out value="${lecturer.firstName}"/></td>
            <td><c:out value="${lecturer.lastName}"/></td>
            <td><c:out value="${lecturer.patronymic}"/></td>
            <fmt:parseDate value="${lecturer.birthDay}" pattern="yyyy-MM-dd"
                           var="parsedDate" type="date"/>
            <td><fmt:formatDate pattern="yyyy-MM-dd" value="${parsedDate}" type="date"/></td>
            <td>
                <form action="lecturers?action=edit&lecturerId=<c:out value="${lecturer.lecturerId}"/>" method="post"><input
                        type="submit" value="Обновить"/></form>
            </td>
            <td>
                <form action="lecturers?action=delete&lecturerId=<c:out value="${lecturer.lecturerId}"/>" method="post">
                    <input
                            type="submit" value="Удалить"/></form>
            </td>
            <td>
                <form action="lecturers?action=serialize&lecturerId=<c:out value="${lecturer.lecturerId}"/>" method="post">
                    <input
                            type="submit" value="В файл"/></form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
Страница:
<c:forEach var="i" begin="1" end="${pageCount}" varStatus="loop">
    <button onclick="location.href='lecturers?page=<c:out value="${loop.count}"/>'"
            <c:if test="${loop.count == pageNumber}">
                disabled
            </c:if>
    ><c:out value="${loop.count}"/></button>
</c:forEach>
<br>
<button onclick="location.href='lecturers?action=insert'">Добавить преподавателя</button>
<hr>
<button onclick="location.href='../'">На главную</button>
</body>
</html>