(ns minecraft-server-clojure.packets
  (:use [minecraft-server-clojure.protocol-types]
        [minecraft-server-clojure.utils])
  (:import (java.io ByteArrayOutputStream InputStream OutputStream)))

(defn read-c2s-handshake-packet [^InputStream stream]
  {:protocol-version (read-varint stream)
   :server-address   (read-prefixed-string stream)
   :server-port      (read-short stream)
   :intent           (read-varint stream)})

(defn handle-c2s-handshake-packet [^InputStream inputStream ^OutputStream outputStream]
  (do
    (println "handshake packet!!!")
    {:protocol (get
                 (read-c2s-handshake-packet inputStream)
                 :intent)}))

(defn make-status-response-json [resp]
  (str
    "{\"version\":{\"name\":\"" (:version-name resp)
    "\",\"protocol\":" (or
                         (:version-protocol resp)
                         772)
    "},\"players\":{\"max\":" (or
                                (:players-max resp)
                                1)
    ",\"online\":0,\"sample\":[]},\"description\":{\"text\":\"" (:motd resp)
    "\"},\"ensuresSecureChat\":true}"))

(defn write-packet [^OutputStream output input id]
  "writes the packet `id` and the contents of `input` into `output` (everything is length prefixed)"
  (do
    (write-prefixed-bytes
      output
      (let [x (ByteArrayOutputStream.)]
        (do
          (write-varint id x)
          (.write x input)
          (.toByteArray x))))
    (.flush output)))

(defn write-s2c-status-response-packet [status-response ^OutputStream stream]
  (write-packet
    stream
    (let [x (ByteArrayOutputStream.)]
      (do
        (write-prefixed-string
          (make-status-response-json
            status-response)
          x)
        (.toByteArray x)))
    0))


(defn handle-c2s-status-request-packet
  ([^OutputStream outputStream]
   (do
     (println "status request!!!!")
     (write-s2c-status-response-packet
       {:version-name     "1.21.8"
        :version-protocol 770
        :motd             "hello from clojure!"}
       outputStream)
     {}))
  ([^InputStream inputStream ^OutputStream outputStream]
   (handle-c2s-status-request-packet outputStream)))
