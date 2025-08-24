package com.github.dewarepk.keytron.repo;


import com.github.dewarepk.keytron.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepo extends MongoRepository<AuditLog, String> { }