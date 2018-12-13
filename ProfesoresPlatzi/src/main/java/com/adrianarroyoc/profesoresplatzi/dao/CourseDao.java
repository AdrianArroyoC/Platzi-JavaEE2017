package com.adrianarroyoc.profesoresplatzi.dao;

import java.util.List;

import com.adrianarroyoc.profesoresplatzi.model.Course;

public interface CourseDao {

	void saveCourse(Course course);
	
	void deleteCourseById(Long idCourse);
	
	void updateCourse(Course course);

	List<Course> findAllCourses();
	
	Course findCourseById(Long idCourse);
	
	Course findCourseByName(String name);
	
	List<Course> findByIdTeacher(Long idTeacher);
	
}
