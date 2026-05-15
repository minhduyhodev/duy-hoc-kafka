package com.example.weatherkafka.service;

import com.example.weatherkafka.model.WeatherResult;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORECAST_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.mail.from-address:no-reply@weather.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public void sendWeatherForecast(WeatherResult result) {
        if (result.getRecipientEmail() == null || result.getRecipientEmail().isBlank()) {
            log.info("Skipping mail for city {} because no recipient email was provided", result.getCity());
            return;
        }

        if (!mailEnabled) {
            log.info("Mail sending is disabled. Forecast for city {} was not emailed to {}", result.getCity(), result.getRecipientEmail());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(result.getRecipientEmail());
            helper.setSubject("Du bao thoi tiet cho " + safeText(result.getCity()));
            helper.setText(buildPlainTextBody(result), buildHtmlBody(result));

            mailSender.send(message);
            log.info("Weather forecast email sent to {}", result.getRecipientEmail());
        } catch (Exception ex) {
            log.error("Failed to send weather forecast email to {}", result.getRecipientEmail(), ex);
        }
    }

    private String buildPlainTextBody(WeatherResult result) {
        return """
                Xin chao %s,

                Day la du bao thoi tiet cua ban:
                - Thanh pho: %s
                - Nhiet do: %d deg C
                - Trang thai: %s
                - Thoi gian du bao: %s

                Cam on ban da su dung Weather Kafka Demo.
                """.formatted(
                displayName(result.getRequestedBy()),
                safeText(result.getCity()),
                result.getTemperature(),
                safeText(result.getStatus()),
                formatForecastTime(result.getForecastTime())
        );
    }

    private String buildHtmlBody(WeatherResult result) {
        String requester = escapeHtml(displayName(result.getRequestedBy()));
        String city = escapeHtml(safeText(result.getCity()));
        String status = escapeHtml(safeText(result.getStatus()));
        String forecastTime = escapeHtml(formatForecastTime(result.getForecastTime()));
        String statusColor = resolveStatusColor(result.getStatus());

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <body style="margin:0;padding:0;background:linear-gradient(180deg,#e0f2fe 0%%,#f8fafc 100%%);font-family:Arial,sans-serif;color:#0f172a;">
                  <div style="padding:32px 16px;">
                    <div style="max-width:640px;margin:0 auto;background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 20px 45px rgba(15,23,42,0.12);">
                      <div style="background:linear-gradient(135deg,#0284c7 0%%,#38bdf8 100%%);padding:32px 28px;color:#ffffff;">
                        <div style="font-size:13px;letter-spacing:1.4px;text-transform:uppercase;opacity:0.9;">Weather Kafka Demo</div>
                        <h1 style="margin:12px 0 8px;font-size:30px;line-height:1.2;">Du bao thoi tiet moi nhat</h1>
                        <p style="margin:0;font-size:15px;line-height:1.7;opacity:0.95;">Xin chao %s, duoi day la thong tin thoi tiet cho khu vuc ban vua yeu cau.</p>
                      </div>

                      <div style="padding:28px;">
                        <div style="background:linear-gradient(135deg,#f8fbff 0%%,#eef6ff 100%%);border:1px solid #dbeafe;border-radius:20px;padding:24px;">
                          <div style="display:inline-block;background:%s;color:#ffffff;border-radius:999px;padding:8px 14px;font-size:13px;font-weight:700;letter-spacing:0.3px;">
                            %s
                          </div>
                          <div style="margin-top:18px;font-size:18px;color:#475569;">Thanh pho</div>
                          <div style="font-size:32px;font-weight:700;line-height:1.2;color:#0f172a;">%s</div>
                          <div style="margin-top:18px;font-size:52px;font-weight:800;line-height:1;color:#0284c7;">%d<span style="font-size:24px;vertical-align:top;">&deg;C</span></div>
                        </div>

                        <div style="margin-top:24px;border:1px solid #e2e8f0;border-radius:18px;overflow:hidden;">
                          <div style="padding:16px 20px;background:#f8fafc;font-weight:700;color:#334155;">Chi tiet du bao</div>
                          <table role="presentation" style="width:100%%;border-collapse:collapse;">
                            <tr>
                              <td style="padding:14px 20px;border-top:1px solid #e2e8f0;color:#64748b;width:42%%;">Nguoi yeu cau</td>
                              <td style="padding:14px 20px;border-top:1px solid #e2e8f0;color:#0f172a;font-weight:600;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:14px 20px;border-top:1px solid #e2e8f0;color:#64748b;">Thoi gian du bao</td>
                              <td style="padding:14px 20px;border-top:1px solid #e2e8f0;color:#0f172a;font-weight:600;">%s</td>
                            </tr>
                            <tr>
                              <td style="padding:14px 20px;border-top:1px solid #e2e8f0;color:#64748b;">Email nhan</td>
                              <td style="padding:14px 20px;border-top:1px solid #e2e8f0;color:#0f172a;font-weight:600;">%s</td>
                            </tr>
                          </table>
                        </div>

                        <p style="margin:24px 0 0;font-size:14px;line-height:1.8;color:#475569;">
                          Email nay duoc gui tu he thong Weather Kafka Demo. Ban co the tiep tuc gui yeu cau moi de nhan du bao cap nhat nhanh hon.
                        </p>
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                requester,
                statusColor,
                status,
                city,
                result.getTemperature(),
                requester,
                forecastTime,
                escapeHtml(safeText(result.getRecipientEmail()))
        );
    }

    private String displayName(String requestedBy) {
        return safeText(requestedBy).isBlank() ? "ban" : safeText(requestedBy);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatForecastTime(LocalDateTime forecastTime) {
        return forecastTime == null ? "Dang cap nhat" : forecastTime.format(FORECAST_TIME_FORMATTER);
    }

    private String resolveStatusColor(String status) {
        String normalizedStatus = safeText(status).toLowerCase();
        if (normalizedStatus.contains("storm") || normalizedStatus.contains("rain")) {
            return "#0f766e";
        }
        if (normalizedStatus.contains("cloud")) {
            return "#64748b";
        }
        if (normalizedStatus.contains("sun") || normalizedStatus.contains("clear")) {
            return "#f59e0b";
        }
        return "#0284c7";
    }

    private String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value);
    }
}
