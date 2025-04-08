import createToast, { toastComponent } from "./toast.js";

// STATIC DATA
const currentUserIdMetaTag = document.querySelector("meta[name='currentUserId']");
console.log("User ID:", currentUserIdMetaTag?.content);

// MESSAGES
const FAILED_OPERATION_MESSAGE = "Đã có lỗi truy vấn!";

// ROOTS/ELEMENTS
const ordersTableRootElement = document.querySelector("#orders-table");

// COMPONENTS
function orderRowComponent(order, index) {
    // Tính tổng cộng số tiền cho đơn hàng
    const productPriceAfterDiscount = order.products.reduce((total, product) => {
        const priceAfterDiscount = product.giamGia === 0 
            ? product.gia * product.soLuong 
            : (product.gia * (100 - product.giamGia) / 100) * product.soLuong;
        return total + priceAfterDiscount;
    }, 0);
    const deliveryPrice = order.giaoHang === "Giao tiêu chuẩn" ? 15000 : 15000; // Phí vận chuyển
    const totalPrice = productPriceAfterDiscount + deliveryPrice;

    // Hiển thị danh sách sản phẩm trong đơn hàng
    const productsHtml = order.products.map(product => `
        ${product.tenSanPham} (${product.soLuong} x ${product.gia.toLocaleString('vi-VN')} VNĐ, Giảm: ${product.giamGia.toLocaleString('vi-VN')} VNĐ)
    `).join('<br>');

    return `
        <tr>
            <td>${productsHtml}</td>
            <td>${order.products.map(p => p.gia.toLocaleString('vi-VN')).join('<br>')}</td>
            <td>${order.products.map(p => p.giamGia.toLocaleString('vi-VN')).join('<br>')}</td>
            <td>${order.products.map(p => p.soLuong).join('<br>')}</td>
            <td>${order.trangThai}</td>
            <td>${order.hoten}</td>
            <td>${order.sdt}</td>
            <td>${order.diaChi}</td>
            <td>${order.giaoHang}</td>
            <td>${new Date(order.ngayTao).toLocaleDateString('vi-VN')}</td>
            <td>${new Intl.NumberFormat('vi-VN').format(totalPrice)} VNĐ</td>
            <td>
                <a href="/user/order-details?orderId=${order.idHoadon}" class="btn btn-primary btn-sm">Xem chi tiết</a>
            </td>
        </tr>
    `;
}

function ordersTableComponent(orderRowComponentsFragment) {
    if (state.groupedOrders.length === 0) {
        return `
            <div class="d-flex justify-content-center p-5 font-monospace">
                Bạn chưa có đơn hàng nào
            </div>
        `;
    }

    return `
        <div class="table-responsive-xl">
            <table class="table table-borderless">
                <thead class="text-muted">
                    <tr class="small text-uppercase">
                        <th scope="col">Tên sản phẩm</th>
                        <th scope="col">Giá</th>
                        <th scope="col">Giảm giá</th>
                        <th scope="col">Số lượng</th>
                        <th scope="col">Trạng thái</th>
                        <th scope="col">Họ tên</th>
                        <th scope="col">Số điện thoại</th>
                        <th scope="col">Địa chỉ</th>
                        <th scope="col">Giao hàng</th>
                        <th scope="col">Ngày tạo</th>
                        <th scope="col">Tổng cộng</th>
                        <th scope="col">Hành động</th>
                    </tr>
                </thead>
                <tbody>${orderRowComponentsFragment}</tbody>
            </table>
        </div>
    `;
}

function loadingComponent() {
    return `
        <div class="d-flex justify-content-center p-5">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Đang tải...</span>
            </div>
        </div>
    `;
}

// UTILS
async function _fetchGetOrders() {
    try {
        const response = await fetch(`/orders?userId=${currentUserIdMetaTag.content}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json",
            },
        });
        return [response.status, await response.json()];
    } catch (error) {
        console.error("Error fetching orders:", error);
        return [500, { message: "Lỗi kết nối đến server!" }];
    }
}

// STATE
const state = {
    orders: [],
    groupedOrders: [], // Danh sách đơn hàng đã nhóm
    initState: async () => {
        if (!currentUserIdMetaTag || !currentUserIdMetaTag.content) {
            ordersTableRootElement.innerHTML = '<p>Vui lòng đăng nhập để xem đơn hàng.</p>';
            return;
        }

        ordersTableRootElement.innerHTML = loadingComponent();

        const [status, data] = await _fetchGetOrders();
        if (status === 200) {
            state.orders = data.orders || [];
            // Nhóm đơn hàng theo idHoadon
            const grouped = {};
            state.orders.forEach(order => {
                if (!grouped[order.idHoadon]) {
                    grouped[order.idHoadon] = {
                        idHoadon: order.idHoadon,
                        trangThai: order.trangThai,
                        hoten: order.hoten,
                        sdt: order.sdt,
                        diaChi: order.diaChi,
                        giaoHang: order.giaoHang,
                        ngayTao: order.ngayTao,
                        products: []
                    };
                }
                grouped[order.idHoadon].products.push({
                    tenSanPham: order.tenSanPham,
                    gia: order.gia,
                    giamGia: order.giamGia,
                    soLuong: order.soLuong
                });
            });
            state.groupedOrders = Object.values(grouped);
            render();
        } else {
            createToast(toastComponent(data.message || FAILED_OPERATION_MESSAGE, "danger"));
            state.orders = [];
            state.groupedOrders = [];
            render();
        }
    },
};

// RENDER
function render() {
    const orderRowComponentsFragment = state.groupedOrders.map((order, index) => orderRowComponent(order, index)).join("");
    ordersTableRootElement.innerHTML = ordersTableComponent(orderRowComponentsFragment);
}

// MAIN
if (currentUserIdMetaTag) {
    void state.initState();
}