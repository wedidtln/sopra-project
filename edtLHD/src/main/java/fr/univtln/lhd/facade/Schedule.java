package fr.univtln.lhd.facade;

import fr.univtln.lhd.model.entities.dao.slots.SlotDAO;
import fr.univtln.lhd.model.entities.dao.users.AdminDAO;
import fr.univtln.lhd.model.entities.dao.users.ProfessorDAO;
import fr.univtln.lhd.model.entities.dao.users.StudentDAO;
import fr.univtln.lhd.model.entities.slots.Group;
import fr.univtln.lhd.model.entities.users.Admin;
import fr.univtln.lhd.model.entities.users.Professor;
import fr.univtln.lhd.model.entities.users.Student;
import fr.univtln.lhd.model.entities.slots.Slot;
import lombok.extern.slf4j.Slf4j;
import org.threeten.extra.Interval;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * facade of the entities/dao and the ihm
 */
@Slf4j
public class Schedule {
    private static final ZoneId TIME_ZONE = ZoneOffset.systemDefault();
    private static final Schedule schedule = new Schedule();

    /**
     * Take a student and two LocalDate(start/end) and return every slot that the student is
     * in in the timerange of the two LocalDate
     * The student must be persisted or will return an empty array
     *
     * @param student   a persisted Student
     * @param timeStart the start of the timerange
     * @param timeEnd   the end of the timerange
     * @return a List of slot that is in the timerange and with the Groups of the Student
     */
    public static List<Slot> getSchedule(Student student, LocalDate timeStart, LocalDate timeEnd) {
        Interval timerange = schedule.getIntervalOf(timeStart, timeEnd);
        return Schedule.getSchedule(student, timerange);
    }


    /**
     * Take a student and an Interval timerange and return every slot within the timerange
     * where the student is in
     *
     * @param student   a persisted Student
     * @param timerange An Interval
     * @return a List of slot that is in the timerange and with the Groups of the Student
     */
    public static List<Slot> getSchedule(Student student, Interval timerange) {
        List<Slot> slotList = new ArrayList<>();
        if (student.getId() < 0) {
            return slotList;
        }//Might be an edge case
        SlotDAO dao = SlotDAO.getInstance();
        for (Group group :
                student.getStudentGroup()) {
            try {
                List<Slot> allSlotOfGroup = dao.getSlotOfGroup(group);
                slotList.addAll(allSlotOfGroup);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        slotList = schedule.filterEverySlotOutsideTimerange(slotList, timerange);
        return slotList;
    }


    /**
     * Take a Group and two LocalDate(start/end) and return every slot that the group is
     * in, in the timerange of the two LocalDate
     * The group must be persisted or will return an empty array
     *
     * @param group     a persisted group
     * @param timeStart the start of the timerange
     * @param timeEnd   the end of the timerange
     * @return a List of slot that is in the timerange with this group
     */
    public static List<Slot> getSchedule(Group group, LocalDate timeStart, LocalDate timeEnd) {
        Interval timerange = schedule.getIntervalOf(timeStart, timeEnd);
        return Schedule.getSchedule(group, timerange);
    }

    /**
     * Take a Group and an Interval and return a List of the slot with this group
     * inside the interval
     * The group must be persisted or will return an empty array
     *
     * @param group     a persisted group
     * @param timerange an Interval
     * @return a List of slot that is in the timerange and with this group
     */
    public static List<Slot> getSchedule(Group group, Interval timerange) {
        if (group.getId() < 0) {
            return new ArrayList<>();
        }//Might be an edge case
        SlotDAO dao = SlotDAO.getInstance();
        List<Slot> allSlotOfGroup = new ArrayList<>();
        try {
            allSlotOfGroup = dao.getSlotOfGroup(group);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return schedule.filterEverySlotOutsideTimerange(allSlotOfGroup, timerange);
    }

    /**
     * Get Student of Database via email & password
     * @param email Email of the student
     * @param password password of the student
     * @return Student entity if exist in database, otherwise return null.
     */
    public static Student getStudentFromAuth(String email, String password){
        StudentDAO dao = StudentDAO.getInstance();
        Student student = null;

        try {
            student = dao.get(email, password).orElseThrow(SQLException::new);
        } catch (SQLException e){
            log.error(e.getMessage());
        }
        return student;
    }

    /**
     * Get Professor of Database via email & password
     * @param email Email of the professor
     * @param password password of the professor
     * @return Professor entity if exist in database, otherwise return null.
     */
    public static Professor getProfessorFromAuth(String email, String password){
        ProfessorDAO dao = ProfessorDAO.of();
        Professor professor = null;

        try {
            professor = dao.get(email, password).orElseThrow(SQLException::new);
        } catch (SQLException e){
            log.error(e.getMessage());
        }
        return professor;
    }

    /**
     * Get Admin of Database via email & password
     * @param email Email of the admin
     * @param password password of the admin
     * @return Admin entity if exist in database, otherwise return null.
     */
    public static Admin getAdminFromAuth(String email, String password){
        AdminDAO dao = AdminDAO.of();
        Admin admin = null;

        try {
            admin = dao.get(email, password).orElseThrow(SQLException::new);
        } catch (SQLException e){
            log.error(e.getMessage());
        }
        return admin;
    }


    /**
     * Take a list of Slot and return another List of slot inside the timerange
     *
     * @param slotList  list of all the slot
     * @param timerange an Intervalle of the slot to keep
     * @return all the slot of slotList that was inside the timerange
     */
    private List<Slot> filterEverySlotOutsideTimerange(List<Slot> slotList, Interval timerange) {
        List<Slot> slotIntimerange = new ArrayList<>();
        if (slotList.isEmpty()) {
            return slotIntimerange;
        }
        for (Slot slot :
                slotList) {
            if (timerange.encloses(slot.getTimeRange())) {
                slotIntimerange.add(slot);
            }
        }
        return slotIntimerange;
    }

    /**
     * Take two LocalDate and return an Interval between the two time
     * to be noted LocalDate doesn't have a time therefor each instant
     * start at the beginning of the day 00h00
     * If the two time are on the same day, the whole day is return
     *
     * @param timeStart the starting LocalDate
     * @param timeEnd   the ending LocalDate
     * @return Interval between the
     */
    private Interval getIntervalOf(LocalDate timeStart, LocalDate timeEnd) {
        Instant start = timeStart.atStartOfDay().atZone(TIME_ZONE).toInstant();
        Instant end = timeEnd.atStartOfDay().atZone(TIME_ZONE).toInstant();
        if (start == end) {
            end = timeEnd.atTime(23, 59, 59).atZone(TIME_ZONE).toInstant();
        }
        return Interval.of(start, end);
    }

}
