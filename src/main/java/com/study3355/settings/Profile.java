package com.study3355.settings;

import com.study3355.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Profile {

    private String bio;
    private String url;
    private String occupation;
    private String location;

    public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
    }
}
