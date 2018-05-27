package com.procyk.industries.module;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

public class BotModuleTest {
    @Test
    public void testTokenPath() {

        Path path = Paths.get(getClass().getClassLoader().getResource("token").getPath());
        Properties properties = new Properties();
        String result="";
        try {
            properties.load(Files.newInputStream(path));
            result = properties.getProperty("token");
        } catch (IOException e) {
            fail("Failed to read token file");
        }
        System.out.println(result);
    }
}
