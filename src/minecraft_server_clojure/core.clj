(ns minecraft-server-clojure.core
  (:use [minecraft-server-clojure.protocol-types]
        [minecraft-server-clojure.packets])
  (:import (java.io ByteArrayInputStream InputStream OutputStream)
           (java.net ServerSocket)))

(defn read-one-packet [^InputStream stream]
  "returns an input stream containing one packet"
  (let [size (read-varint stream)]
    (ByteArrayInputStream.
      (.readNBytes
        stream
        size))))

(def c2s-handshake-protocol
  {0 handle-c2s-handshake-packet})

(def c2s-status-protocol
  {0 handle-c2s-status-request-packet})

(def c2s-packet-protocols
  {0 c2s-handshake-protocol
   1 c2s-status-protocol})

(defn handle-packet [^InputStream inputStream ^OutputStream outputStream protocol]
  ((get
     (get c2s-packet-protocols protocol)
     (read-varint
       inputStream))
   (.readAllBytes inputStream)
   outputStream))

(defn -main [& args]
  "the main"
  (let [socket (ServerSocket. 25565)]
    (do
      (println "Listening on "
               (.toString
                 (.getLocalSocketAddress socket))
               ":"
               (.getLocalPort socket))
      (while true
        (let [client-sock (.accept socket)]
          (let [inputStream (.getInputStream client-sock)]
            (let [outputStream (.getOutputStream client-sock)]
              (let [state (atom {:protocol 0})]
                (while true
                  (swap!
                    state
                    conj (handle-packet
                           (read-one-packet
                             inputStream)
                           outputStream
                           (:protocol @state))))))))))))

