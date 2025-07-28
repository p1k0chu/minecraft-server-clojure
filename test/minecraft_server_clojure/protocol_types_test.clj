(ns minecraft-server-clojure.protocol-types-test
  (:require [clojure.test :refer :all])
  (:use [minecraft-server-clojure.protocol-types])
  (:import (java.io ByteArrayInputStream)))

(defn test-read-varint [input expected]
  (let [output (read-varint
                 (ByteArrayInputStream.
                   (byte-array input)))]
    (testing (str expected " == " output)
      (is (==
            output
            expected)))))

(defn test-write-varint [input expected]
  (testing (str "writing varint " input)
    (is (=
          expected
          (varint-bytes input)))))

(defn test-varint [bytes varint]
  (do
    (test-read-varint bytes varint)
    (test-write-varint varint bytes)))

(deftest test-read-write-varints
  (do
    (test-varint
      [0xdd 0xc7 0x01]
      25565)
    (test-varint
      [2]
      2)
    (test-varint
      [1]
      1)
    (test-varint
      [128 1]
      128)
    (test-varint
      [127]
      127)))
