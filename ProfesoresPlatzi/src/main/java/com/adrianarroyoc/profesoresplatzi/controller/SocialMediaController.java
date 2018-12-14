package com.adrianarroyoc.profesoresplatzi.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.adrianarroyoc.profesoresplatzi.model.SocialMedia;
import com.adrianarroyoc.profesoresplatzi.service.SocialMediaService;
import com.adrianarroyoc.profesoresplatzi.util.CustomErrorType;

@Controller
@RequestMapping("/v1")
public class SocialMediaController {

	@Autowired
	SocialMediaService _socialMediaService;
	
	//GET
	@RequestMapping(value="/socialMedias", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<SocialMedia>> getSocialMedias(@RequestParam(value="name", required=false)String name) {
		List<SocialMedia> socialMedias = new ArrayList<>();
		socialMedias = _socialMediaService.findAllSocialMedias();
		if (name == null) {
			if (socialMedias.isEmpty()) {
				return new ResponseEntity(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<SocialMedia>>(socialMedias, HttpStatus.OK);
		}
		else {
			SocialMedia socialMedia = _socialMediaService.findSocialMediaByName(name);
			if (socialMedia == null) {
				return new ResponseEntity(HttpStatus.NOT_FOUND);
			}
			socialMedias.add(socialMedia);
			return new ResponseEntity<List<SocialMedia>>(socialMedias, HttpStatus.OK);
		}
	}
	
	//GET
	@RequestMapping(value="/socialMedias/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<SocialMedia> getSocialMediaById(@PathVariable("id") Long idSocialMedia) {
		if (idSocialMedia == null || idSocialMedia <= 0) {
			return new ResponseEntity(new CustomErrorType("idSocialMedia is required"), HttpStatus.CONFLICT);
		}
		SocialMedia socialMedia = _socialMediaService.findSocialMediaById(idSocialMedia);
		if (socialMedia == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<SocialMedia>(socialMedia, HttpStatus.OK);
	}
	
	//POST
	@RequestMapping(value="/socialMedias", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<?> createSocialMedia(@RequestBody SocialMedia socialMedia, UriComponentsBuilder uriComponentsBuilder) {
		if (socialMedia.getName().equals(null) || socialMedia.getName().isEmpty()) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		if (_socialMediaService.findSocialMediaByName(socialMedia.getName()) != null) {
			return new ResponseEntity(new CustomErrorType("socialMedia already exist"), HttpStatus.CONFLICT);
		}
		_socialMediaService.saveSocialMedia(socialMedia);
		SocialMedia socialMedia2 = _socialMediaService.findSocialMediaByName(socialMedia.getName());
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uriComponentsBuilder
				.path("/v1/socialMedias/{id}")
				.buildAndExpand(socialMedia2.getIdSocialMedia())
				.toUri());
		return new ResponseEntity<String>(headers, HttpStatus.CREATED);
	}

	//UPDATE
	@RequestMapping(value="/socialMedias/{id}", method = RequestMethod.PATCH, headers = "Accept=application/json")
	public ResponseEntity<?> updateSocialMedia(@PathVariable("id") Long idSocialMedia, @RequestBody SocialMedia socialMedia) {
		SocialMedia currentSocialMedia = _socialMediaService.findSocialMediaById(idSocialMedia);
		if (currentSocialMedia == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		currentSocialMedia.setName(socialMedia.getName());
		currentSocialMedia.setIcon(socialMedia.getIcon());
		_socialMediaService.updateSocialMedia(currentSocialMedia);
		return new ResponseEntity<SocialMedia>(currentSocialMedia, HttpStatus.OK);
	}
	
	//DELETE
	@RequestMapping(value="/socialMedias/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<?> deleteSocialMedia(@PathVariable("id") Long idSocialMedia) {
		if (idSocialMedia == null || idSocialMedia <=  0) {
			return new ResponseEntity(new CustomErrorType("idSocialMedia is required"), HttpStatus.CONFLICT);
		}
		SocialMedia socialMedia = _socialMediaService.findSocialMediaById(idSocialMedia);
		if (socialMedia == null) {
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}
		_socialMediaService.deleteSocialMediaById(idSocialMedia);
		return new ResponseEntity<SocialMedia>(socialMedia, HttpStatus.OK);
	}
	
	public static final String TEACHER_UPLOADED_FOLDER = "images/socialMedias/";
	//CREATE TEACHER IMAGE
	@RequestMapping(value="/socialMedias/image", method = RequestMethod.POST, headers = ("content-type=multipart/form-data"))
	public ResponseEntity<byte[]> uploadSocialMediaImage(
	        @RequestParam("id") Long idSocialMedia, 
	        @RequestParam("file") MultipartFile multipartFile, 
	        UriComponentsBuilder componentsBuilder) {
	    if (idSocialMedia == null) {
	        return new ResponseEntity(new CustomErrorType("Please set an id for a socialMedia"), HttpStatus.NO_CONTENT);
	    }
	    if (multipartFile.isEmpty()) {
	        return new ResponseEntity(new CustomErrorType("Please select a file to upload"), HttpStatus.NO_CONTENT);
	    }
	    SocialMedia socialMedia = _socialMediaService.findSocialMediaById(idSocialMedia);
	    if (socialMedia == null) {
	        return new ResponseEntity(new CustomErrorType("Invalid id"), HttpStatus.NOT_FOUND);
	    }
	    if (socialMedia.getIcon() != null &&  !socialMedia.getIcon().isEmpty()) {
	        String fileName = socialMedia.getIcon();
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
	        String fileName = String.valueOf(idSocialMedia) + "-pictureSocialMedia-" + dateName + "." + multipartFile.getContentType().split("/")[1];
	        socialMedia.setIcon(TEACHER_UPLOADED_FOLDER + fileName);
	        byte[] bytes = multipartFile.getBytes();
	        Path path = Paths.get(TEACHER_UPLOADED_FOLDER + fileName);
	        Files.write(path, bytes);
	        _socialMediaService.updateSocialMedia(socialMedia);
	        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
	    } catch (Exception e) {
	        // TODO: handle exception
	        e.printStackTrace();
	        return new ResponseEntity(new CustomErrorType("Error during upload: " + multipartFile.getOriginalFilename()), HttpStatus.CONFLICT);
	    }
	}

	//GET IMAGE
	@RequestMapping(value="/tachers/{id_socialMedia}/images/", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getSocialMediaImage(@PathVariable("id") Long idSocialMedia) {
	    if (idSocialMedia == null) {
	        return new ResponseEntity(new CustomErrorType("Please set an id for a socialMedia"), HttpStatus.NO_CONTENT);
	    }
	    SocialMedia socialMedia = _socialMediaService.findSocialMediaById(idSocialMedia);
	    if (socialMedia == null) {
	        return new ResponseEntity(new CustomErrorType("Invalid id"), HttpStatus.NO_CONTENT);
	    }
	    try {
	        String fileName = socialMedia.getIcon();
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
	@RequestMapping(value="/tachers/{id_socialMedia}/images/", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<?> deleteSocialMediaImage(@PathVariable("id") Long idSocialMedia) {
	    if (idSocialMedia == null) {
	        return new ResponseEntity(new CustomErrorType("Please set an id for a socialMedia"), HttpStatus.NO_CONTENT);
	    }
	    SocialMedia socialMedia = _socialMediaService.findSocialMediaById(idSocialMedia);
	    if (socialMedia == null) {
	        return new ResponseEntity(new CustomErrorType("Invalid id"), HttpStatus.NO_CONTENT);
	    }
	    if (socialMedia.getIcon() == null || socialMedia.getIcon().isEmpty()) {
	        return new ResponseEntity(new CustomErrorType("This socialMedia doesnt have image assigned"), HttpStatus.NOT_FOUND);
	    }
	    String fileName = socialMedia.getIcon();
	    Path path = Paths.get(fileName);
	    File f = path.toFile();
	    if (f.exists()) {
	        f.delete();
	    }
	    socialMedia.setIcon("");
	    _socialMediaService.updateSocialMedia(socialMedia);
	    return new ResponseEntity<SocialMedia>(HttpStatus.NO_CONTENT);
	}
	
}
