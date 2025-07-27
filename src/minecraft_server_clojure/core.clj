(ns minecraft-server-clojure.core
  (:import (java.io PrintWriter)
           (java.net ServerSocket)))

(defn -main [& args]
  "the main"
  (let [socket (ServerSocket. 25565)]
    (while true
      (let [client-sock (.accept socket)]
        (do
          (let [writer (PrintWriter.
                         (.getOutputStream
                           client-sock)
                         true)]
            (.println writer ":3"))
          (.close client-sock))))))
