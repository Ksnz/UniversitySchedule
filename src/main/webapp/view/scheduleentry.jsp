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
    <script type="text/javascript" src="js/jquery.ui.timepicker.js"></script>
    <title>Добавить новое расписание</title>
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
        $('input[name=startTime]').timepicker({
            showPeriodLabels: false,
        });
        $('input[name=endTime]').timepicker({
            showPeriodLabels: false,
        });
    })
    ;
</script>
<div class="main">
    <form method="POST" action='scheduleentries?action=create'>
        <div class="field">
            <label>Код:</label>
            <input readonly
                   type="text" name="scheduleentryId" maxlength="8"
                   value="<c:out value="${scheduleentry.id}" />"/>
        </div>
        <div class="field">
            <label>Код преподавателя :</label>
            <input required pattern="[0-9]+"
                   type="text" name="lecturerId" maxlength="16"
                   value="<c:out value="${scheduleentry.lecturer.lecturerId}" />"/>
        </div>
        <div class="field">
            <label>Аудитория :</label>
            <input required pattern="[0-9]+"
                   type="text" name="auditoriumIdRoom" maxlength="3"
                   value="<c:out value="${scheduleentry.auditorium.room}" />"/>
        </div>
        <div class="field">
            <label>Корпус :</label>
            <input required pattern="[0-9]+"
                   type="text" name="auditoriumIdHousing" maxlength="1"
                   value="<c:out value="${scheduleentry.auditorium.housing}" />"/>
        </div>
        <div class="field">
            <label>Код предмета :</label>
            <input required pattern="[0-9]+"
                   type="text" name="courseId" maxlength="8"
                   value="<c:out value="${scheduleentry.course.id}" />"/>
        </div>
        <div class="field">
            <label>День недели :</label>
            <select name="dayOfWeek">
                <option value="<c:out value="${scheduleentry.dayOfWeek}"/>" selected hidden><c:out value="${scheduleentry.dayOfWeek}"/></option>
                <option value="MONDAY">MONDAY</option>
                <option value="TUESDAY">TUESDAY</option>
                <option value="WEDNESDAY">WEDNESDAY</option>
                <option value="THURSDAY">THURSDAY</option>
                <option value="FRIDAY">FRIDAY</option>
                <option value="SATURDAY">SATURDAY</option>
                <option value="SUNDAY">SUNDAY</option>
            </select>
        </div>
        <div class="field">
            <label>Номер недели :</label>
            <input required pattern="[1-4]"
                   type="text" name="weekNumber" maxlength="1"
                   value="<c:out value="${scheduleentry.weekNumber}" />"/>
        </div>
        <div class="field">
            <label>Группы через запятую :</label>
            <input required
                   type="text" name="groups"
                   value="<c:forEach var="group" items="${scheduleentry.groups}"><c:out value="${group.groupId},"/></c:forEach>"/>
        </div>
        <div class="field">
            <label>
                Время начала :</label>
            <input required
                   type="text" name="startTime" readonly
                    <fmt:parseDate value="${ scheduleentry.startTime }" pattern="HH:mm"
                                   var="parsedStartDateTime"
                                   type="time"/>
                   value="<fmt:formatDate pattern="HH:mm" value="${ parsedStartDateTime }" />"/>
        </div>
        <div class="field">
            <div class="field">
                <label>
                    Время окончания :</label>
                <input required
                       type="text" name="endTime" readonly
                        <fmt:parseDate value="${ scheduleentry.endTime }" pattern="HH:mm"
                                       var="parsedEndDateTime"
                                       type="time"/>
                       value="<fmt:formatDate pattern="HH:mm" value="${ parsedEndDateTime }" />"/>
            </div>
        </div>
        <div class="field">
            <input
                    type="submit" value="Отправить"/>
        </div>
    </form>
    <form action="scheduleentries?action=upload" method="post" enctype="multipart/form-data">
        Добавить из файла: <input type="file" name="scheduleentry" accept=".xml" multiple/>
        <input type="submit" value="Загрузить"/>
    </form>
</div>
</body>
</html>