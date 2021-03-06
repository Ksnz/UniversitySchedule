<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link type="text/css"
          href="css/jquery-ui.min.css" rel="stylesheet"/>
    <script type="text/javascript" src="js/jquery.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui.min.js"></script>
    <title>Добавить нового студента</title>
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
<script>
    $(function () {
        $('input[name=birthDay]').datepicker({
            changeMonth: true,
            changeYear: true,
            yearRange: '-100:+0',
            dateFormat: 'yy-mm-dd'
        });
    });
</script>
<div class="main">
    <form method="POST" action='students?action=create'>
        <div class="field">
            <label>Номер студенческого:</label>
            <input <c:if test="${not empty edit}">readonly</c:if> required pattern="[0-9]+"
                   type="text" name="studentId" maxlength="8"
                   value="<c:out value="${student.studentId}" />"/>
        </div>
        <div class="field">
            <label>Имя :</label>
            <input required pattern="[A-Za-zА-Яа-яЁё]+"
                   type="text" name="firstName" maxlength="16"
                   value="<c:out value="${student.firstName}" />"/>
        </div>
        <div class="field">
            <label>Фамилия :</label>
            <input required pattern="[A-Za-zА-Яа-яЁё]+"
                   type="text" name="lastName" maxlength="16"
                   value="<c:out value="${student.lastName}" />"/>
        </div>
        <div class="field">
            <label>Отчество :</label>
            <input required pattern="[A-Za-zА-Яа-яЁё]+"
                   type="text" name="patronymic" maxlength="16"
                   value="<c:out value="${student.patronymic}" />"/>
        </div>
        <div class="field">
            <label>
                День рождения :</label>
            <input required
                   type="text" name="birthDay" readonly
                    <fmt:parseDate value="${ student.birthDay }" pattern="yyyy-MM-dd" var="parsedDateTime"
                                   type="date"/>
                   value="<fmt:formatDate pattern="yyyy-MM-dd" value="${ parsedDateTime }" />"/>
        </div>
        <div class="field">
            <label>
                Группа :</label>
            <input required pattern="[0-9]+"
                   type="text" name="groupId"
                   value="<c:out value="${student.group.groupId}" />"/>
        </div>
        <div class="field"><input
                type="submit" value="Отправить"/>
        </div>
    </form>
    <form action="students?action=upload" method="post" enctype="multipart/form-data">
        Из файла: <input type="file" name="student" accept=".xml" multiple/>
        <input type="submit" value="Загрузить"/>
    </form>
</div>
</body>
</html>