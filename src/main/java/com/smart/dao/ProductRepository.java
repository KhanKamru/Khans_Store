package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Image;
import com.smart.entities.Product;
import com.smart.entities.User;

public interface ProductRepository extends JpaRepository<Product, Integer>{
	
	@Query("Select p From Product p where p.name like %:value% or p.category like %:value%")
	public Page<Product> searchQuery(@Param("value") String value,Pageable pageAble);
	public Page<Product> findBySellerId(int id,Pageable pageAble);
	
	@Query("select p.images from Product p where p.id =:id")
	public List<Image> getAllImage(@Param("id") int id);
	
	@Query("select p from Product p where p.category =:category")
	public List<Product> getProductFromCategoty(@Param("category") String category);
	
	@Query(value="select * from Product  group by category limit 6",nativeQuery=true)
	public List<Product> getCategory();
	
	@Query("select p from Product p where p.seller =:seller")
	public Page<Product> getP(@Param("seller") User seller,Pageable pageable);
	
	@Query("select p from Product p where p.seller =:seller")
	public List<Product> getP(@Param("seller") User seller);
}
