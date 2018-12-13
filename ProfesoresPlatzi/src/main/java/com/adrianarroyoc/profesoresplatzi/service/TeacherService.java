package com.adrianarroyoc.profesoresplatzi.service;

import java.util.List;

import com.adrianarroyoc.profesoresplatzi.model.Teacher;

public interface TeacherService {

	void saveTeacher(Teacher teacher);
	
	void deleteTeacherById(Long idTeacher);
	
	void updateTeacher(Teacher teacher);

	List<Teacher> findAllTeachers();
	
	Teacher findTeacherById(Long idTeacher);
	
	Teacher findTeacherByName(String name);
	
}
