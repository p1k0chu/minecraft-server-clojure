(ns minecraft-server-clojure.protocol-types
  (:use [minecraft-server-clojure.core])
  (:import (java.io InputStream)))

(defn validate-varint-shift [shift]
  "throws if the varint shift (size-1) is too big"
  (when (>= shift 5)
    (throw
      (IllegalStateException.
        (str "VarInt cannot be of size" (inc shift))))))

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
  "returns a byte array that represents x as written VarInt"
  ([varint shift bytes]
   (let [x (unsigned-bit-shift-right
             varint
             (* shift 7))]
     (if (not
           (==
             (bit-and x -128)
             0))
       (write-varint
         varint
         (inc shift)
         (conj
           bytes
           (bit-or
             (bit-and x 127)
             128)))
       (conj
         bytes
         (bit-and x 127)))))
  ([varint] (write-varint varint 0 [])))

