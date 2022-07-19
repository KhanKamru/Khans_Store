package com.smart.dao;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Order;
import com.smart.entities.Product;

public interface OrderRepo extends JpaRepository<Order, Integer>{

//	@Query("select o from Order o where o.product=:product")
//	public Page<Order> getOrder(@Param("product") Product product,Pageable pageable);
	
	@Query("select o from Order o where o.product=:product")
	public List<Order> getOrder(@Param("product") Product product);

}
