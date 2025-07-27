(ns minecraft-server-clojure.protocol-types-test
  (:require [clojure.test :refer :all])
  (:use minecraft-server-clojure.protocol-types))

(deftest test-read-varint
  (let [output (read-varint
                 (byte-array [0xdd 0xc7 0x01]))]
    (testing (str 25565 " == " output)
      (is (==
            output
            25565)))))

(deftest test-write-varint
  (let [output (write-varint 25565)]
    (let [expected [0xdd 0xc7 0x01]]
      (testing (str expected " == " output )
        (is (=
              expected
              output))))))
