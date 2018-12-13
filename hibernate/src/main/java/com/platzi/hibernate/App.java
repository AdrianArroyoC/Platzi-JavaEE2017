package com.platzi.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.platzi.hibernate.dao.TeacherDaoImpl;
import com.platzi.hibernate.model.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello!" );
        
        Teacher teacher = new Teacher("Christian3", "url/c3");
        TeacherDaoImpl teacherDaoImpl = new TeacherDaoImpl();
        teacherDaoImpl.saveTeacher(teacher);
        List<Teacher> teachers = teacherDaoImpl.findAllTeachers();
        for (Teacher t : teachers) {
			System.out.println("Id: " + t.getIdTeacher() + " Nombre: " + t.getName());
		}
        System.out.println(teacherDaoImpl.findTeacherById((long)7).getName());
        //teacherDaoImpl.deleteTeacherById((long) 6);
    }
}
