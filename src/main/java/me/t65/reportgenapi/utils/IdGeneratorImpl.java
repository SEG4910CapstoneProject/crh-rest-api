package me.t65.reportgenapi.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdGeneratorImpl implements IdGenerator {

    private final NoArgGenerator uuidGenerator;

    public IdGeneratorImpl() {
        uuidGenerator = Generators.timeBasedEpochGenerator();
    }

    @Override
    public UUID generateId() {
        return uuidGenerator.generate();
    }
}
