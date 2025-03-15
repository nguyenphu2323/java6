package poly.edu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.entity.Users;

public interface UsersRepository extends JpaRepository<Users, String> {
	Optional<Users> findBySdt(String sdt);
}