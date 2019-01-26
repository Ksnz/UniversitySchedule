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
    <title>Добавить новый факультет</title>
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
    <form accept-charset=utf-8 method="POST" action='faculties?action=create'>
        <div class="field">
            <label>Код факультета :</label>
            <input required pattern="[0-9a-zA-Z]+" type="text" name="facultyId" maxlength="1"
                   value="<c:out value="${faculty.id}"/>"/>
        </div>
        <div class="field">
            <label>
                Аббревиатура : </label>
            <input required pattern="[A-Za-zА-Яа-яЁё]+" type="text" name="shortName" maxlength="8"
                   value="<c:out value="${faculty.shortName}"/>"/>
        </div>
        <div class="field">
            <label>
                Полное название :</label>
            <input required pattern="[A-Za-zА-Яа-яЁё ]+" type="text" name="fullName"
                   value="<c:out value="${faculty.fullName}"/>"/>
        </div>
        <div class="field">
            <input type="submit" value="Отправить"/>
        </div>
    </form>
    <form action="faculties?action=upload" method="post" enctype="multipart/form-data">
        Добавить из файла: <input type="file" name="faculty" accept=".xml" multiple/>
        <input type="submit" value="Загрузить"/>
    </form>
</div>
</body>
</html>