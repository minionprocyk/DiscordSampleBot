package com.procyk.industries.audio.record;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.procyk.industries.module.DeepspeechModelsPath;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public class SpeechToTextTask  implements Callable<String> {
    private final String filename;
    private final Path modelsPath;
    @AssistedInject
    public SpeechToTextTask(@Assisted String filename, @DeepspeechModelsPath Path modelsPath) {
        this.filename=filename;
        this.modelsPath=modelsPath;
    }
    @Override
    public String call() throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "deepspeech",
                "--model", modelsPath.resolve("output_graph.pbmm").toString(),
                "--lm", modelsPath.resolve("lm.binary").toString(),
                "--trie", modelsPath.resolve("trie").toString(),
                "--audio","/tmp/".concat(filename).concat(".wav"));
        Process process = processBuilder.start();
        process.waitFor();
        return new String(process.getInputStream().readAllBytes());
    }
}
