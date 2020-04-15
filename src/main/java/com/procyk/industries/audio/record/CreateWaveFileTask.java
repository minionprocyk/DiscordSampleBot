package com.procyk.industries.audio.record;

import clojure.lang.RT;
import clojure.lang.Var;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

public class CreateWaveFileTask implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(CreateWaveFileTask.class.getName());
    private final ByteArrayOutputStream data;
    private String filename;

    @AssistedInject
    public CreateWaveFileTask(@Assisted ByteArrayOutputStream data, @Assisted String filename) {
        this.data=data;
        this.filename=filename;
    }

    /**
     * Invoke clojure script to generate a wave file using the provided data into a formatted WAVE file
     * */
    @Override
    public void run() {
//todo convert clojure into java?
//        FileOutputStream fileOutputStream = new FileOutputStream(new File("/tmp/"+filename));
//        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
//        byte[] bData = data.toByteArray();
//        logger.info("Writing file with {} bytes",bData.length);
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bData);
//        AudioInputStream inputStream = new AudioInputStream(byteArrayInputStream, format, bData.length);
//        AudioSystem.write(
//                inputStream,
//                AudioFileFormat.Type.WAVE,
//                fileOutputStream);
        try {
            RT.loadResourceScript("runme.clj");
            Var foo = RT.var("com.procyk.industries.audio.record.runme", "writeToFile");
            Object result = foo.invoke(data,filename);
            logger.info("Result from clojure: {}", result);
        }catch(Exception e) {
            logger.info("Failed to recognize speech", e);
        }
    }
}
