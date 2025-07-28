(ns minecraft-server-clojure.utils)

(defn check [check-f x msg-f]
  "check-f must be a function that takes one argument.
  throws an exception with message supplied by msg-f (with x as an arg)
  if `(check-f x)` returns false. otherwise returns x"
  (if (not (check-f x))
    (throw (RuntimeException. (str (msg-f x))))
    x))
