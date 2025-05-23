package com.ucapital.sharkshub.investor.repository;

import com.ucapital.sharkshub.investor.model.Investor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface InvestorRepository extends MongoRepository<Investor, String> {

    boolean existsByName(String name);
    Optional<Investor> findByName(String name);

}