package com.adrianarroyoc.profesoresplatzi.dao;

import java.util.List;

import com.adrianarroyoc.profesoresplatzi.model.Teacher;

public interface TeacherDao {
	
	void saveTeacher(Teacher teacher);
	
	void deleteTeacherById(Long idTeacher);
	
	void updateTeacher(Teacher teacher);

	List<Teacher> findAllTeachers();
	
	Teacher findTeacherById(Long idTeacher);
	
	Teacher findTeacherByName(String name);
	
}
