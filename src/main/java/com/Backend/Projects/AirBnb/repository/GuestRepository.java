package com.Backend.Projects.AirBnb.repository;

import com.Backend.Projects.AirBnb.entities.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest, Long> {
}