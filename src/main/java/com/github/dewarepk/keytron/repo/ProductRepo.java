package com.github.dewarepk.keytron.repo;

import com.github.dewarepk.keytron.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProductRepo extends MongoRepository<Product, String> {

    Optional<Product> findByCode(String code);
}
