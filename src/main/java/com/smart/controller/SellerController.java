package com.smart.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ImageRepository;
import com.smart.dao.OrderRepo;
import com.smart.dao.ProductRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Image;
import com.smart.entities.Message;
import com.smart.entities.Order;
import com.smart.entities.Product;
import com.smart.entities.User;

@Controller
@RequestMapping("/seller")
public class SellerController {

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private ImageRepository imageRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@ModelAttribute("user")
	public User user(Model model, Principal principle) {
//		User user=this.userRepo.findById((Integer)ses.getAttribute("userId")).get();
		User user = this.userRepo.getUserByEmail(principle.getName());
		return user;
	}

	@GetMapping("/add-product")
	public String addProduct(Model model) {
		model.addAttribute("title", "Add Product");
		model.addAttribute("product", new Product());
		return "seller/add-product";
	}

	@PostMapping("/add-product")
	public String addedProduct(@Valid @ModelAttribute("product") Product product, BindingResult result,
			@RequestParam("files") List<MultipartFile> images, Model model, HttpSession sess) {
		model.addAttribute("title", "Add Product");

		if (result.hasErrors()) {
			return "seller/add-product";
		}
		try {
			File saveFile = new ClassPathResource("static/img").getFile();
			User user = (User) model.getAttribute("user");
			product.setSeller(user);
			if (images.size() > 5) {
				throw new Exception("You Can't upload more than 5 images");
			}
			if (images.size() == 0) {
				throw new Exception("Please Upload at least one photo");
			}
			Product p = this.productRepo.save(product);
			for (MultipartFile multipartFile : images) {
				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.write(path, multipartFile.getBytes());
//				Product derivedProduct=this.productRepo.findBySellerId(3);
				Image img = new Image();
				img.setImageText(multipartFile.getOriginalFilename());
				img.setProduct(p);
				this.imageRepo.save(img);
			}
			model.addAttribute("product", new Product());
			sess.setAttribute("msg", new Message("success", "Product Added"));
		} catch (Exception ex) {
			model.addAttribute("product", product);
			sess.setAttribute("msg", new Message("error", ex.getMessage()));
		}
		return "seller/add-product";
	}

	@GetMapping("/dashboard/{page}")
	public String dashboard(@PathVariable("page") int page, Model model) {
		User user = (User) model.getAttribute("user");
		int userId = user.getId();
		model.addAttribute("userId", userId);
		model.addAttribute("title", "Dashboard");
		Pageable pageable = PageRequest.of(page - 1, 6);
		Page<Product> products = this.productRepo.findBySellerId(userId, pageable);
		model.addAttribute("products", products);
		int totalPage = products.getTotalPages();
		int nextPage = page + 1;
		int prePage = page - 1;
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("prePage", prePage);
		return "seller/dashboard";
	}

	@GetMapping("/product/edit/{productId}")
	public String renderEdit(@PathVariable("productId") int pId, Model model) {
		Product product = this.productRepo.findById(pId).get();

		model.addAttribute("product", product);
		return "seller/edit";
	}

	@PostMapping("/product/edit/{productId}")
	public String productEdit(@ModelAttribute("product") Product p, @PathVariable("productId") int pId, Model model,
			HttpSession sess) {
		Product product = this.productRepo.findById(pId).get();
		product = p;
		product.setId(pId);
		User user = (User) model.getAttribute("user");
		product.setSeller(user);
		this.productRepo.save(product);
		sess.setAttribute("msg", new Message("success", "Edited Successfully"));
		return "redirect:/seller/dashboard/1";
	}

	@GetMapping("/product/delete/{productId}")
	public String productDelete(@PathVariable("productId") int pId, Model model, HttpSession sess) {
		try {
			List<Image> images = this.imageRepo.findByProductId(pId);
			File saveFile = new ClassPathResource("static/img").getFile();
			for (Image image : images) {
				File f = new File(saveFile.getAbsoluteFile() + File.separator + image.getImageText());
			}
			this.productRepo.deleteById(pId);
			sess.setAttribute("msg", new Message("success", "Deleted Successfully"));

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "redirect:/seller/dashboard/1";
	}

	@GetMapping("/product/image/select/{productId}")
	public String viewPhoto(@PathVariable("productId") int pId, Model model) {
		List<Image> images = this.productRepo.getAllImage(pId);
		User user = (User) model.getAttribute("user");
//		List<Product> products = this.productRepo.getP(user);
//		for (Product product : products) {
//			System.out.println(product);
//		}
		model.addAttribute("images", images);
		return "seller/view-image";
	}

	@PostMapping("/product/image/delete")
	public String getImage(@RequestParam("imgId") int[] ids, Model model, HttpSession sess) {
		try {
			File saveFile = new ClassPathResource("static/img").getFile();
			for (int id : ids) {
				Image image = this.imageRepo.findById(id).get();
				File f = new File(saveFile.getAbsoluteFile() + File.separator + image.getImageText());
				f.delete();
				this.imageRepo.delete(image);
				sess.setAttribute("msg", new Message("success", "Deleted Successfully"));

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "redirect:/seller/dashboard/1";
	}

	@PostMapping("/product/image/add")
	public String addPhoto(@RequestParam("pId") int pId, @RequestParam("files") List<MultipartFile> files, Model model,
			HttpSession sess) {
		Product p = this.productRepo.findById(pId).get();
		List<Image> images = this.productRepo.getAllImage(pId);
		if (images.size() >= 5) {
			sess.setAttribute("msg", new Message("error", "Image Should not be more than 5"));
			return "seller/imageOperation";
		}
		try {
			for (MultipartFile f : files) {
				File file = new File(new ClassPathResource("static/img").getFile().getAbsoluteFile() + File.separator
						+ f.getOriginalFilename());
				FileOutputStream fos = new FileOutputStream(file);
				InputStream content = f.getInputStream();
				byte data[] = content.readAllBytes();
				fos.write(data);
				Image img = new Image();
				img.setImageText(f.getOriginalFilename());
				img.setProduct(p);
				this.imageRepo.save(img);
				sess.setAttribute("msg", new Message("success", "Added Successfully"));

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "redirect:/seller/dashboard/1";
	}

	@GetMapping("/product/image-operation/{page}")
	public String imageOperation(@PathVariable("page") int page, Model model) {
		User user = (User) model.getAttribute("user");
		model.addAttribute("title", "Image Operation");
		Pageable pageable = PageRequest.of(page - 1, 6);
		Page<Product> products = this.productRepo.getP(user, pageable);
		int totalPage = products.getTotalPages();
		int nextPage = page + 1;
		int prePage = page - 1;
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("prePage", prePage);
		model.addAttribute("products", products);
		return "seller/imageOperation";
	}

	@GetMapping("/product/customer/{page}")
	public String getCustomer(@PathVariable("page") int page, Model model) {
		User user = (User) model.getAttribute("user");
		model.addAttribute("title", "Customer Details");
		List<Product> products = this.productRepo.getP(user);
		List<Order> ordersList = new ArrayList<>();
		for (Product product : products) {
			List<Order> order = this.orderRepo.getOrder(product);
			ordersList.addAll(order);
		}
		int totalPage = ordersList.size()/6;
		if(ordersList.size()%6!=0)
		{
			totalPage++;
		}
		int nextPage = page + 1;
		int prePage = page - 1;
		int range=6*page;
		if(range>ordersList.size())
		{
			range=ordersList.size();
		}
		List<Order> orders = ordersList.subList(6 * (page - 1), range);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("prePage", prePage);
		model.addAttribute("orders", orders);
		return "seller/customer";
	}

	@GetMapping("/customer-details/{orderId}")
	public String singleCustomer(@PathVariable("orderId") int orderId, Model model) {
		Order order = this.orderRepo.findById(orderId).get();
		model.addAttribute("title", "Customer Details");
		model.addAttribute("order", order);
		return "seller/single-customer";
	}

	@GetMapping("/change-password")
	public String renderChangePassword(Model model) {
		model.addAttribute("title", "Change Password");
		return "seller/change-password-form";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPass, @RequestParam("newPass") String newPass,
			Model model, HttpSession sess) {
		User user = (User) model.getAttribute("user");
		if (oldPass.isEmpty()) {
			sess.setAttribute("msg", new Message("error", "Please Enter Old Password"));
			return "seller/change-password-form";
		}
		if (newPass.isEmpty()) {
			sess.setAttribute("msg", new Message("error", "Please Enter New Password"));
			return "seller/change-password-form";
		}
		if (this.passEncoder.matches(oldPass, user.getPassword())) {
			user.setPassword(passEncoder.encode(newPass));
			userRepo.save(user);
			sess.setAttribute("msg", new Message("success", "Password Changed"));

		} else {
			sess.setAttribute("msg", new Message("error", "Please Enter Correct Old Password"));
		}
		model.addAttribute("title", "Change Password");
		return "seller/dashboard";
	}
}
