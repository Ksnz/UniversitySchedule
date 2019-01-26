* Контейнер сервлетов - Tomcat (версия 8 и выше)
* Java EE 8
* Java SE 1.8
* Бд - POSTGRES 10-11
* JPA povider - Eclipse Link
* Система сборки Maven.
* Система контроля верий - Git`

Клиентская сторона на выбор:
Freemarker template, **jsp**, jsf, thymeleaf, mustache

### Бизнес-задача:

Реализовать систему расписания университета (если в задании что то, недосказано - то проявить фантазия и реализовать бизнес логику на свой вкус).

Сущности: преподаватель, студент, группа, факультет, аудтория, предмет.
Необходимо иметь возможность просматривать расписание для определенных групп и преподавателей, менять это расписание, добавлять новые группы и преподавателей,
убирать и добавлять студентов из групп (странички для CRUD операций по всем сущностям). На этих страничках вводить данные можно из клавиатуры, либо загрузить файл (на основании которого будут вставлены данные в таблицы).

Авторизацию делать не надо, представляем что с системой работает только преподаватель.


Предметы, имена преподавателей и студентов можно придумать или взять откуда то готовые

Для запуска
```
git clone
mvn install
mvn cargo:run
```