
(ns docs.prototypes
    (:require [candy.api         :refer [param]]
              [mid-fruits.string :as string]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn options-prototype
  ; @param (map) options
  ;  {}
  ;
  ; @return
  ;  {}
  [{:keys [path] :as options}]
  (merge {}
         (param options)
         {:path (-> path (string/not-starts-with! "/")
                         (string/not-ends-with!   "/"))}))
