package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.Address;

public interface AddressRepo extends JpaRepository<Address, Integer> {

}
