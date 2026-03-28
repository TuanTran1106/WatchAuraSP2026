package com.example.watchaura.service;

import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.HoaDonChiTiet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOrderConfirmation(HoaDon hoaDon) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(hoaDon.getEmail());
            helper.setSubject("Xác nhận đơn hàng #" + hoaDon.getMaDonHang() + " - WatchAura");
            helper.setFrom(fromEmail);

            String htmlContent = buildOrderConfirmationHtml(hoaDon);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Gửi email xác nhận thành công: {}", hoaDon.getEmail());

        } catch (MessagingException e) {
            log.error("❌ Lỗi gửi email xác nhận: {}", e.getMessage());
        }
    }

    private String buildOrderConfirmationHtml(HoaDon hoaDon) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("hoaDon", hoaDon);
        variables.put("orderDate", hoaDon.getNgayDat() != null
                ? hoaDon.getNgayDat().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                : "");
        variables.put("totalFormatted", formatCurrency(hoaDon.getTongTienThanhToan()));

        Context context = new Context();
        context.setVariables(variables);

        return templateEngine.process("email/order-confirmation", context);
    }

    public void sendOrderConfirmationFallback(String toEmail, String tenKhachHang, String maDonHang,
                                               BigDecimal tongTien, String ngayDat) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đơn hàng #" + maDonHang + " - WatchAura");
            helper.setFrom(fromEmail);

            Map<String, Object> variables = new HashMap<>();
            variables.put("tenKhachHang", tenKhachHang);
            variables.put("maDonHang", maDonHang);
            variables.put("tongTienFormatted", formatCurrency(tongTien));
            variables.put("ngayDat", ngayDat);

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/order-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Gửi email xác nhận thành công: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Lỗi gửi email: {}", e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount.doubleValue()).replace(",", ".") + " VND";
    }
}
