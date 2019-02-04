(ns com.procyk.industries.audio.record.runme
  (:require [clojure.java.io :as io])
  (:import [java.io ByteArrayInputStream
            ByteArrayOutputStream]
           [javax.sound.sampled
            AudioSystem
            AudioInputStream
            AudioFormat
            AudioFileFormat]))

(def file-type
  javax.sound.sampled.AudioFileFormat$Type/WAVE)

(defn input->audio [input-stream]
  (AudioSystem/getAudioInputStream input-stream))

(defn bytes->discord-audio [bs]
  (AudioInputStream. (ByteArrayInputStream. bs)
                     (AudioFormat. 48000 16 2 true true)
                     (count bs)))

(defn bytes->sphinx-audio [bs-raw]
  (let [bs (->> bs-raw
                (partition 2)
                (into [] (comp (take-nth 6)
                               (map reverse)))
                (flatten)
                (byte-array))]
    (AudioInputStream. (ByteArrayInputStream. bs)
                       (AudioFormat. 16000 16 1 true false)
                       (count bs))))

(defn write [audio-stream target]
  (AudioSystem/write audio-stream file-type (io/output-stream target)))

(defn ->bytes [s]
  (.toByteArray s))
(defn byte-array-output []
  (ByteArrayOutputStream.))

(defn writeToFile [out filename]
(write
 (bytes->sphinx-audio (->bytes out))
 (clojure.java.io/output-stream (str "/tmp/" filename ".wav") )))

(defn foo [a b]
  (str a " " b))