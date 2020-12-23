package com.o1teck.controllers;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.validation.Valid;

import org.cloudinary.json.JSONObject;
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
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
//import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
//import com.o1teck.MockMultipartFile;
import com.o1teck.model.entity.Interest;
import com.o1teck.model.entity.Profile;
import com.o1teck.model.entity.SiteUser;
import com.o1teck.model.repository.UserDao;
import com.o1teck.service.CloudinaryService;
import com.o1teck.service.FileService;
import com.o1teck.service.InterestService;
import com.o1teck.service.ProfileService;
import com.o1teck.service.UserService;


@Controller
public class ProfileController {
	
	@Autowired
    private Cloudinary cloudinary;

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
	
	//private String profilePhotoName;
	
	
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

	private ModelAndView showProfile(SiteUser user) {
		ModelAndView modelAndView = new ModelAndView();

		if (user == null) {
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

	// To show Profile of the CURRENT USER
	// Calls: showProfile(user)
	@RequestMapping(value = "/profile")
	public ModelAndView showProfile() {

		SiteUser user = getUser();

		String firstname = user.getFirstname();
		String surname = user.getSurname();

		ModelAndView modelAndView = showProfile(user); // This is our new refactored method above

		modelAndView.getModel().put("ownProfile", true);
		modelAndView.getModel().put("firstname", firstname);
		modelAndView.getModel().put("surname", surname);

		return modelAndView;
	}

	
	
	@RequestMapping(value = "/profile/{id}")
	public ModelAndView showProfile(@PathVariable("id") Long id) {
		
		// use the PathVariable(id) to get the corresponding user...
		Optional<SiteUser> userOpt = userService.get(id);
		SiteUser user = userOpt.get();

		String firstname = user.getFirstname();
		String surname = user.getSurname();

		// And maybe create our modelAndView for returning
		ModelAndView modelAndView = new ModelAndView();

		// now to get the user...
		if (user != null) {

			// set modelAndView equal to show profile? (must be an M&V return method)
			modelAndView = showProfile(user);

			modelAndView.getModel().put("ownProfile", false);
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
	
	
	
	
	
	@PostMapping("/upload")
    @ResponseBody
    public String uploadFile(Model model, @RequestParam("file") MultipartFile file) throws IOException, URISyntaxException, ServletException {
    	
		String imageToUpload = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/2013-Aerial-Mount_of_Olives.jpg/270px-2013-Aerial-Mount_of_Olives.jpg";
    	
    	cloudinary.uploader().unsignedUpload(imageToUpload, "apzbavjn", ObjectUtils.asMap("cloud_name", "nicko1teck"));
    	
    	return "";
		
		/*
	
		//LOG
    	System.out.println();
    	System.out.println("Called the uploadFile() method !");
    	System.out.println();
    	
//    	MultipartFile multipartFile = new MockMultipartFile("sourceFile.temp", file.getBytes());
//    	File theFile = new File("src/main/resources/targetFile.tmp");
//    	multipartFile.transferTo(theFile);

    	//1) create a client
 
    	//2) create the post body as required by Cloudinary API
 
    	//3)  send POST request and capture response
    	
    	
    	//Get the timestamp in seconds
    	Long timestamp = Instant.now().getEpochSecond();
    	
    	//LOG
    	System.out.println();
    	System.out.println(timestamp);
    	System.out.println();
    	
    	//create sig-request params
    	List<NameValuePair> paramsToSign = new ArrayList<NameValuePair>();
    	
    	//add timestamp to those params
    	paramsToSign.add(new BasicNameValuePair("timestamp", timestamp.toString()));
    	
    	//LOG
    	System.out.println();
    	System.out.println(paramsToSign);
    	System.out.println();
    	
    	//Get the signature using the Java SDK method api_sign_request
    	String signature = cloudinary.apiSignRequest((Map<String, Object>) paramsToSign, "UFH1ZXH_4UZ8XNHchIj8Lwhpszw"); 
    	
    	//LOG
    	System.out.println();
    	System.out.println(signature);
    	System.out.println();
    	
    	String imageAddress = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/2013-Aerial-Mount_of_Olives.jpg/270px-2013-Aerial-Mount_of_Olives.jpg";
    	
    	CloseableHttpClient client = HttpClients.createDefault();
    	
    	HttpPost httpPost = new HttpPost("https://api.cloudinary.com/v1_1/nicko1teck/image/upload");
    	
    	List<NameValuePair> params = new ArrayList<NameValuePair>();
    	
    	params.add(new BasicNameValuePair("file", imageAddress));
    	
    	params.add(new BasicNameValuePair("signature", signature));
    	
    	httpPost.setEntity(new UrlEncodedFormEntity(params));
    	
    	CloseableHttpResponse response = client.execute(httpPost);
    	
    	client.close();
    	
    	//JSONObject json=new JSONObject(authResponse);
    	
    	//String url=json.getString("url");
    	
    	return response.toString();
    	
    	*/
		
		
		
    	
    }
	
	
	
	
	
	
	@RequestMapping(value = "/profilephoto/{id}", method = RequestMethod.GET)
	@ResponseBody
	ResponseEntity<InputStreamResource> servePhoto(@PathVariable Long id) throws IOException, URISyntaxException {

		// SiteUser user = userService.get(id);
		Optional<SiteUser> userOpt = userService.get(id);
		SiteUser user = userOpt.get();

		Profile profile = profileService.getUserProfile(user);

		// example Cloudinary image URL
		// https://res.cloudinary.com/nicko1teck/image/upload/v1602199667/MTDeductive_100x100_gubzzf.jpg

		String photoPathString = "https://res.cloudinary.com/nicko1teck/image/upload/v1603731768/my_folder/my_sub_folder/" + "${profile.profilePhotoName}";
		Path photoPath = Paths.get(photoPathString);
		System.out.println();
		System.out.println();
		System.out.println("Testing for photo URL construction: ");
		System.out.println(photoPathString);
		System.out.println();
		System.out.println();
		
		/*
		Path photoPath = Paths.get(new URI(
				"https://res.cloudinary.com/nicko1teck/image/fetch/https://res.cloudinary.com/nicko1teck/image/upload/v1602199667/MTDeductive_100x100_gubzzf.jpg"));

		*/
		
		
		
		//Path photoPath = Paths.get(photoUploadDirectory, "default","productive-hobbies100x100.jpg");

		/*
		 * Excluding this code to see if I can get a Cloudinary image to serve here,
		 * found at the above URL
		 * 
		 * if (profile != null && profile.getPhoto(photoUploadDirectory) != null) {
		 * photoPath = profile.getPhoto(photoUploadDirectory); }
		 * 
		 */

		return ResponseEntity.ok().contentLength(Files.size(photoPath))
				.contentType(MediaType.parseMediaType(URLConnection.guessContentTypeFromName(photoPath.toString())))
				.body(new InputStreamResource(Files.newInputStream(photoPath, StandardOpenOption.READ)));
	}

	/*
	 * 
	 * @RequestMapping(value = "/profilephoto/{id}", method = RequestMethod.GET)
	 * 
	 * @ResponseBody ResponseEntity<InputStreamResource> servePhoto2(@PathVariable
	 * Long id)throws IOException {
	 * 
	 * 
	 * Optional<SiteUser> userOpt = userService.get(id); SiteUser user =
	 * userOpt.get(); Profile profile = profileService.getUserProfile(user);
	 * 
	 * 
	 * Path photoPath = Paths.get(photoUploadDirectory, "default",
	 * "productive-hobbies100x100.jpg"); //Path photoPath2 =
	 * Paths.get(photoUploadDirectory, "default");
	 * 
	 * if (profile != null && profile.getPhoto(photoUploadDirectory) != null) {
	 * photoPath = profile.getPhoto(photoUploadDirectory); }
	 * 
	 * return ResponseEntity.ok() .contentLength(Files.size(photoPath))
	 * .contentType(MediaType.parseMediaType(URLConnection.guessContentTypeFromName(
	 * photoPath.toString()))) .body(new
	 * InputStreamResource(Files.newInputStream(photoPath,
	 * StandardOpenOption.READ))); }
	 * 
	 * 
	 */

	
	
	@RequestMapping(value = "/save-interest", method = RequestMethod.POST)
	@ResponseBody // this method returns whatever it does... no view name for Tiles or anything
					// like that.
	public ResponseEntity<?> saveInterest(@RequestParam("name") String interestName) {

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		String cleanedInterestName = htmlPolicy.sanitize(interestName);

		Interest interest = interestService.createIfNotExists(cleanedInterestName);

		profile.addInterest(interest);
		profileService.save(profile);

		return new ResponseEntity<>(null, HttpStatus.OK);
	}
	
	

	@RequestMapping(value = "delete-interest", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> deleteInterest(@RequestParam("name") String interestName) {

		SiteUser user = getUser();
		Profile profile = profileService.getUserProfile(user);

		profile.removeInterest(interestName);

		profileService.save(profile);
		
		System.out.println();
		System.out.println();
		System.out.println("IS ANYTHING FUCKING WORKING?");
		System.out.println();
		System.out.println();

		return new ResponseEntity<>(null, HttpStatus.OK);
	}

	
	
	
}
