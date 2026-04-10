package com.Backend.Projects.AirBnb.repository;

import com.Backend.Projects.AirBnb.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}

