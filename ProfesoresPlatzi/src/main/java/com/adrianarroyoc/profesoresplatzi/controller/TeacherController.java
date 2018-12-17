package com.adrianarroyoc.profesoresplatzi.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.adrianarroyoc.profesoresplatzi.model.SocialMedia;
import com.adrianarroyoc.profesoresplatzi.model.Teacher;
import com.adrianarroyoc.profesoresplatzi.model.TeacherSocialMedia;
import com.adrianarroyoc.profesoresplatzi.service.SocialMediaService;
import com.adrianarroyoc.profesoresplatzi.service.TeacherService;
import com.adrianarroyoc.profesoresplatzi.util.CustomErrorType;

public class TeacherController {
	
	@Autowired
	private TeacherService _teacherService;
	
	@Autowired
	private SocialMediaService _socialMediaService;
	
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
			return new ResponseEntity(new CustomErrorType("Please set an id for a teacher"), HttpStatus.CONFLICT);
		}
		Teacher teacher = _teacherService.findTeacherById(idTeacher);
		if (teacher == null) {
			return new ResponseEntity(new CustomErrorType("Unable to delete teacher beacuse the id is incorrect"), HttpStatus.NOT_FOUND);
		}
		_teacherService.deleteTeacherById(idTeacher);
		return new ResponseEntity<Teacher>(teacher, HttpStatus.OK);
	}
	
	public static final String TEACHER_UPLOADED_FOLDER = "images/teachers/";
	//CREATE TEACHER IMAGE
	@RequestMapping(value="/teachers/image", method = RequestMethod.POST, headers = ("content-type=multipart/form-data"))
	public ResponseEntity<byte[]> uploadTeacherImage(
			@RequestParam("id") Long idTeacher, 
			@RequestParam("file") MultipartFile multipartFile, 
			UriComponentsBuilder componentsBuilder) {
		if (idTeacher == null) {
			return new ResponseEntity(new CustomErrorType("Please set an id for a teacher"), HttpStatus.NO_CONTENT);
		}
		if (multipartFile.isEmpty()) {
			return new ResponseEntity(new CustomErrorType("Please select a file to upload"), HttpStatus.NO_CONTENT);
		}
		Teacher teacher = _teacherService.findTeacherById(idTeacher);
		if (teacher == null) {
			return new ResponseEntity(new CustomErrorType("Invalid id"), HttpStatus.NOT_FOUND);
		}
		if (teacher.getAvatar() != null &&  !teacher.getAvatar().isEmpty()) {
			String fileName = teacher.getAvatar();
			Path path = Paths.get(fileName);
			File f = path.toFile();
			if (f.exists()) {
				f.delete();
			}
		}
		try {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String dateName = dateFormat.format(date);
			String fileName = String.valueOf(idTeacher) + "-pictureTeacher-" + dateName + "." + multipartFile.getContentType().split("/")[1];
			teacher.setAvatar(TEACHER_UPLOADED_FOLDER + fileName);
			byte[] bytes = multipartFile.getBytes();
			Path path = Paths.get(TEACHER_UPLOADED_FOLDER + fileName);
			Files.write(path, bytes);
			_teacherService.updateTeacher(teacher);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResponseEntity(new CustomErrorType("Error during upload: " + multipartFile.getOriginalFilename()), HttpStatus.CONFLICT);
		}
	}
	
	//GET IMAGE
	@RequestMapping(value="/tachers/{id_teacher}/images/", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getTeacherImage(@PathVariable("id") Long idTeacher) {
		if (idTeacher == null) {
			return new ResponseEntity(new CustomErrorType("Please set an id for a teacher"), HttpStatus.NO_CONTENT);
		}
		Teacher teacher = _teacherService.findTeacherById(idTeacher);
		if (teacher == null) {
			return new ResponseEntity(new CustomErrorType("Invalid id"), HttpStatus.NO_CONTENT);
		}
		try {
			String fileName = teacher.getAvatar();
			Path path = Paths.get(fileName);
			File f = path.toFile();
			if (!f.exists()) {
				return new ResponseEntity(new CustomErrorType("Image not found"), HttpStatus.CONFLICT);
			}
			byte[] image = Files.readAllBytes(path);
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResponseEntity(new CustomErrorType("Error showing the image"), HttpStatus.CONFLICT);
		}
	}
	
	//DELETE IMAGE
	@RequestMapping(value="/tachers/{id_teacher}/images/", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<?> deleteTeacherImage(@PathVariable("id") Long idTeacher) {
		if (idTeacher == null) {
			return new ResponseEntity(new CustomErrorType("Please set an id for a teacher"), HttpStatus.NO_CONTENT);
		}
		Teacher teacher = _teacherService.findTeacherById(idTeacher);
		if (teacher == null) {
			return new ResponseEntity(new CustomErrorType("Invalid id"), HttpStatus.NO_CONTENT);
		}
		if (teacher.getAvatar() == null || teacher.getAvatar().isEmpty()) {
			return new ResponseEntity(new CustomErrorType("This teacher doesnt have image assigned"), HttpStatus.NOT_FOUND);
		}
		String fileName = teacher.getAvatar();
		Path path = Paths.get(fileName);
		File f = path.toFile();
		if (f.exists()) {
			f.delete();
		}
		teacher.setAvatar("");
		_teacherService.updateTeacher(teacher);
		return new ResponseEntity<Teacher>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(value="teachers/socialMedias", method = RequestMethod.PATCH, headers = "Accept=application/JSON")
	public ResponseEntity<?> assignTeacherSocialMedia(@RequestBody Teacher teacher, UriComponentsBuilder uriComponentsBuilder) {
		if (teacher.getIdTeacher() == null) {
			return new ResponseEntity(new CustomErrorType("We need the teacher"), HttpStatus.NO_CONTENT);
		}
		Teacher teacherSaved = _teacherService.findTeacherById(teacher.getIdTeacher());
		if (teacherSaved == null) {
			return new ResponseEntity(new CustomErrorType("Teacher not found"), HttpStatus.NO_CONTENT);
		}
		if (teacher.getTeacherSocialMedias().size() == 0) {
			return new ResponseEntity(new CustomErrorType("No social medias ids in for the teacher"), HttpStatus.NO_CONTENT);
		}
		else {
			Iterator<TeacherSocialMedia> i = teacher.getTeacherSocialMedias().iterator();
			while (i.hasNext()) {
				TeacherSocialMedia teacherSocialMedia = i.next();
				if (teacherSocialMedia.getSocialMedia().getIdSocialMedia() == null || teacherSocialMedia.getNickname() == null) {
					return new ResponseEntity(new CustomErrorType("Social media data not found"), HttpStatus.NO_CONTENT);
				}
				else {
					TeacherSocialMedia tsmAux = _socialMediaService.findSocialMediaByIdAndName(
							teacherSocialMedia.getSocialMedia().getIdSocialMedia(), 
							teacherSocialMedia.getNickname());
					if (tsmAux != null) {
						return new ResponseEntity(new CustomErrorType("Social media for the teacher already exists"), HttpStatus.CONFLICT);
					}
					SocialMedia socialMedia = _socialMediaService.findSocialMediaById(
							teacherSocialMedia.getSocialMedia().getIdSocialMedia());
					if (socialMedia == null) {
						return new ResponseEntity(new CustomErrorType("Social media for the teacher not found"), HttpStatus.NOT_FOUND);
					}
					teacherSocialMedia.setSocialMedia(socialMedia);
					teacherSocialMedia.setTeacher(teacherSaved);
					if (tsmAux == null) {
						teacherSaved.getTeacherSocialMedias().add(teacherSocialMedia);
					}
					else {
						LinkedList<TeacherSocialMedia> teacherSocialMedias = new LinkedList<>();
						teacherSocialMedias.addAll(teacherSaved.getTeacherSocialMedias());
						for (int j = 0; j < teacherSocialMedias.size(); j++) {
							TeacherSocialMedia teacherSocialMedia2 = teacherSocialMedias.get(j);
							if (teacherSocialMedia.getTeacher().getIdTeacher() == teacherSocialMedia2.getTeacher().getIdTeacher()
									&& teacherSocialMedia.getSocialMedia().getIdSocialMedia() == teacherSocialMedia2.getSocialMedia().getIdSocialMedia()) {
								teacherSocialMedia2.setNickname(teacherSocialMedia.getNickname());
								teacherSocialMedias.set(j, teacherSocialMedia2);
							}
							else {
								teacherSocialMedias.set(j, teacherSocialMedia2);
							}
						}
						teacherSaved.getTeacherSocialMedias().clear();
						teacherSaved.getTeacherSocialMedias().addAll(teacherSocialMedias);
					}
				}
			}
		}
		_teacherService.updateTeacher(teacherSaved);
		return new ResponseEntity<Teacher>(teacherSaved, HttpStatus.OK);
	}
	
}
