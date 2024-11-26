package me.t65.reportgenapi.utils;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DateServiceImpl implements DateService {
    @Override
    public Instant getCurrentInstant() {
        return Instant.now();
    }
}
