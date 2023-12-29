package com.budget.project.service.repository;

import com.budget.project.model.db.SubCategory;
import com.budget.project.model.db.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {

    @Query("select s from SubCategory s where s.hash = :hash and :user member of  s.parent.users")
    Optional<SubCategory> findByHashAndUser(String hash, User user);

    @Query("select s from SubCategory s where s.name = :name and :user member of  s.parent.users")
    Optional<SubCategory> findByNameAndUser(String name, User user);
}
