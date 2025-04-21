package poly.edu.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import poly.edu.entity.SanPham;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
	Page<SanPham> findByMotanganContainingIgnoreCase(String motangan, Pageable pageable);
	Page<SanPham> findAll(Pageable pageable);
	Page<SanPham> findByLoai_IdLoai(Integer id, Pageable pageable);

	@Query("SELECT s FROM SanPham s WHERE s.loai.idLoai = :idLoai")
	List<SanPham> findByLoai(@Param("idLoai") int idLoai);


	@Query("SELECT COALESCE(SUM(hdct.soluong), 0) "
			+ "FROM HoaDonChiTiet hdct "
			+ "JOIN hdct.hoaDon hd "
			+ "WHERE hd.trangthai = 'received' " + "AND hdct.sanPham.idSanpham = :idSanpham")
	int tongSoLuongSanPhamDaBanTheoId(@Param("idSanpham") Integer idSanpham);
}
