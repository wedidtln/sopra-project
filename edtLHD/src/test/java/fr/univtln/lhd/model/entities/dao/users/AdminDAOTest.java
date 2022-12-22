package fr.univtln.lhd.model.entities.dao.users;

import fr.univtln.lhd.exceptions.IdException;
import fr.univtln.lhd.model.entities.users.Admin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class AdminDAOTest {
    public static final AdminDAO dao = AdminDAO.of();

    private Admin getRandomNewAdmin(){
        Admin admin = Admin.of("UnitTest","UnitTestFirstName",
                "UnitTestName.Firstname"+Math.random()+"@email.com","ScienceUnique");//A admin is a new one if is email is new
        return admin;
    }

    
    private Admin getTheTestAdmin(){
        Admin admin = Admin.of("TheTestAdmin","UnitTestFirstName",
                "UnitTestName.Firstname@email.com","RandomSciences");
        return admin;
    }

    @Test
    void CreateADAO(){
        Assertions.assertNotNull(dao);
    }

    @Test
    void addNewAdmin(){
        Admin admin = getRandomNewAdmin();
        int oldsize = dao.getAll().size();
        dao.save(admin,"1234");
        assertEquals(oldsize+1,dao.getAll().size());
        dao.delete(admin);
    }

    @Test
    void updateAadmin() throws IdException {
        Admin admin = getRandomNewAdmin();
        Admin admin1 = Admin.of(admin.getName()+"1",admin.getFname()+"1",admin.getEmail()+"1", admin.getFaculty());
        dao.save(admin,"1234");
        admin1.setId(admin.getId());
        dao.update(admin1);
        assertEquals(dao.get(admin.getId()).get(),admin1);
    }

    @Test
    void addSameAdmin(){
        Admin admin = getTheTestAdmin();
        dao.save(admin,"1234");
        int oldsize = dao.getAll().size();
        dao.save(admin,"1234");
        assertEquals(oldsize,dao.getAll().size());
    }


    @Test
    void deleteTheAdmin(){
        Admin admin = getRandomNewAdmin();
        dao.save(admin,"1234");
        int oldsize = dao.getAll().size();
        dao.delete(admin);
        assertEquals(oldsize-1,dao.getAll().size());
    }

}