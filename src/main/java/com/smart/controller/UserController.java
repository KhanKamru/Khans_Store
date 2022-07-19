package com.smart.controller;

import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.smart.dao.AddressRepo;
import com.smart.dao.CartRepository;
 import com.smart.dao.OrderRepo;
import com.smart.dao.ProductRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Address;
import com.smart.entities.Cart;
import com.smart.entities.Image;
import com.smart.entities.Message;
import com.smart.entities.Order;
import com.smart.entities.Product;
import com.smart.entities.User;

@Controller
@RequestMapping("/customer")
public class UserController {

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private CartRepository cartRepo;

	@Autowired
	private AddressRepo addRepo;

	@Autowired
	private OrderRepo orderRepo;

	@Autowired
	private BCryptPasswordEncoder passEncoder;
	@ModelAttribute("user")
	public User user(Model model,Principal principle) {
		User user =this.userRepo.getUserByEmail(principle.getName());
		return user;
	}

	@ModelAttribute
	public void userData(Model model) {
		User user=(User)model.getAttribute("user");
		model.addAttribute("cart",user.getCartItems().size());
		model.addAttribute("logged",Boolean.TRUE);

	}
	@GetMapping("/add-to-cart/{productId}")
	public String addToCart(@PathVariable("productId") int id, HttpSession sess, Model model) {
		Product product = this.productRepo.findById(id).get();
		User user = (User) model.getAttribute("user");
		Cart c = this.cartRepo.findByProductId(id);
		if (c == null) {
			Cart cart = new Cart();
			cart.setProduct(product);
			cart.setUser(user);
			this.cartRepo.save(cart);
		} else {
			int totalQuantity = c.getQunatity() + 1;
			c.setQunatity(totalQuantity);
			this.cartRepo.save(c);
		}

		sess.setAttribute("msg", new Message("success", "Added To Cart"));
		return "redirect:/customer/cart";

	}

	@GetMapping("/cart")
	public String cart(Model model) {
		User user = (User) model.getAttribute("user");
		double total = 0;
		for (Cart cart : user.getCartItems()) {
			total += (cart.getProduct().getPrice() * cart.getQunatity());
		}
		
		model.addAttribute("items", user.getCartItems());
		model.addAttribute("total", total);
		model.addAttribute("title", "Your Cart");
		return "user/cart";
	}

	@GetMapping("/remove-from-cart/{cartId}")
	public String removeFromCart(@PathVariable("cartId") int id, HttpSession sess) {
		this.cartRepo.deleteById(id);
		sess.setAttribute("msg", new Message("success", "Removed From cart"));
		return "redirect:/customer/cart";
	}

	@GetMapping("/order/{productId}")
	public String showProduct(@PathVariable("productId") int id, HttpSession sess, Model model) {
		User user = (User) model.getAttribute("user");
		Address add = user.getAddress();
		if (add == null) {
			add = new Address();
		}
		model.addAttribute("address", add);
		Product product = this.productRepo.findById(id).get();
		model.addAttribute("product", product);
		model.addAttribute("title","Book "+product.getName());
		return "user/order";
	}

	@PostMapping("/book/{productId}")
	public String book(@Valid @ModelAttribute("address") Address add,BindingResult result, @PathVariable("productId") int pId,
			@RequestParam("quantity") int quantity, HttpSession sess, Model model) {
		if(result.hasErrors())
		{
			return "user/order";
		}
		try {
			User user = (User) model.getAttribute("user");
			//int addId = user.getAddress().getId();
//			Address address = this.addRepo.findById(addId).get();
			Address address=user.getAddress();
			if (address != null) {
				int addId = user.getAddress().getId();
				address = add;
				address.setId(addId);
				
			}
			address=add;
			address.setUser(user);
			this.addRepo.save(address);
			Product product = this.productRepo.findById(pId).get();
			if (product.getTotalQuantity() < quantity) {
				throw new Exception(quantity + "is not available");
			}
			int newQuantity = product.getTotalQuantity() - quantity;
			product.setTotalQuantity(newQuantity);
			product = this.productRepo.save(product);
			Order order = new Order();
			order.setQuantity(quantity);
			order.setUser(user);
			order.setProduct(product);
			this.orderRepo.save(order);
			sess.setAttribute("msg", new Message("success", "Order Successfull"));

		} catch (Exception ex) {
			sess.setAttribute("msg", new Message("error", ex.getMessage()));
			ex.printStackTrace();
			return "redirect:/customer/order/" + pId;
		}
		return "redirect:/customer/my-orders/1";
	}

	@GetMapping("/my-orders/{page}")
	public String myorders(@PathVariable("page")int page, Model model) {
		User user = (User) model.getAttribute("user");
		List<Order> ordersList = user.getOrders();
		model.addAttribute("title", "My Orders");
//		for (Order order : user.getOrders()) {
//			System.out.println(order);
//		}
		
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
		return "user/myorders";
	}
	@GetMapping("/change-password")
	public String renderChangePassword(Model model) {
		model.addAttribute("title", "Change Password");
		return "user/change-password-form";
	}
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPass") String oldPass,@RequestParam("newPass") String newPass,Model model,HttpSession sess)
	{
		User user=(User)model.getAttribute("user");
		if (oldPass.isEmpty()) {
			sess.setAttribute("msg", new Message("error", "Please Enter Old Password"));
			return "user/change-password-form";
		}
		if (newPass.isEmpty()) {
			sess.setAttribute("msg", new Message("error", "Please Enter New Password"));
			return "user/change-password-form";
		}

		if(this.passEncoder.matches(oldPass, user.getPassword()))
		{
			user.setPassword(passEncoder.encode(newPass));
			userRepo.save(user);
			sess.setAttribute("msg", new Message("success", "Password Changed"));
		}
		else {
			sess.setAttribute("msg", new Message("error", "Please Enter Correct Old Password"));
		}
		model.addAttribute("title", "Change Password");
		return "user/change-password-form";
	}
}
