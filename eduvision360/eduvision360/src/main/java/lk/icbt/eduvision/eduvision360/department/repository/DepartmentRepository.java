package lk.icbt.eduvision.eduvision360.department.repository;

import lk.icbt.eduvision.eduvision360.department.model.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DepartmentRepository extends MongoRepository<Department, String> {

    boolean existsByCode(String code);

    boolean existsByName(String name);
}