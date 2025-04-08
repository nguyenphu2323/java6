package poly.edu.repository;

import java.util.List;
import java.util.Optional;

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

    List<HoaDon> findByUsersIdUser(String idUser);

    @Query("SELECT hd FROM HoaDon hd JOIN FETCH hd.users u JOIN FETCH hd.hoaDonChiTiets ct JOIN FETCH ct.sanPham sp WHERE u.idUser = :idUser")
    List<HoaDon> findHoaDonByUserIdWithDetails(@Param("idUser") String idUser);

    @Query("SELECT hd FROM HoaDon hd LEFT JOIN FETCH hd.hoaDonChiTiets ct LEFT JOIN FETCH ct.sanPham WHERE hd.idHoadon = :idHoadon")
    Optional<HoaDon> findHoaDonWithDetailsById(Integer idHoadon);
}