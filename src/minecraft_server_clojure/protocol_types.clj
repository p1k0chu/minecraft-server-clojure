(ns minecraft-server-clojure.protocol-types
  (:import (java.io InputStream OutputStream)
           (java.nio.charset StandardCharsets))
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

(defn varint-size
  "returns the size of the varint on the beginning of the coll"
  [coll]
  (let [x (atom 0)]
    (do
      (while (and
               (<
                 @x
                 (count coll))
               (!=
                 (bit-and
                   (get coll @x)
                   128)
                 128))
        (swap!
          x
          inc))
      @x)))

(defn read-varint-real [bytes]
  (reduce
    bit-or
    (map-indexed
      #(bit-shift-left
         %2
         (* %1 7))
      bytes)))

(defn read-varint-real-use-this
  "returns map with key :result (the varint) and :leftover (the bytes left from reading)"
  [coll]
  (let [size (varint-size coll)]
    {:result (read-varint-real
               (take
                 size
                 coll))
     :leftover (drop
                 size
                 coll)}))

(defn read-varint-side-effect
  "convenience function to read a VarInt from a atom coll"
  [atom-coll]
  (let [x (read-varint-real-use-this @atom-coll)]
    (do
      (swap!
        atom-coll
        (:leftover x))
      (:result x))))

(defn varint-bytes
  "returns a byte array for the VarInt"
  ([current return-bytes]
   (if (!=
         (bit-and current -128)
         0)
     (varint-bytes
       (unsigned-bit-shift-right
         current
         7)
       (conj
         return-bytes
         (bit-or
           (bit-and current 127)
           128)))
     (conj
       return-bytes
       (bit-and current 127))))
  ([int] (varint-bytes int [])))

(defn read-prefixed-string [^InputStream stream]
  (let [size (read-varint stream)]
    (String.
      (.readNBytes
        stream
        size))))

(defn prefix-bytes
  "prefixes byte array `bytes` with its length as VarInt"
  [bytes]
  (reduce
    #(conj %1 %2)
    (varint-bytes
      (count bytes))
    bytes))

(defn write-prefixed-string [^String value ^OutputStream stream]
  (.write
    stream
    (byte-array
      (prefix-bytes
        (.getBytes
          value
          StandardCharsets/UTF_8)))))

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
