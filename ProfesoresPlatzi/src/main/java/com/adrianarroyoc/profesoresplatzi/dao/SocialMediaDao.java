package com.adrianarroyoc.profesoresplatzi.dao;

import java.util.List;

import com.adrianarroyoc.profesoresplatzi.model.SocialMedia;
import com.adrianarroyoc.profesoresplatzi.model.TeacherSocialMedia;

public interface SocialMediaDao {

	void saveSocialMedia(SocialMedia socialMedia);
	
	void deleteSocialMediaById(Long idSocialMedia);
	
	void updateSocialMedia(SocialMedia socialMedia);

	List<SocialMedia> findAllSocialMedias();
	
	SocialMedia findTeacherById(Long idSocialMedia);
	
	SocialMedia findSocialMediaByName(String name);
	
	TeacherSocialMedia findSocialMediaByIdAndName(Long idSociaMedia, String nickname);
	
}
