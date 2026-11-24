package eventmanagement.util;

import eventmanagement.entity.Event;
import eventmanagement.exception.ForbiddenException;

/**
 * Các quy tắc phân quyền theo sự kiện dùng chung cho Schedule và Ticket.
 * Luôn dựa vào CurrentUser (session), không bao giờ tin id do client gửi lên.
 * Phải gọi bên trong transaction vì event.getOrganizer() là quan hệ LAZY.
 */
public final class AccessRules {

    public static final String EVENT_APPROVED = "APPROVED";

    private AccessRules() {
    }

    /**
     * Người xem có được xem dữ liệu của sự kiện này không?
     * - Sự kiện APPROVED: ai cũng xem được (kể cả chưa đăng nhập).
     * - Sự kiện chưa duyệt/bị từ chối: chỉ Admin và Organizer chủ sự kiện.
     */
    public static boolean canView(Event event) {
        if (EVENT_APPROVED.equals(event.getStatus())) {
            return true;
        }
        if (!CurrentUser.isAuthenticated()) {
            return false;
        }
        return CurrentUser.isAdmin() || isOwner(event);
    }

    /** Người đang đăng nhập có phải Organizer sở hữu sự kiện này không? */
    public static boolean isOwner(Event event) {
        return CurrentUser.isAuthenticated()
                && CurrentUser.isOrganizer()
                && event.getOrganizer().getId().equals(CurrentUser.getId());
    }

    /** Chỉ chủ sự kiện mới được thao tác; ngược lại ném ForbiddenException (HTTP 403). */
    public static void requireOwner(Event event) {
        if (!isOwner(event)) {
            throw new ForbiddenException("Bạn chỉ được thao tác trên sự kiện của chính mình.");
        }
    }
}
