package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	//method to add common data
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USER NAME " + userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER: " + user);
		model.addAttribute("user",user);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact", new Contact());
		return "add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, 
								@RequestParam("profileImage") MultipartFile file,
								Principal principal,
								HttpSession session) {
		try {
		System.out.println("DATA " + contact);
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		
		//image processing
		if (file.isEmpty()) {
			System.out.println("File is Emplty");
			
		}else {
			contact.setImageUrl(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING );
		}
		
		contact.setUser(user);		
		user.getContacts().add(contact);
		userRepository.save(user);
		System.out.println("ADDED TO THE DATABASE");
		session.setAttribute("message",new Message("Your Contact is Added", "success"));
		}catch(Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			session.setAttribute("message",new Message("Something Wrong !! Try Again", "danger"));
		}
		return "add_contact_form";
	}
	
	@GetMapping("/show-contacts")
	public String showContacts(Model model) {
		return "show_contacts";
	}
	
	

}
