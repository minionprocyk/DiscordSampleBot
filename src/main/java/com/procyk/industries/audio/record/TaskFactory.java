package com.procyk.industries.audio.record;

import java.io.ByteArrayOutputStream;

public interface TaskFactory {
    SpeechToTextTask create(String filename);
    CreateWaveFileTask create(ByteArrayOutputStream data, String filename);
}
