package com.adrianarroyoc.profesoresplatzi.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.adrianarroyoc.profesoresplatzi.model.Teacher;
import com.adrianarroyoc.profesoresplatzi.service.TeacherService;
import com.adrianarroyoc.profesoresplatzi.util.CustomErrorType;

public class TeacherController {
	
	@Autowired
	TeacherService _teacherService;
	
	//GET
	@RequestMapping(value="/teachers", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<Teacher>> getTeachers(@RequestParam(value="name", required=false)String name){
		List<Teacher> teachers = new ArrayList<>();
		teachers = _teacherService.findAllTeachers();
		if (name == null) {
			if (teachers.isEmpty()) {
				return new ResponseEntity(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<Teacher>>(teachers, HttpStatus.OK);
		}
		Teacher teacher = _teacherService.findTeacherByName(name);
		if (teacher == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		teachers.add(teacher);
		return new ResponseEntity<List<Teacher>>(teachers, HttpStatus.OK);
	}
	
	//GET
	@RequestMapping(value="/teachers/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<Teacher> getTeacherById(@PathVariable("id") Long idTeacher) {
		if (idTeacher == null || idTeacher <= 0) {
			return new ResponseEntity(new CustomErrorType("Valid idTeacher is required"), HttpStatus.CONFLICT);
		}
		Teacher teacher = _teacherService.findTeacherById(idTeacher);
		if (teacher == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Teacher>(teacher, HttpStatus.OK);
	}
	
	//POST
	@RequestMapping(value="/teachers", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<?> createTeacher(@RequestBody Teacher teacher, UriComponentsBuilder uriComponentsBuilder) {
		if (teacher.getName().equals(null) || teacher.getName().isEmpty()) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		if (_teacherService.findTeacherByName(teacher.getName()) != null) {
			return new ResponseEntity(new CustomErrorType("teacher already exist"), HttpStatus.CONFLICT);
		}
		_teacherService.saveTeacher(teacher);
		Teacher teacher2 = _teacherService.findTeacherByName(teacher.getName());
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uriComponentsBuilder
				.path("/v1/teachers/{id}")
				.buildAndExpand(teacher2.getIdTeacher())
				.toUri());
		return new ResponseEntity<String>(headers, HttpStatus.CREATED);
	}
	
	//UPDATE
	@RequestMapping(value="/teachers/{id}", method = RequestMethod.PATCH, headers = "Accept=application/json")
	public ResponseEntity<?> updateTeacher(@PathVariable("id") Long idTeacher, @RequestBody Teacher teacher) {
		Teacher currentTeacher = _teacherService.findTeacherById(idTeacher);
		if (currentTeacher == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		currentTeacher.setName(teacher.getName());
		_teacherService.updateTeacher(currentTeacher);
		return new ResponseEntity<Teacher>(currentTeacher, HttpStatus.OK);
	}
	
	//DELETE
	@RequestMapping(value="/teachers/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<?> deleteTeacher(@PathVariable("id") Long idTeacher) {
		if (idTeacher == null || idTeacher <=  0) {
			return new ResponseEntity(new CustomErrorType("idTeacher is required"), HttpStatus.CONFLICT);
		}
		Teacher teacher = _teacherService.findTeacherById(idTeacher);
		if (teacher == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		_teacherService.deleteTeacherById(idTeacher);
		return new ResponseEntity<Teacher>(teacher, HttpStatus.OK);
	}
	
}
