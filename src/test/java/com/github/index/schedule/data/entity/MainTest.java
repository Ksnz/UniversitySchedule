package com.github.index.schedule.data.entity;

import com.github.index.schedule.converters.LocalDateAttributeConverter;
import com.github.index.schedule.data.dao.*;
import com.github.index.schedule.utils.TransactionUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@RunWith(Arquillian.class)
public class MainTest {
    @Deployment
    public static JavaArchive createDeployment() {

        JavaArchive[] libs = Maven.resolver().loadPomFromFile("pom.xml").importCompileAndRuntimeDependencies().resolve().withTransitivity().as(JavaArchive.class);
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class)
                .addPackage(StudentDAO.class.getPackage())
                .addPackage(LocalDateAttributeConverter.class.getPackage())
                .addPackage(Student.class.getPackage())
                .addPackage(TransactionUtils.class.getPackage())
                .addPackage(Student.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        for (JavaArchive lib : libs) {
            lib.delete("META-INF" + ArchivePath.SEPARATOR_STRING + "ECLIPSE_.RSA");
            lib.delete("META-INF" + ArchivePath.SEPARATOR_STRING + "ECLIPSE_.SF");
            javaArchive = javaArchive.merge(lib);
        }
        return javaArchive;
    }

    EntityManager entityManager;

    @org.junit.Before
    public void setUp() throws Exception {
        EntityManagerFactory f = Persistence.createEntityManagerFactory("SchedulePersistenceUnitTest");
        entityManager = f.createEntityManager();
        AuditoriumDAO auditoriumDAO = new AuditoriumDAO();
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 6; j++) {
                auditoriumDAO.createAuditorium(i, j, 20 + i * j);
            }
        }
        FacultyDAO facultyDAO = new FacultyDAO();
        facultyDAO.createFaculty('1', "ФКП", "Факультет компьютерного проектирования");
        facultyDAO.createFaculty('2', "ФИТУ", "Факультет информационных технологий и управления");
        facultyDAO.createFaculty('3', "ВФ", "Военный факультет");
        facultyDAO.createFaculty('4', "ФРЭ", "Факультет радиотехники и электроники");
        facultyDAO.createFaculty('5', "ФКСИС", "Факультет компьютерных систем и сетей");
        facultyDAO.createFaculty('6', "ФТК", "Факультет телекоммуникаций");
        facultyDAO.createFaculty('7', "ИЭФ", "Инженерно-экономический факультет");
        facultyDAO.createFaculty('8', "ИИТ", "Институт информационных технологий");
        facultyDAO.createFaculty('9', "ФНДО", "Факультет непрерывного и дистанционного обучения");
        facultyDAO.createFaculty('0', "ФЗО", "Факультет заочного обучения");

        GroupDAO groupDAO = new GroupDAO();
        groupDAO.createGroup(502441, facultyDAO.find('0').get());
        groupDAO.createGroup(502442, facultyDAO.find('0').get());
        groupDAO.createGroup(502443, facultyDAO.find('0').get());
        groupDAO.createGroup(552001, facultyDAO.find('5').get());
        groupDAO.createGroup(552002, facultyDAO.find('5').get());
        groupDAO.createGroup(552003, facultyDAO.find('5').get());

        CourseDAO courseDAO = new CourseDAO();
        courseDAO.createCourse(0, "ППВИС", "Программирование программ в интеллектуальных средах");
        courseDAO.createCourse(11, "ПБЗ", "Проектирование баз знаний");
        courseDAO.createCourse(2, "ВМ", "Высшая математика");
        courseDAO.createCourse(3, "ОАИП", "Основы алгоритмизации и программирования");

        StudentDAO studentDAO = new StudentDAO();
        studentDAO.createStudent(5024411, "Алексей", "Ксёнжик", "Геннадьевич", LocalDate.of(1992, 2, 6), groupDAO.find(502441).get());
        studentDAO.createStudent(5024412, "Екатерина", "Ксёнжик", "Геннадьевна", LocalDate.of(1995, 3, 11), groupDAO.find(502441).get());


        LecturerDAO lecturerDAO = new LecturerDAO();
        lecturerDAO.createLecturer("Иван", "Иванов", "Иванович", LocalDate.of(1976, 6, 5));
        lecturerDAO.createLecturer("Петр", "Петров", "Петрович", LocalDate.of(1977, 1, 2));

        ScheduleEntryDAO dao = new ScheduleEntryDAO();
        dao.createScheduleEntry(auditoriumDAO.findAll().get(0), courseDAO.findAll().get(2), DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(10, 0), (byte) 1, new HashSet<>(), lecturerDAO.findAll().get(0));

        ScheduleEntry scheduleEntry = dao.findAll().get(0);
        List<Group> all = groupDAO.findAll();
        dao.addGroup(scheduleEntry, all.get(0));
        dao.addGroup(scheduleEntry, all.get(1));

    }

    @Test
    public void newStudent() throws InterruptedException {

        ScheduleEntryDAO dao = new ScheduleEntryDAO();

        System.out.println("Расписание:");
        dao.findAll().forEach(scheduleEntry -> System.out.println(scheduleEntry.toString() + scheduleEntry.getGroups()));
        GroupDAO groupDAO = new GroupDAO();

        System.out.println("Группы");
        groupDAO.findAll().forEach(group -> System.out.println(group));

//        FacultyDAO facultyDAO = new FacultyDAO(entityManager);
//        facultyDAO.findAll().forEach(faculty -> {
//            facultyDAO.deleteFaculty(faculty);
//        });
//
//        System.out.println("Группы теперь");
//        groupDAO.findAll().forEach(group -> System.out.println(group));
//
//        System.out.println("Расписание теперь:");
//        dao.findAll().forEach(scheduleEntry -> System.out.println(scheduleEntry.getGroups()));

    }

    @After
    public void tearDown() throws Exception {
    }
}
