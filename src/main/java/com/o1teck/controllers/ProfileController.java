package com.o1teck.controllers;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.validation.Valid;

import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.o1teck.exceptions.ImageTooSmallException;
import com.o1teck.exceptions.InvalidFileException;
import com.o1teck.model.dto.FileInfo;
import com.o1teck.model.entity.Interest;
import com.o1teck.model.entity.Profile;
import com.o1teck.model.entity.SiteUser;
import com.o1teck.model.repository.UserDao;
import com.o1teck.service.FileService;
import com.o1teck.service.InterestService;
import com.o1teck.service.ProfileService;
import com.o1teck.service.UserService;
import com.o1teck.status.PhotoUploadStatus;

@Controller
public class ProfileController {

	@Autowired
	private FileService fileService;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private InterestService interestService;

	@Autowired
	private PolicyFactory htmlPolicy;
	
	@Autowired
	private UserDao userDao;
	
	@Value("${photo.upload.directory}")
	private String photoUploadDirectory;

	@Value("${photo.upload.ok}")
	private String photoStatusOK;

	@Value("${photo.upload.invalid}")
	private String photoStatusInvalid;

	@Value("${photo.upload.ioexception}")
	private String photoStatusIOException;

	@Value("${photo.upload.toosmall}")
	private String photoStatusTooSmall;

	
	private SiteUser getUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		return userService.get(email);
	}

	private ModelAndView showProfile(SiteUser user){
		ModelAndView modelAndView = new ModelAndView();
		
		if (user==null){
			modelAndView.setViewName("redirect:/");
			return modelAndView;
		}
		
		Profile profile = profileService.getUserProfile(user);
	
		if (profile == null) {
			profile = new Profile();
			profile.setUser(user);
			profileService.save(profile);
		}
		
		Profile webProfile = new Profile();
		webProfile.safeCopyFrom(profile);
		
		modelAndView.getModel().put("userId", user.getId());
		modelAndView.getModel().put("profile", webProfile);

		modelAndView.setViewName("app.profile");
		
		return modelAndView;
	}
	
	
	//To show Profile of the CURRENT USER
		//Calls:  showProfile(user)
	@RequestMapping(value = "/profile")
	public ModelAndView showProfile() {

		SiteUser user = getUser();
		
		String firstname = user.getFirstname();
		String surname = user.getSurname();

		ModelAndView modelAndView = showProfile(user); //This is our new refactored method above
		
		modelAndView.getModel().put("ownProfile",  true);
		modelAndView.getModel().put("firstname", firstname);
		modelAndView.getModel().put("surname", surname);
		
		return modelAndView;
	}
	
	
	@RequestMapping(value = "/profile/{id}")
	public ModelAndView showProfile(@PathVariable("id") Long id) {
		//Before, it was enough to get the current user with this private method
		//SiteUser user = getUser();   But now we want to see profiles of other users, so we need to 'get' a different way
		
		//So use the PathVariable(id) to get the corresponding user...
		Optional<SiteUser> userOpt = userService.get(id);
		SiteUser user = userOpt.get();
		
		String firstname = user.getFirstname();
		String surname = user.getSurname();
		
		//And maybe create our modelAndView for returning 
		ModelAndView modelAndView = new ModelAndView();
		
		//now to get the user...
		if (user != null){
			
			//set modelAndView equal to show profile? (must be an M&V return method)
			modelAndView = showProfile(user);
			
			modelAndView.getModel().put("ownProfile",  false);
			modelAndView.getModel().put("firstname", firstname);
			modelAndView.getModel().put("surname", surname);
			
			return modelAndView;
			
		} else {
			modelAndView.setViewName("redirect:/");
			return modelAndView;
		}
	}

	
	@RequestMapping(value = "/edit-profile-about", method = RequestMethod.GET)
	public ModelAndView editProfile(ModelAndView modelAndView) {

		// We need to know what user's profile to edit!
		SiteUser user = getUser();

		Profile profile = profileService.getUserProfile(user);

		Profile webProfile = new Profile();

		webProfile.safeCopyFrom(profile);

		modelAndView.getModel().put("profile", webProfile);

		modelAndView.setViewName("app.editProfileAbout");

		return modelAndView;
	}
	

	@RequestMapping(value = "/edit-profile-about", method = RequestMethod.POST)
	ModelAndView editProfile(ModelAndView modelAndView, @Valid Profile webProfile, BindingResult result) {

		modelAndView.setViewName("app.editProfileAbout");

		SiteUser user = getUser();

		Profile profile = profileService.getUserProfile(user);

		profile.safeMergeFrom(webProfile, htmlPolicy);

		if (!result.hasErrors()) {
			profileService.save(profile);
			modelAndView.setViewName("redirect:/profile");
		}
		return modelAndView;
	}

	// In this method we are calling saveImageFile(), but
	// that method makes up a new/random name for our image.
	// Therefore, we need some object that saveImageFile() can pass BACK to the
	// profile controller, so it can save that information.
	// 
	// Why?
	// Because we need to be able to save the details of the file that's being
	// saved and created.
	@RequestMapping(value = "/upload-profile-photo", method = RequestMethod.POST)
	@ResponseBody // returns data in JASON format
	public ResponseEntity<PhotoUploadStatus> handlePhotoUploads(@RequestParam("file") MultipartFile file) {

		// First, get the user -- with our utility method
		SiteUser user = getUser();

		// Then get the user's profile and pass in the info
		Profile profile = profileService.getUserProfile(user);

		Path oldPhotoPath = profile.getPhoto(photoUploadDirectory);

		PhotoUploadStatus status = new PhotoUploadStatus(photoStatusOK);

		try {
			FileInfo photoInfo = fileService.saveImageFile(file, photoUploadDirectory, "photos", "p" + user.getId(),
					100, 100);

			// Having saved the details to fields in the profile class, we can
			// now...
			profile.setPhotoDetails(photoInfo);
			profileService.save(profile);

			//debug
			//System.out.println();
			//System.out.println(photoInfo.toString());
			//System.out.println();

			if (oldPhotoPath != null) {
				Files.delete(oldPhotoPath);
			}

		} catch (InvalidFileException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (ImageTooSmallException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		// modelAndView.setViewName("/profile");
		// return modelAndView;
		
		return new ResponseEntity(status, HttpStatus.OK);
	}

	@RequestMapping(value = "/profilephoto/{id}", method = RequestMethod.GET)
	// we also need to say that what's returned is not some Tiles mapping, but
	// rather a load of data that should be displayed directly
	@ResponseBody
	// which will then be wrapped in this ResponseEntity, in other words...
	// What we're going to send in the body if this response is an
	// InputStreamResource which will contain the data of our photo
	ResponseEntity<InputStreamResource> servePhoto(@PathVariable Long id)throws IOException {

		//SiteUser user = userService.get(id);
		Optional<SiteUser> userOpt = userService.get(id);
		SiteUser user = userOpt.get();

		Profile profile = profileService.getUserProfile(user);

		Path photoPath = Paths.get(photoUploadDirectory, "default", "productive-hobbies100x100.jpg");
		//Path photoPath2 = Paths.get(photoUploadDirectory, "default");
		
		if (profile != null && profile.getPhoto(photoUploadDirectory) != null) {
			photoPath = profile.getPhoto(photoUploadDirectory);
		}

		return ResponseEntity.ok()
				.contentLength(Files.size(photoPath))
				.contentType(MediaType.parseMediaType(URLConnection.guessContentTypeFromName(photoPath.toString())))
				.body(new InputStreamResource(Files.newInputStream(photoPath, StandardOpenOption.READ)));
	}

	
	//We want methods for saving/deleting interests, but how we do this completely depends on 
	//how we implement the list of interests
		//For instance, we could do something similar to how we did Status Update
		//(only difference is that Interests are associated with a particular profile)
	//what we want is a method that we can post data to, and it will save our interets and return an httpresponse code
	@RequestMapping(value="/save-interest", method=RequestMethod.POST)
	@ResponseBody //this method returns whatever it does... no view name for Tiles or anything like that.
	public ResponseEntity<?> saveInterest(@RequestParam("name") String interestName){
		
		SiteUser user = getUser();
		Profile profile=profileService.getUserProfile(user);
		
		String cleanedInterestName = htmlPolicy.sanitize(interestName);
		
		Interest interest = interestService.createIfNotExists(cleanedInterestName);
		
		profile.addInterest(interest);
		profileService.save(profile);
		
		return new ResponseEntity<>(null, HttpStatus.OK);
	}
	
	@RequestMapping(value="delete-interest", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> deleteInterest(@RequestParam("name") String interestName) {
		
		SiteUser user = getUser();
		Profile profile=profileService.getUserProfile(user);
		
		profile.removeInterest(interestName);
		
		profileService.save(profile);
		
		return new ResponseEntity<>(null, HttpStatus.OK);
	}
}
