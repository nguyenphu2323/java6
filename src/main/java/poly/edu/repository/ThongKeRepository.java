package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import poly.edu.entity.SanPham;

@Repository
public interface ThongKeRepository extends JpaRepository<SanPham, Integer> {

 @Query(value = "SELECT l.ten_loai, COUNT(sp.id_sanpham), SUM(sp.soluong) " +
    "FROM SANPHAM sp JOIN LOAI l ON sp.id_loai = l.id_loai " +
    "GROUP BY l.ten_loai", nativeQuery = true)
List<Object[]> thongKeTonKho();

@Query(value = "SELECT hd.ngaytao, SUM(sp.gia * cthd.soluong) " +
    "FROM HOADON hd " +
    "JOIN HOADONCHITIET cthd ON hd.id_hoadon = cthd.id_hoadon " +
    "JOIN SANPHAM sp ON cthd.id_sanpham = sp.id_sanpham " +
    "GROUP BY hd.ngaytao ORDER BY hd.ngaytao DESC", nativeQuery = true)
List<Object[]> thongKeDoanhThuTheoNgay();

@Query(value = "SELECT sp.ten_sanpham, SUM(cthd.soluong) " +
    "FROM SANPHAM sp " +
    "JOIN HOADONCHITIET cthd ON sp.id_sanpham = cthd.id_sanpham " +
    "GROUP BY sp.ten_sanpham ORDER BY SUM(cthd.soluong) DESC", nativeQuery = true)
List<Object[]> thongKeSanPhamBanChay();
}