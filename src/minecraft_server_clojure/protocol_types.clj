(ns minecraft-server-clojure.protocol-types
  (:import (java.io InputStream OutputStream))
  (:use [minecraft-server-clojure.utils]))

(defn validate-varint-shift [shift]
  "throws if the varint shift (size-1) is too big"
  (when (>= shift 5)
    (throw
      (IllegalStateException.
        (str "VarInt cannot be of size " (inc shift))))))

(defn put-into-varint [value varint shift]
  "puts the byte `value` into a `varint` at the specified `shift`"
  (bit-or
    varint
    (bit-shift-left
      (bit-and
        value
        127)
      (* shift 7))))

(defn is-last-varint-byte [byte]
  "checks if the byte has the continuation bit set for VarInt's"
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
     (let [byte (.read stream)]
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

