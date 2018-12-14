package com.adrianarroyoc.profesoresplatzi.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.adrianarroyoc.profesoresplatzi.model.Course;
import com.adrianarroyoc.profesoresplatzi.service.CourseService;
import com.adrianarroyoc.profesoresplatzi.util.CustomErrorType;

@Controller
@RequestMapping("/v1")
public class CourseController {

	@Autowired
	CourseService _courseService;
	
	//GET
	@RequestMapping(value="/courses", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<Course>> getCourses(@RequestParam(value="name", required=false)String name){
		List<Course> courses = new ArrayList<>();
		courses = _courseService.findAllCourses();
		if (name == null) {
			if (courses.isEmpty()) {
				return new ResponseEntity(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<Course>>(courses, HttpStatus.OK);
		}
		Course course = _courseService.findCourseByName(name);
		if (course == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		courses.add(course);
		return new ResponseEntity<List<Course>>(courses, HttpStatus.OK);
	}
	
	//GET
	@RequestMapping(value="/courses/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Course> getCourseById(@PathVariable("id") Long idCourse) {
		if (idCourse == null || idCourse <= 0) {
			return new ResponseEntity(new CustomErrorType("Valid idCourse is required"), HttpStatus.CONFLICT);
		}
		Course course = _courseService.findCourseById(idCourse);
		if (course == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Course>(course, HttpStatus.OK);
	}
	
	//POST
	@RequestMapping(value="/courses", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<?> createCourse(@RequestBody Course course, UriComponentsBuilder uriComponentsBuilder) {
		if (course.getName().equals(null) || course.getName().isEmpty()) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		if (_courseService.findCourseByName(course.getName()) != null) {
			return new ResponseEntity(new CustomErrorType("course already exist"), HttpStatus.CONFLICT);
		}
		_courseService.saveCourse(course);
		Course course2 = _courseService.findCourseByName(course.getName());
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uriComponentsBuilder
				.path("/v1/courses/{id}")
				.buildAndExpand(course2.getIdCourse())
				.toUri());
		return new ResponseEntity<String>(headers, HttpStatus.CREATED);
	}
	
	//UPDATE
	@RequestMapping(value="/courses/{id}", method = RequestMethod.PATCH, headers = "Accept=application/json")
	public ResponseEntity<?> updateCourse(@PathVariable("id") Long idCourse, @RequestBody Course course) {
		Course currentCourse = _courseService.findCourseById(idCourse);
		if (currentCourse == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		currentCourse.setName(course.getName());
		_courseService.updateCourse(currentCourse);
		return new ResponseEntity<Course>(currentCourse, HttpStatus.OK);
	}
	
	//DELETE
	@RequestMapping(value="/courses/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<?> deleteCourse(@PathVariable("id") Long idCourse) {
		if (idCourse == null || idCourse <=  0) {
			return new ResponseEntity(new CustomErrorType("idCourse is required"), HttpStatus.CONFLICT);
		}
		Course course = _courseService.findCourseById(idCourse);
		if (course == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		_courseService.deleteCourseById(idCourse);
		return new ResponseEntity<Course>(course, HttpStatus.OK);
	}
	
}
