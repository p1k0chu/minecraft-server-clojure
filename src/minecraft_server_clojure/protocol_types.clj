(ns minecraft-server-clojure.protocol-types
  (:import (java.io InputStream OutputStream))
  (:use [minecraft-server-clojure.utils]))

(defn validate-varint-shift
  "throws if the varint shift (size-1) is too big"
  [shift]
  (when (>= shift 5)
    (throw
      (IllegalStateException.
        (str "VarInt cannot be of size " (inc shift))))))

(defn put-into-varint
  "puts the byte `value` into a `varint` at the specified `shift`"
  [value varint shift]
  (bit-or
    varint
    (bit-shift-left
      (bit-and
        value
        127)
      (* shift 7))))

(defn is-last-varint-byte
  "checks if the byte has the continuation bit set for VarInt's"
  [byte]
  (not
    (==
      (bit-and
        byte
        128)
      128)))

(defn read-varint
  "reads a VarInt from byte array `bytes`"
  ([^InputStream stream shift result]
   (do
     (validate-varint-shift shift)
     (let [byte (check
                  #(!= % -1)
                  (.read stream)
                  #(str "end of stream while reading varint: shift=" shift
                        ",result=" result
                        ",byte=" %))]
       (let [new-varint (put-into-varint
                          byte
                          result
                          shift)]
         (if (is-last-varint-byte byte)
           new-varint
           (read-varint
             stream
             (inc shift)
             new-varint))))))
  ([^InputStream stream] (read-varint stream 0 0)))

(defn write-varint
  ([varint shift ^OutputStream stream]
   (let [x (unsigned-bit-shift-right
             varint
             (* shift 7))]
     (if (not
           (==
             (bit-and x -128)
             0))
       (do
         (.write
           stream
           (bit-or
             (bit-and x 127)
             128))
         (write-varint
           varint
           (inc shift)
           stream))
       (.write
         stream
         (bit-and x 127)))))
  ([varint stream] (write-varint varint 0 stream)))

(defn read-prefixed-string [^InputStream stream]
  (let [size (read-varint stream)]
    (String.
      (.readNBytes
        stream
        size))))

(defn write-prefixed-string [^String value ^OutputStream stream]
  (let [x (.getBytes value "UTF-8")]
    (do
      (write-varint
        (alength x)
        stream)
      (.write
        stream
        x))))

(defn read-n-bytes-long [^InputStream stream n]
  (reduce
    bit-or
    (for [x (range n)]
      (bit-shift-left
        (bit-and
          (check
            #(!= % -1)
            (.read stream)
            #"AAAAAAAAAAAAAAA")
          0xFF)
        (*
          (- n x 1)
          8)))))

(defn read-short [stream]
  (read-n-bytes-long stream Short/BYTES))

(defn write-prefixed-bytes [^OutputStream output input]
  (do
    (write-varint
      (alength input)
      output)
    (.write
      output
      input)))
