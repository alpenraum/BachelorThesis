package com.example.bachelorthesis.persistence.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.bachelorthesis.persistence.entities.template;

import java.util.List;

/**
 * @author Finn Zimmer
 */
@Dao
public interface TemplateDAO {

    @Insert
    void insert(template template);

    @Query("SELECT * from template ORDER BY score DESC")
    List<template> selectAllTemplates();

}
