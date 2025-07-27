(ns minecraft-server-clojure.protocol-types
  (:use [minecraft-server-clojure.core]))

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
  ([bytes shift result]
   (do
     (validate-varint-shift shift)
     (let [byte (get bytes shift)]
       (let [new-varint (put-into-varint
                          byte
                          result
                          shift)]
         (if (is-last-varint-byte byte)
           new-varint
           (read-varint
             bytes
             (inc shift)
             new-varint))))))
  ([bytes] (read-varint bytes 0 0)))
