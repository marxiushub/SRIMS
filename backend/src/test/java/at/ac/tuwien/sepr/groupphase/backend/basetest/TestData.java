package at.ac.tuwien.sepr.groupphase.backend.basetest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface TestData {

    Long ID = 1L;
    String TEST_NEWS_TITLE = "Title";
    String TEST_NEWS_SUMMARY = "Summary";
    String TEST_NEWS_TEXT = "TestMessageText";
    LocalDateTime TEST_NEWS_PUBLISHED_AT =
        LocalDateTime.of(2019, 11, 13, 12, 15, 0, 0);

    String BASE_URI = "/api/v1";
    String MESSAGE_BASE_URI = BASE_URI + "/messages";

    String ADMIN_USER = "admin@email.com";
    List<String> ADMIN_PERMISSIONS = new ArrayList<>() {
        {
            add("EQUIPMENT_CREATE");
            add("EQUIPMENT_READ");
            add("EQUIPMENT_SEARCH");
            add("RESERVATION_SEARCH");
            add("EQUIPMENT_UPDATE");
            add("EQUIPMENT_DELETE");
            add("RESERVATION_READ");
            add("STAFF_CREATE");
            add("STAFF_READ");
            add("STAFF_UPDATE");
            add("STAFF_DELETE");
            add("CUSTOMERPROFILE_READ");
            add("STAFF");
        }
    };
    String DEFAULT_USER = "admin@email.com";
    List<String> USER_PERMISSIONS = new ArrayList<>() {
        {
            add("RESERVATION_CREATE");
            add("RESERVATION_READ");
            add("RESERVATION_SEARCH");
            add("RESERVATION_UPDATE");
            add("RESERVATION_DELETE");
            add("CUSTOMERPROFILE_CREATE");
            add("CUSTOMERPROFILE_READ");
            add("CUSTOMERPROFILE_UPDATE");
            add("CUSTOMERPROFILE_DELETE");
            add("EQUIPMENT_READ");
            add("EQUIPMENT_SEARCH");
            add("CUSTOMER_READ");
            add("CUSTOMER_UPDATE");
            add("CUSTOMER_DELETE");
        }
    };

}
