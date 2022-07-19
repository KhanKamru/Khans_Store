package com.smart.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.Image;


public interface ImageRepository extends JpaRepository<Image, Integer>{
	public List<Image> findByProductId(int pid);
}
