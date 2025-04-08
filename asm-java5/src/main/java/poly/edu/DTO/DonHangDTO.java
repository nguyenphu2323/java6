package poly.edu.DTO;

import java.sql.Date;

public class DonHangDTO {
    private Integer idHoadon;
    private String trangThai;
    private String diaChi;
    private String hoten;
    private String sdt;
    private Integer soLuong;
    private String tenSanPham;
    private Double gia;
    private Double giamGia;
    private Date ngayTao;

    // Constructor
    public DonHangDTO(Integer idHoadon, String tenSanPham, Double gia, Double giamGia, Integer soLuong,
                      String trangThai, String hoten, String sdt, String diaChi, Date ngayTao) {
        this.idHoadon = idHoadon;
        this.tenSanPham = tenSanPham;
        this.gia = gia;
        this.giamGia = giamGia;
        this.soLuong = soLuong;
        this.trangThai = trangThai;
        this.hoten = hoten;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.ngayTao = ngayTao;
    }

    // Getters and Setters
    public Integer getIdHoadon() {
        return idHoadon;
    }

    public void setIdHoadon(Integer idHoadon) {
        this.idHoadon = idHoadon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getHoten() {
        return hoten;
    }

    public void setHoten(String hoten) {
        this.hoten = hoten;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public Double getGia() {
        return gia;
    }

    public void setGia(Double gia) {
        this.gia = gia;
    }

    public Double getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(Double giamGia) {
        this.giamGia = giamGia;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }
}