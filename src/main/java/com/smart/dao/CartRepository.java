package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

	public Cart findByProductId(int productId);
}
