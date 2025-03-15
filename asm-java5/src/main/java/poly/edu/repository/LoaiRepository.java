package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import poly.edu.entity.Loai;

import java.util.Optional;


public interface LoaiRepository extends JpaRepository<Loai, Integer>{

	Optional<Loai> findByTenLoai(String tenLoai);
}
