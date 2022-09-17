package com.study3355.settings.form;

import com.study3355.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

// bean 이 아니기 때문에 been 주입 X
@Data
public class Notifications {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;
}
