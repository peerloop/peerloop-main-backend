package com.peerloop.peerloopapp;

import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PeerloopAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeerloopAppApplication.class, args);
    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Locale.setDefault(Locale.KOREA);
    }

}
