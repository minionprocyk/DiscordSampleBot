package com.procyk.industries.audio.record;

import clojure.lang.RT;
import clojure.lang.Var;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class CreateWaveFileTask implements Callable<String> {
    private static final Logger logger = Logger.getLogger(CreateWaveFileTask.class.getName());
    private final ByteArrayOutputStream data;
    private String filename;

    public CreateWaveFileTask(ByteArrayOutputStream byteArrayOutputStream, String filename) {
        this.data=byteArrayOutputStream;
        this.filename=filename;
    }

    /**
     * Invoke clojure script to generate a wave file using the provided data into a formatted WAVE file
     * @return
     * @throws Exception
     */
    @Override
    public String call() throws Exception {
        String ret = "";
        try {
            RT.loadResourceScript("runme.clj");
            Var foo = RT.var("com.procyk.industries.audio.record.runme", "writeToFile");
            Object Result = foo.invoke(data,filename);
            logger.info("Result from clojure: "+Result);
        }catch(Exception e) {
            logger.info("Failed to recognize speech");
            e.printStackTrace();
        }
        return ret;
    }
}
