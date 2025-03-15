package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import poly.edu.entity.HoaDonChiTiet;
import poly.edu.entity.HoaDonChiTietId;

public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, HoaDonChiTietId>{
	@Modifying
	@Transactional
	@Query("DELETE FROM HoaDonChiTiet h WHERE h.hoaDon.users.idUser = :idUser")
	void deleteByUserId(@Param("idUser") String idUser);
}
