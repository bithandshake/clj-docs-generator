
(ns docs.process.env
    (:require [docs.import.state :as import.state]
              [string.api        :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn clj-library?
  ; @return (boolean)
  []
  (or (-> (get @import.state/LAYERS "clj")  empty? not)
      (-> (get @import.state/LAYERS "cljc") empty? not)))

(defn cljs-library?
  ; @return (boolean)
  []
  (or (-> (get @import.state/LAYERS "cljc") empty? not)
      (-> (get @import.state/LAYERS "cljs") empty? not)))
