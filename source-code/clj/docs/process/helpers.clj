
(ns docs.process.helpers
    (:require [docs.import.state :as import.state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn clj-library?
  ; @return (boolean)
  []
  (or (get @import.state/LAYERS "clj")
      (get @import.state/LAYERS "cljc")))

(defn cljs-library?
  ; @return (boolean)
  []
  (or (get @import.state/LAYERS "cljs")
      (get @import.state/LAYERS "cljc")))
