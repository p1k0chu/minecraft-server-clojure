(ns minecraft-server-clojure.utils)

(defn check
  "check-f must be a function that takes one argument.
  throws an exception with message supplied by msg-f (with x as an arg)
  if `(check-f x)` returns false. otherwise returns x"
  [check-f x msg-f]
  (if (not (check-f x))
    (throw (RuntimeException. (str (msg-f x))))
    x))

(defn != [left right]
  (not
    (==
      left
      right)))
