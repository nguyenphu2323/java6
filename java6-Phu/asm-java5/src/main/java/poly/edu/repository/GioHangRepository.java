package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import poly.edu.entity.GioHang;

public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
	@Modifying
	@Transactional
	@Query("DELETE FROM GioHang g WHERE g.users.idUser = :idUser")
	void deleteByUserId(@Param("idUser") String idUser);

	GioHang findByUsers_IdUser(String idUser);
}
