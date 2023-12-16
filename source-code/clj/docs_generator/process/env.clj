
(ns docs-generator.process.env
    (:require [docs-generator.import.state :as import.state]
              [fruits.string.api           :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn clj-library?
  ; @ignore
  ;
  ; @return (boolean)
  []
  (or (-> (get @import.state/LAYERS "clj")  empty? not)
      (-> (get @import.state/LAYERS "cljc") empty? not)))

(defn cljs-library?
  ; @ignore
  ;
  ; @return (boolean)
  []
  (or (-> (get @import.state/LAYERS "cljc") empty? not)
      (-> (get @import.state/LAYERS "cljs") empty? not)))
