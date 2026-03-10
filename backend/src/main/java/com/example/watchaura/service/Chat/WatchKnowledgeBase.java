package com.example.watchaura.service.Chat;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class WatchKnowledgeBase {

    private final Map<String, String> responses;
    private final String[] defaultResponses;
    private final Random random;

    public WatchKnowledgeBase() {
        this.responses = initializeResponses();
        this.defaultResponses = initializeDefaultResponses();
        this.random = new Random();
    }

    public String getResponse(String userMessage) {
        String message = userMessage.toLowerCase().trim();

        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        if (containsAny(message, new String[]{"giá", "rẻ", "tiền", "ngân sách"})) {
            return "Để tư vấn giá tốt nhất, bạn vui lòng cho biết:\n" +
                    "• **Ngân sách** cụ thể (VD: dưới 2 triệu)\n" +
                    "• **Loại đồng hồ** (nam/nữ, thể thao/công sở)\n" +
                    "• **Thương hiệu** yêu thích\n\n" +
                    " Hoặc click **câu hỏi mẫu** bên dưới để được tư vấn nhanh!";
        }

        if (containsAny(message, new String[]{"tư vấn", "gợi ý", "chọn", "mua"})) {
            return "Tôi sẽ tư vấn đồng hồ phù hợp cho bạn! Hãy cho biết:\n" +
                    "• **Mục đích sử dụng**: Đi làm, thể thao, dạo phố\n" +
                    "• **Ngân sách**: Từ 500k đến trên 10 triệu\n" +
                    "• **Phong cách**: Cổ điển, hiện đại, thể thao\n" +
                    "• **Size**: Nhỏ gọn hay to nổi bật\n\n" +
                    " **Tip**: Thử hỏi \"Casio dưới 1 triệu\" hoặc \"Orient tốt nhất\"";
        }

        if (containsAny(message, new String[]{"chăm sóc", "bảo dưỡng", "vệ sinh"})) {
            return "**Cách chăm sóc đồng hồ đúng cách:**\n" +
                    "• **Tránh từ trường** mạnh (loa, điện thoại)\n" +
                    "• **Không để nước vào** nếu không chống nước\n" +
                    "• **Lau khăn mềm** hàng tuần\n" +
                    "• **Bảo dưỡng định kỳ** 2-3 năm/lần\n" +
                    "• **Cất nơi khô ráo**, tránh ánh sáng trực tiếp\n\n" +
                    "⚠ **Lưu ý**: Đồng hồ cơ cần lên dây đều đặn!";
        }

        return defaultResponses[random.nextInt(defaultResponses.length)];
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> initializeResponses() {
        Map<String, String> map = new HashMap<>();

        map.put("dưới 500k", "**Đồng hồ dưới 500k** - Lựa chọn phổ thông:\n" +
                "• **Casio F-91W** (180k) - huyền thoại, bền bỉ\n" +
                "• **Casio AE-1000W** (350k) - đa chức năng, chống nước\n" +
                "• **Q&Q VP46J** (250k) - thiết kế đơn giản\n" +
                "• **Casio MW-240** (200k) - analog cơ bản\n" +
                "• **Skmei 1016** (150k) - thể thao, giá rẻ\n\n" +
                "Tất cả đều **bảo hành 1 năm**, phù hợp học sinh - sinh viên.");

        map.put("dưới 1 triệu", "**Top đồng hồ dưới 1 triệu** đáng mua:\n" +
                "• **Casio AE-1200WHD** (650k) - \"Casio Royale\", retro\n" +
                "• **Casio W-736H** (450k) - năng lượng mặt trời\n" +
                "• **Orient FAB00005D** (850k) - máy cơ entry level\n" +
                "• **Casio MTP-1302** (550k) - đơn giản, lịch sự\n" +
                "• **Citizen Q&Q A212** (750k) - chất lượng Nhật\n\n" +
                "**Gợi ý**: Casio nếu thích điện tử, Orient nếu muốn máy cơ!");

        map.put("dưới 2 triệu", "**Đồng hồ dưới 2 triệu** - Sweet spot:\n" +
                "• **Casio Edifice EFV-100** (1.8tr) - chronograph, thép không gỉ\n" +
                "• **Orient FAC00002D** (1.5tr) - máy cơ, power reserve 40h\n" +
                "• **Citizen NH8350** (1.6tr) - automatic, exhibition caseback\n" +
                "• **Seiko SNK805** (1.2tr) - field watch cổ điển\n" +
                "• **Casio MDV-106** (1.4tr) - \"Duro\", chống nước 200m\n\n" +
                "**Chất lượng tăng rõ rệt** so với phân khúc dưới 1tr!");

        map.put("casio", "**Casio** - Thương hiệu Nhật Bản nổi tiếng:\n" +
                "• **G-Shock**: Bền bỉ, chống sốc\n" +
                "• **Edifice**: Thiết kế thể thao sang trọng\n" +
                "• **Pro Trek**: Đồng hồ leo núi, cảm biến\n" +
                "• **Baby-G**: Dành cho nữ, nhiều màu sắc\n\n" +
                " **Ưu điểm**: Pin 7-10 năm, chống nước tốt, giá hợp lý");

        map.put("g-shock", "**G-Shock** - Dòng đồng hồ huyền thoại:\n" +
                "• **DW-5600E** (1.6tr) - G-Shock gốc, square case\n" +
                "• **GA-2100** (2.1tr) - \"CasiOak\", mỏng nhất\n" +
                "• **DW-6900** (1.8tr) - 3 mắt, phổ biến\n" +
                "• **GW-M5610** (2.5tr) - solar, radio sync\n\n" +
                " **Đặc điểm**: Chống sốc 10G, chống nước 200m, bền 20+ năm");

        map.put("orient", "**Orient** - Thương hiệu máy cơ Nhật:\n" +
                "• **Bambino**: Dress watch cổ điển\n" +
                "• **Mako/Ray**: Dive watch chuyên nghiệp\n" +
                "• **Orient Star**: Dòng cao cấp\n" +
                "• **Flight**: Pilot watch, crown lớn\n\n" +
                "⚙ **Ưu điểm**: Máy cơ in-house, giá trị tốt nhất phân khúc");

        map.put("bambino", "**Orient Bambino** - Dress watch kinh điển:\n" +
                "• **Version 1** (2.8tr) - dome crystal cổ điển\n" +
                "• **Version 2** (2.9tr) - Roman numerals\n" +
                "• **Version 3** (2.7tr) - stick indices\n" +
                "• **Version 4** (3.1tr) - small seconds\n\n" +
                " **Phong cách**: Vintage 1960s, formal wear hoàn hảo");

        map.put("citizen", "**Citizen** - Công nghệ Eco-Drive:\n" +
                "• **Eco-Drive**: Pin vĩnh viễn từ ánh sáng\n" +
                "• **Promaster**: Đồng hồ chuyên nghiệp\n" +
                "• **Satellite Wave**: GPS, atomic time\n" +
                "• **Chronomaster**: Siêu chính xác\n\n" +
                " **Đặc biệt**: Không cần thay pin, thân thiện môi trường");

        map.put("eco-drive", "**Citizen Eco-Drive** - Năng lượng ánh sáng:\n" +
                "• **BM7360** (2.5tr) - sport, dây thép\n" +
                "• **AT2144** (4.2tr) - chronograph\n" +
                "• **BN0151** (4.5tr) - dive 300m\n" +
                "• **FE1081** (2.3tr) - nữ, elegant\n\n" +
                " **Công nghệ**: 6 tháng dự trữ trong tối, pin 20+ năm");

        map.put("seiko", "**Seiko** - Thương hiệu đồng hồ hàng đầu:\n" +
                "• **Seiko 5**: Entry automatic, giá tốt\n" +
                "• **Prospex**: Đồng hồ thể thao chuyên nghiệp\n" +
                "• **Presage**: Dress watch, cocktail time\n" +
                "• **Grand Seiko**: Luxury, Swiss level\n\n" +
                " **Nổi tiếng**: Máy cơ chất lượng, lịch sử 140+ năm");

        map.put("seiko 5", "**Seiko 5** - Automatic giá tốt nhất:\n" +
                "• **SNK805** (1.2tr) - field watch, green\n" +
                "• **SNK809** (1.1tr) - all black military\n" +
                "• **SRPD55** (1.8tr) - sports, modern\n" +
                "• **SNZG13** (1.6tr) - pilot style\n\n" +
                " **Seiko 5 Rule**: Automatic, day-date, under $100 (khi ra mắt)");

        return map;
    }

    private String[] initializeDefaultResponses() {
        return new String[]{
                "Tôi chuyên tư vấn đồng hồ theo mọi **mức giá** và **thương hiệu**. Hãy thử hỏi:\n" +
                        "• \"Casio dưới 2 triệu tốt nhất\"\n" +
                        "• \"Orient Bambino giá tốt\"\n" +
                        "• \"Đồng hồ thể thao giá rẻ\"\n\n" +
                        "Hoặc click **câu hỏi mẫu** để được hỗ trợ nhanh chóng!",

                "Để tư vấn phù hợp, bạn có thể hỏi cụ thể:\n" +
                        "• **Theo giá**: \"Dưới 1 triệu\", \"Từ 2-5 triệu\"\n" +
                        "• **Theo thương hiệu**: \"Casio giá rẻ\", \"Citizen tốt nhất\"\n" +
                        "• **Theo loại**: \"Nam công sở\", \"Nữ thời trang\"\n\n" +
                        "Tôi có **database 100+ mẫu đồng hồ** với giá cập nhật!",

                "Tôi có thể giúp bạn tìm đồng hồ phù hợp! Hãy nói rõ:\n" +
                        "• **Ngân sách cụ thể** (VD: dưới 3 triệu)\n" +
                        "• **Thương hiệu quan tâm** (Casio, Citizen, Orient, Seiko)\n" +
                        "• **Mục đích sử dụng** (công việc, thể thao, hàng ngày)\n\n" +
                        " **Tip**: Click vào nút câu hỏi bên dưới để được tư vấn nhanh!"
        };
    }
}