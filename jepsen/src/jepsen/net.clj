(ns jepsen.net
  "Controls network manipulation."
  (:use jepsen.control)
  (:require [jepsen.control.net :as control.net]))

(defprotocol Net
  (drop! [net test src dest] "Drop traffic between nodes src and dest.")
  (heal! [net test]          "End all traffic drops."))

(def noop
  "Does nothing."
  (reify Net
    (drop! [net test src dest])
    (heal! [net test])))

(def iptables
  "Default iptables (assumes we control everything)."
  (reify Net
    (drop! [net test src dest]
      (on dest (su (exec :iptables :-A :INPUT :-s (control.net/ip src) :-j :DROP))))

    (heal! [net test]
      (on-many (:nodes test) (su
                               (exec :iptables :-F)
                               (exec :iptables :-X))))))
