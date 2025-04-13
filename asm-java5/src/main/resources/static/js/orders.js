import createToast, { toastComponent } from "./toast.js";

// STATIC DATA
const currentUserIdMetaTag = document.querySelector("meta[name='currentUserId']");
console.log("User ID:", currentUserIdMetaTag?.content);

// MESSAGES
const FAILED_OPERATION_MESSAGE = "Đã có lỗi truy vấn!";

// ROOTS/ELEMENTS
const ordersTableRootElement = document.querySelector("#orders-table");
const paginationRootElement = document.querySelector("#pagination");

// COMPONENTS
function orderRowComponent(order, index) {
    // Tính tổng cộng số tiền cho đơn hàng
    const productPriceAfterDiscount = order.products.reduce((total, product) => {
        const priceAfterDiscount = product.giamGia === 0
            ? product.gia * product.soLuong
            : (product.gia * (100 - product.giamGia) / 100) * product.soLuong;
        return total + priceAfterDiscount;
    }, 0);
    const deliveryPrice = order.giaohang === "Giao tiêu chuẩn" ? 15000 : 50000;
    const totalPrice = productPriceAfterDiscount + deliveryPrice;

    // Hiển thị danh sách sản phẩm trong đơn hàng
    const productsHtml = order.products.map(product =>
        `${product.tenSanPham} (x${product.soLuong})`
    ).join(', ');
    const pricesHtml = order.products.map(product =>
        `${product.gia.toLocaleString('vi-VN')} VNĐ`
    ).join('<br>');
    const discountsHtml = order.products.map(product =>
        `${product.giamGia.toLocaleString('vi-VN')} %`
    ).join('<br>');
    const quantitiesHtml = order.products.map(product =>
        `${product.soLuong}`
    ).join('<br>');

    return `
        <tr>
            <td>${productsHtml}</td>
            <td>${pricesHtml}</td>
            <td>${discountsHtml}</td>
            <td>${quantitiesHtml}</td>
            <td>${order.trangThai}</td>
            <td>${order.hoten}</td>
            <td>${order.sdt}</td>
            <td>${order.diaChi}</td>
            <td>${order.giaohang}</td>
            <td>${new Date(order.ngayTao).toLocaleDateString('vi-VN')}</td>
            <td>${new Intl.NumberFormat('vi-VN').format(totalPrice)} VNĐ</td>
            
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

function paginationComponent(currentPage, totalPages) {
    let html = `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">Previous</a>
        </li>
    `;

    for (let i = 0; i < totalPages; i++) {
        html += `
            <li class="page-item ${currentPage === i ? 'active' : ''}">
                <a class="page-link" href="#" data-page="${i}">${i + 1}</a>
            </li>
        `;
    }

    html += `
        <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">Next</a>
        </li>
    `;

    return html;
}

// UTILS
async function _fetchGetOrders(trangthai = '', page = 0, size = 10) {
    try {
        const url = `/orders?userId=${currentUserIdMetaTag.content}&page=${page}&size=${size}${trangthai ? `&trangthai=${encodeURIComponent(trangthai)}` : ''}`;
        const response = await fetch(url, {
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
    groupedOrders: [],
    currentPage: 0,
    totalPages: 0,
    initState: async (trangthai = '', page = 0) => {
        if (!currentUserIdMetaTag || !currentUserIdMetaTag.content) {
            ordersTableRootElement.innerHTML = '<p>Vui lòng đăng nhập để xem đơn hàng.</p>';
            return;
        }

        ordersTableRootElement.innerHTML = loadingComponent();

        const [status, data] = await _fetchGetOrders(trangthai, page);
        if (status === 200) {
            state.orders = data.orders || [];
            state.currentPage = data.currentPage;
            state.totalPages = data.totalPages;

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
                        giaohang: order.giaohang,
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
    if (paginationRootElement) {
        paginationRootElement.innerHTML = paginationComponent(state.currentPage, state.totalPages);

        // Thêm sự kiện cho các nút phân trang
        paginationRootElement.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(e.target.getAttribute('data-page'));
                if (!isNaN(page)) {
                    const selectedStatus = document.querySelector('input[name="statusFilter"]:checked').value;
                    state.initState(selectedStatus, page);
                }
            });
        });
    }
}

// EVENT LISTENERS
const statusFilters = document.querySelectorAll('input[name="statusFilter"]');
statusFilters.forEach(filter => {
    filter.addEventListener('change', () => {
        const selectedStatus = document.querySelector('input[name="statusFilter"]:checked').value;
        state.initState(selectedStatus, 0);
    });
});

// MAIN
if (currentUserIdMetaTag) {
    void state.initState();
}