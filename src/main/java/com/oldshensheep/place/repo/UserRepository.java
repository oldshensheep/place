package com.oldshensheep.place.repo;

import com.oldshensheep.place.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}