package com.procyk.industries.module;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

class BotModuleTest {
    @Test
    void testTokenPath() {

        Path path = null;
        try {
            path = Paths.get(getClass().getClassLoader().getResource("token").getPath());
        } catch (Exception e) {
            fail("Token file does not exist in project directory");
        }
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
