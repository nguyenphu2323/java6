package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import poly.edu.entity.HoaDon;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
	@Modifying
	@Transactional
	@Query("DELETE FROM HoaDon h WHERE h.users.idUser = :idUser")
	void deleteByUserId(@Param("idUser") String idUser);
}
