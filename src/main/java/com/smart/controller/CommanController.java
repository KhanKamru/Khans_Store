package com.smart.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.ProductRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Message;
import com.smart.entities.Product;
import com.smart.entities.User;

@Controller
public class CommanController {

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@ModelAttribute
	public void loginData(Model model, Principal principal) {
		if (principal == null) {
			model.addAttribute("logged", Boolean.FALSE);
			model.addAttribute("cart", "");
		} else {
			model.addAttribute("cart", userRepo.getUserByEmail(principal.getName()).getCartItems().size());
			model.addAttribute("logged", Boolean.TRUE);
		}

	}

	@GetMapping("/")
	public String home(Model model, Principal principal) {
		model.addAttribute("title", "Khan's Store");
		List<Product> products = this.productRepo.getCategory();
		for (Product product : products) {
			System.out.println(product);
		}
		model.addAttribute("products", products);
		return "home";
	}

	@GetMapping("/search/{page}")
	public String home(@PathVariable("page") int page, @RequestParam("search") String value, Model model) {
		model.addAttribute("title", "Result for " + value);
		Pageable pageAble = PageRequest.of(page-1, 6);
		Page<Product> products = productRepo.searchQuery(value, pageAble);
		int totalPage = products.getTotalPages();
		int nextPage = page + 1;
		int prePage = page - 1;

		model.addAttribute("products", products);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("nextPage",nextPage);
		model.addAttribute("prePage", prePage);
		model.addAttribute("params", value);
		return "search-content";
	}

	@GetMapping("/category/{category}")
	public String category(@PathVariable("category") String category, Model model) {
		model.addAttribute("title", "Result for " + category);
		List<Product> products = productRepo.getProductFromCategoty(category);
		model.addAttribute("products", products);
		return "search-content";
	}

	@GetMapping("/customer-register")
	public String customerRegister(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("role", "ROLE_CUSTOMER");
		return "register-form";
	}

	@GetMapping("/seller-register")
	public String sellerRegister(Model model) {
		model.addAttribute("role", "ROLE_SELLER");
		model.addAttribute("user", new User());
		return "register-form";
	}

	@GetMapping("/signin")
	public String renderLogin() {
		return "login-form";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam("confirm_pass") String cPass, Model model, HttpSession sess) {
		if (result.hasErrors()) {
			return "register-form";
		}
		try {
			if (this.userRepo.getUserByEmail(user.getEmail()) != null) {
				throw new Exception("This Email has Taken");
			}

			String pass = user.getPassword();
			if (!pass.equals(cPass)) {
				throw new Exception("Password Should Match.");
			}
			user.setPassword(passEncoder.encode(pass));
			this.userRepo.save(user);
		} catch (Exception ex) {
			ex.printStackTrace();
			model.addAttribute("user", user);
			sess.setAttribute("msg", new Message("error", ex.getMessage()));
			return "register-form";
		}
		return "redirect:/signin";
	}

	@GetMapping("/redirect")
	public String redirect(Principal principal) {
		User user = this.userRepo.getUserByEmail(principal.getName());
		if (user.getRole().equals("ROLE_CUSTOMER")) {
			return "redirect:/";
		}
		return "redirect:/seller/dashboard/1";
	}
}
